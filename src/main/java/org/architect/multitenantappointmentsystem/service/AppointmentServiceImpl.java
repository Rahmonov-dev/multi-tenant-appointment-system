package org.architect.multitenantappointmentsystem.service;

import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.request.CancelAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.request.CreateAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.request.RescheduleAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.request.*;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentCalendarResponse;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentResponse;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentStatisticsResponse;
import org.architect.multitenantappointmentsystem.dto.response.AvailableSlotResponse;
import org.architect.multitenantappointmentsystem.entity.*;
import org.architect.multitenantappointmentsystem.exception.*;
import org.architect.multitenantappointmentsystem.repository.*;
import org.architect.multitenantappointmentsystem.security.TenantContext;
import org.architect.multitenantappointmentsystem.service.interfaces.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TenantRepository tenantRepository;
    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final StaffScheduleRepository staffScheduleRepository;
    private final CurrentStaffService currentStaffService;

    /**
     * Appointment yaratish (navbat olish)
     *
     * @param request
     */
    @Override
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        // 1. Validate entities
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant topilmadi"));

        Staff staff = staffRepository.findById(request.staffId())
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));

        Employement employement =
                serviceRepository.findByIdAndTenantId(request.serviceId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi"));

        // 2. Validate tenant consistency
        if (!staff.getTenant().getId().equals(tenant.getId())) {
            throw new BusinessException("Staff boshqa tenantga tegishli");
        }
        if (!employement.getTenant().getId().equals(tenant.getId())) {
            throw new BusinessException("Employement boshqa tenantga tegishli");
        }

        // 3. Validate date (advance booking days)
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(tenant.getAdvanceBookingDays());
        if (request.appointmentDate().isAfter(maxDate)) {
            throw new BusinessException(
                    "Faqat " + tenant.getAdvanceBookingDays() + " kun oldinga navbat olish mumkin");
        }
        if (request.appointmentDate().isBefore(today)) {
            throw new BusinessException("O'tmishga navbat olib bo'lmaydi");
        }

        // 4. Validate staff schedule
        int dayOfWeek = request.appointmentDate().getDayOfWeek().getValue();
        StaffSchedule schedule = staffScheduleRepository
                .findByStaffIdAndDayOfWeek(staff.getId(), dayOfWeek)
                .orElseThrow(() -> new BusinessException("Staff jadvali topilmadi"));

        if (!schedule.getIsAvailable()) {
            throw new BusinessException("Staff shu kuni ishlamaydi");
        }

        if (!schedule.isWorkingTime(request.startTime())) {
            throw new BusinessException(
                    "Staff ish vaqtida emas. Ish vaqti: " + 
                    schedule.getStartTime() + " - " + schedule.getEndTime());
        }

        // 5. Calculate end time
        LocalTime endTime = request.startTime().plusMinutes(employement.getDuration());

        // Validate end time within working hours
        if (endTime.isAfter(schedule.getEndTime())) {
            throw new BusinessException("Appointment ish vaqtidan tashqariga chiqib ketadi");
        }

        // 6. Check time conflict
        boolean hasConflict = appointmentRepository.hasTimeConflict(
                staff.getId(),
                request.appointmentDate(),
                request.startTime(),
                endTime
        );

        if (hasConflict) {
            throw new BusinessException("Bu vaqt allaqachon band");
        }

        // 7. Create appointment
        Appointment appointment = new Appointment();
        appointment.setTenant(tenant);
        appointment.setStaff(staff);
        appointment.setEmployement(employement);
        appointment.setCustomerName(request.customerName());
        appointment.setCustomerPhone(request.customerPhone());
        appointment.setCustomerEmail(request.customerEmail());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(endTime);
        appointment.setTotalPrice(employement.getPrice());
        appointment.setNotes(request.notes());

        // Auto-confirm if enabled
        if (tenant.getAutoConfirmBooking()) {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointment.setConfirmedAt(java.time.LocalDateTime.now());
        } else {
            appointment.setStatus(AppointmentStatus.PENDING);
        }

        appointment = appointmentRepository.save(appointment);

        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Appointment ma'lumotlarini olish (ID bo'yicha)
     *
     * @param id
     */
    @Override
    public AppointmentResponse getAppointmentById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (id == null) {
            throw new BusinessException("Appointment ID talab qilinadi");
        }
        
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Appointment topilmadi: " + id));
        
        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Appointment yangilash
     *
     * @param id
     * @param request
     */
    @Override
    public AppointmentResponse updateAppointment(Long id, UpdateAppointmentRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (id == null) {
            throw new BusinessException("Appointment ID talab qilinadi");
        }
        
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Appointment topilmadi: " + id));

        // Can only update pending or confirmed appointments
        if (appointment.isFinal()) {
            throw new BusinessException("Yakunlangan yoki bekor qilingan appointmentni yangilab bo'lmaydi");
        }

        // Update fields
        if (request.customerName() != null) {
            appointment.setCustomerName(request.customerName());
        }
        if (request.notes() != null) {
            appointment.setNotes(request.notes());
        }

        appointment = appointmentRepository.save(appointment);
        
        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Appointment vaqtini o'zgartirish
     *
     * @param id
     * @param request
     */
    @Override
    public AppointmentResponse rescheduleAppointment(Long id, RescheduleAppointmentRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (id == null) {
            throw new BusinessException("Appointment ID talab qilinadi");
        }
        
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Appointment topilmadi: " + id));

        // Can only reschedule pending or confirmed appointments
        if (appointment.isFinal()) {
            throw new BusinessException("Yakunlangan yoki bekor qilingan appointmentni o'zgartirib bo'lmaydi");
        }

        // Validate new date
        LocalDate today = LocalDate.now();
        if (request.newDate().isBefore(today)) {
            throw new BusinessException("O'tmishga navbat olib bo'lmaydi");
        }

        // Validate staff schedule for new date
        int dayOfWeek = request.newDate().getDayOfWeek().getValue();
        StaffSchedule schedule = staffScheduleRepository
                .findByStaffIdAndDayOfWeek(appointment.getStaff().getId(), dayOfWeek)
                .orElseThrow(() -> new BusinessException("Staff jadvali topilmadi"));

        if (!schedule.getIsAvailable()) {
            throw new BusinessException("Staff shu kuni ishlamaydi");
        }

        if (!schedule.isWorkingTime(request.newTime())) {
            throw new BusinessException("Staff ish vaqtida emas");
        }

        // Calculate new end time
        LocalTime newEndTime = request.newTime().plusMinutes(appointment.getEmployement().getDuration());

        // Check time conflict (excluding current appointment)
        boolean hasConflict = appointmentRepository.hasTimeConflict(
                appointment.getStaff().getId(),
                request.newDate(),
                request.newTime(),
                newEndTime
        );

        if (hasConflict) {
            throw new BusinessException("Yangi vaqt allaqachon band");
        }

        // Update appointment
        appointment.setAppointmentDate(request.newDate());
        appointment.setStartTime(request.newTime());
        appointment.setEndTime(newEndTime);
        
        if (request.reason() != null) {
            String existingNotes = appointment.getNotes() != null ? appointment.getNotes() : "";
            appointment.setNotes(existingNotes + "\n[Vaqt o'zgartirildi: " + request.reason() + "]");
        }

        appointment = appointmentRepository.save(appointment);
        
        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Appointment bekor qilish
     *
     * @param id
     * @param request
     */
    @Override
    public AppointmentResponse cancelAppointment(Long id, CancelAppointmentRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (id == null) {
            throw new BusinessException("Appointment ID talab qilinadi");
        }
        
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Appointment topilmadi: " + id));

        if (appointment.isFinal()) {
            throw new BusinessException("Allaqachon yakunlangan yoki bekor qilingan");
        }

        appointment.cancel();
        
        if (request != null && request.reason() != null) {
            String existingNotes = appointment.getNotes() != null ? appointment.getNotes() : "";
            appointment.setNotes(existingNotes + "\n[Bekor qilish sababi: " + request.reason() + "]");
        }

        appointment = appointmentRepository.save(appointment);
        
        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Appointment tasdiqlash
     *
     * @param id
     */
    @Override
    public AppointmentResponse confirmAppointment(Long id) {
        currentStaffService.requireOwnerOrManager();
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (id == null) {
            throw new BusinessException("Appointment ID talab qilinadi");
        }
        
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Appointment topilmadi: " + id));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException("Faqat PENDING statusdagi appointmentlarni tasdiqlash mumkin");
        }

        appointment.confirm();
        appointment = appointmentRepository.save(appointment);
        
        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Appointment yakunlash
     *
     * @param id
     */
    @Override
    public AppointmentResponse completeAppointment(Long id) {
        currentStaffService.requireOwnerOrManager();
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (id == null) {
            throw new BusinessException("Appointment ID talab qilinadi");
        }
        
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Appointment topilmadi: " + id));

        if (!appointment.isActive()) {
            throw new BusinessException("Faqat aktiv appointmentlarni yakunlash mumkin");
        }

        appointment.complete();
        appointment = appointmentRepository.save(appointment);
        
        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Appointment "No Show" qilish
     *
     * @param id
     */
    @Override
    public AppointmentResponse markAsNoShow(Long id) {
        currentStaffService.requireOwnerOrManager();
        Long tenantId = TenantContext.getTenantId();
        if (id == null) {
            throw new BusinessException("Appointment ID talab qilinadi");
        }
        
        Appointment appointment = appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Appointment topilmadi: " + id));

        if (!appointment.isActive()) {
            throw new BusinessException("Faqat aktiv appointmentlarni no-show qilish mumkin");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment = appointmentRepository.save(appointment);
        
        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Bo'sh vaqtlarni olish
     *
     * @param staffId
     * @param date
     * @param serviceId
     */
    @Override
    public List<AvailableSlotResponse> getAvailableSlots(Long staffId, LocalDate date, Long serviceId) {
        // Get staff
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (staffId == null) {
            throw new BusinessException("Staff ID talab qilinadi");
        }
        
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));
        if (!staff.getTenant().getId().equals(tenantId)) {
            throw new BusinessException("Staff boshqa tenantga tegishli");
        }

        // Get schedule for this day
        int dayOfWeek = date.getDayOfWeek().getValue();
        StaffSchedule schedule = staffScheduleRepository
                .findByStaffIdAndDayOfWeek(staffId, dayOfWeek)
                .orElseThrow(() -> new BusinessException("Staff jadvali topilmadi"));

        if (!schedule.getIsAvailable()) {
            return Collections.emptyList();
        }

        // Get slot duration from tenant
        Integer slotDuration = staff.getTenant().getSlotDuration();

        // If service provided, use service duration instead
        if (serviceId != null) {
            Employement employement =
                    serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                    .orElseThrow(() -> new NotFoundException("Employement topilmadi"));
            slotDuration = employement.getDuration();
        }

        // Generate all possible slots
        LocalTime startTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        List<AvailableSlotResponse> slots = new ArrayList<>();

        // Get booked appointments for this day
        List<Appointment> bookedAppointments = appointmentRepository
                .findActiveAppointments(staffId, date, Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED));

        LocalTime current = startTime;
        while (current.plusMinutes(slotDuration).isBefore(endTime) || 
               current.plusMinutes(slotDuration).equals(endTime)) {
            
            LocalTime slotStart = current;
            LocalTime slotEnd = current.plusMinutes(slotDuration);

            // Check if slot is available
            boolean available = bookedAppointments.stream()
                    .noneMatch(app -> 
                        (slotStart.isBefore(app.getEndTime()) && slotEnd.isAfter(app.getStartTime()))
                    );

            slots.add(new AvailableSlotResponse(
                    slotStart,
                    available,
                    slotStart.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            ));

            current = current.plusMinutes(slotDuration);
        }

        return slots;
    }

    /**
     * Vaqt bo'shligini tekshirish
     *
     * @param staffId
     * @param date
     * @param time
     * @param duration
     */
    @Override
    public boolean isSlotAvailable(Long staffId, LocalDate date, LocalTime time, Integer duration) {
        LocalTime endTime = time.plusMinutes(duration);
        
        return !appointmentRepository.hasTimeConflict(staffId, date, time, endTime);
    }

    /**
     * Tenant bo'yicha appointmentlarni olish
     * @param date
     */
    @Override
    public List<AppointmentResponse> getAppointmentsByTenant(LocalDate date) {
        currentStaffService.requireOwnerOrManager();
        Long tenantId = TenantContext.getTenantId();
        return appointmentRepository.findByTenantIdAndAppointmentDate(tenantId, date)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Staff bo'yicha appointmentlarni olish
     *
     * @param staffId
     * @param date
     */
    @Override
    public List<AppointmentResponse> getAppointmentsByStaff(Long staffId, LocalDate date) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (staffId == null) {
            throw new BusinessException("Staff ID talab qilinadi");
        }
        
        // Validate staff belongs to current tenant
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + staffId));
        if (!staff.getTenant().getId().equals(tenantId)) {
            throw new BusinessException("Staff boshqa tenantga tegishli");
        }
        
        return appointmentRepository.findByStaffIdAndAppointmentDate(staffId, date)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Employement bo'yicha appointmentlarni olish
     *
     * @param serviceId
     * @param date
     */
    @Override
    public List<AppointmentResponse> getAppointmentsByService(Long serviceId, LocalDate date) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (serviceId == null) {
            throw new BusinessException("Service ID talab qilinadi");
        }
        
        // Validate service belongs to current tenant
        Employement service = serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + serviceId));
        
        return appointmentRepository.findByEmployementIdAndAppointmentDate(serviceId, date)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Mijoz telefon raqami bo'yicha appointmentlarni olish
     *
     * @param phone
     */
    @Override
    public List<AppointmentResponse> getAppointmentsByCustomerPhone(String phone) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("Telefon raqami talab qilinadi");
        }
        
        return appointmentRepository.findByCustomerPhoneAndTenantId(phone.trim(), tenantId)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Mijoz email bo'yicha appointmentlarni olish
     *
     * @param email
     */
    @Override
    public List<AppointmentResponse> getAppointmentsByCustomerEmail(String email) {
        return appointmentRepository.findUpcomingAppointmentsByEmail(email, LocalDate.now())
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Bugungi appointmentlar
     */
    @Override
    public List<AppointmentResponse> getTodayAppointments( ) {
        Long tenantId = TenantContext.getTenantId();
        return appointmentRepository.findTodayAppointments(tenantId)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Kelajakdagi appointmentlar
     * @param limit
     */
    @Override
    public List<AppointmentResponse> getUpcomingAppointments( Integer limit) {
        Long tenantId = TenantContext.getTenantId();
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit != null ? limit : 10);
        return appointmentRepository.findUpcomingAppointments(tenantId, pageable)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Kelajakdagi appointmentlar (mijoz telefoni bo'yicha)
     *
     * @param phone
     */
    @Override
    public List<AppointmentResponse> getUpcomingAppointmentsByPhone(String phone) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("Telefon raqami talab qilinadi");
        }
        
        return appointmentRepository.findUpcomingAppointmentsByPhoneAndTenantId(phone.trim(), LocalDate.now(), tenantId)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * O'tmish appointmentlar (mijoz telefoni bo'yicha)
     *
     * @param phone
     * @param limit
     */
    @Override
    public List<AppointmentResponse> getPastAppointmentsByPhone(String phone, Integer limit) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("Telefon raqami talab qilinadi");
        }
        
        // Use tenant-scoped query
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit != null ? limit : 10);
        return appointmentRepository.findPastAppointmentsByPhoneAndTenantId(phone.trim(), tenantId, pageable)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Status bo'yicha appointmentlar
     * @param status
     */
    @Override
    public List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status) {
        Long tenantId = TenantContext.getTenantId();
        return appointmentRepository.findByTenantIdAndStatus(tenantId, status)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Pagination bilan appointmentlar
     *
     * @param activeOnly
     * @param pageable
     */
    @Override
    public Page<AppointmentResponse> getAppointmentsPaginated( Boolean activeOnly, Pageable pageable) {
        currentStaffService.requireOwnerOrManager();
        Long tenantId = TenantContext.getTenantId();
        Page<Appointment> appointmentPage = appointmentRepository.findByTenantId(tenantId, pageable);
        return appointmentPage.map(AppointmentResponse::fromEntity);
    }

    /**
     * Sana oralig'i bo'yicha appointmentlar
     *
     * @param startDate
     * @param endDate
     */
    @Override
    public List<AppointmentResponse> getAppointmentsByDateRange( LocalDate startDate, LocalDate endDate) {
        currentStaffService.requireOwnerOrManager();
        Long tenantId = TenantContext.getTenantId();
        return appointmentRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Staff bo'yicha sana oralig'ida appointmentlar
     *
     * @param staffId
     * @param startDate
     * @param endDate
     */
    @Override
    public List<AppointmentResponse> getStaffAppointmentsByDateRange(Long staffId, LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.findByStaffIdAndDateRange(staffId, startDate, endDate)
                .stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Calendar view uchun ma'lumotlar
     * @param startDate
     * @param endDate
     */
    @Override
    public List<AppointmentCalendarResponse> getCalendarData(LocalDate startDate, LocalDate endDate) {
        currentStaffService.requireOwnerOrManager();
        Long tenantId = TenantContext.getTenantId();
        List<Appointment> appointments = appointmentRepository
                .findByTenantIdAndDateRange(tenantId, startDate, endDate);

        Map<LocalDate, List<Appointment>> appointmentsByDate = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getAppointmentDate));

        List<AppointmentCalendarResponse> calendar = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<Appointment> dayAppointments = appointmentsByDate.getOrDefault(current, Collections.emptyList());
            
            long confirmed = dayAppointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                    .count();
            
            long pending = dayAppointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.PENDING)
                    .count();
            
            long completed = dayAppointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .count();

            String dayName = current.getDayOfWeek()
                    .getDisplayName(java.time.format.TextStyle.FULL, new Locale("uz", "UZ"));

            calendar.add(AppointmentCalendarResponse.fromEntity(
                    current,
                    dayName,
                    dayAppointments.size(),
                    (int) confirmed,
                    (int) pending,
                    (int) completed
            ));

            current = current.plusDays(1);
        }

        return calendar;
    }

    /**
     * Tenant bo'yicha statistika
     */
    @Override
    public AppointmentStatisticsResponse getStatistics() {
        currentStaffService.requireOwnerOrManager();
        Long tenantId = TenantContext.getTenantId();

        List<Appointment> allAppointments = appointmentRepository.findByTenantId(tenantId, 
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        return calculateStatistics(tenantId, allAppointments);
    }

    /**
     * Staff bo'yicha statistika
     *
     * @param staffId
     */
    @Override
    public AppointmentStatisticsResponse getStaffStatistics(Long staffId) {
        currentStaffService.requireOwnerOrManager();
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context topilmadi");
        }
        if (staffId == null) {
            throw new BusinessException("Staff ID talab qilinadi");
        }
        
        // Validate staff belongs to current tenant
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + staffId));
        if (!staff.getTenant().getId().equals(tenantId)) {
            throw new BusinessException("Staff boshqa tenantga tegishli");
        }
        
        List<Appointment> allAppointments = appointmentRepository.findByStaffId(staffId, 
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        return calculateStatistics(tenantId, allAppointments);
    }

    /**
     * Sana oralig'i bo'yicha statistika
     *
     * @param startDate
     * @param endDate
     */
    @Override
    public AppointmentStatisticsResponse getStatisticsByDateRange( LocalDate startDate, LocalDate endDate) {
        currentStaffService.requireOwnerOrManager();
        Long tenantId = TenantContext.getTenantId();
        List<Appointment> appointments = appointmentRepository
                .findByTenantIdAndDateRange(tenantId, startDate, endDate);

        return calculateStatistics(tenantId, appointments);
    }

    private AppointmentStatisticsResponse calculateStatistics(Long tenantId,List<Appointment> appointments) {
        long total = appointments.size();
        long pending = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING)
                .count();
        long confirmed = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                .count();
        long completed = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();
        long cancelled = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                .count();
        long noShow = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW)
                .count();

        // Revenue calculations
        BigDecimal totalRevenue = appointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .map(Appointment::getTotalPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        BigDecimal pendingRevenue = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING)
                .map(Appointment::getTotalPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        BigDecimal completedRevenue = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .map(Appointment::getTotalPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Average appointments per day
        long uniqueDays = appointments.stream()
                .map(Appointment::getAppointmentDate)
                .distinct()
                .count();
        double avgPerDay = uniqueDays > 0 ? (double) total / uniqueDays : 0;

        // Completion rate
        long totalNonCancelled = total - cancelled;
        double completionRate = totalNonCancelled > 0 
                ? (double) completed / totalNonCancelled * 100 
                : 0;

        // Cancellation rate
        double cancellationRate = total > 0 
                ? (double) cancelled / total * 100 
                : 0;

        return AppointmentStatisticsResponse.fromEntity(
                tenantId,
                total,
                pending,
                confirmed,
                completed,
                cancelled,
                noShow,
                totalRevenue,
                pendingRevenue,
                completedRevenue,
                avgPerDay,
                completionRate,
                cancellationRate
        );
    }
}
