package org.architect.multitenantappointmentsystem.service;

import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.request.UpdateStaffScheduleRequest;
import org.architect.multitenantappointmentsystem.dto.response.StaffDetailResponse;
import org.architect.multitenantappointmentsystem.dto.request.CreateStaffRequest;
import org.architect.multitenantappointmentsystem.dto.request.CreateStaffScheduleRequest;
import org.architect.multitenantappointmentsystem.dto.request.UpdateStaffRequest;
import org.architect.multitenantappointmentsystem.dto.response.StaffResponse;
import org.architect.multitenantappointmentsystem.dto.response.StaffScheduleResponse;
import org.architect.multitenantappointmentsystem.dto.response.StaffStatisticsResponse;
import org.architect.multitenantappointmentsystem.entity.*;
import org.architect.multitenantappointmentsystem.exception.AccessDeniedException;
import org.architect.multitenantappointmentsystem.exception.BadRequestException;
import org.architect.multitenantappointmentsystem.exception.BusinessException;
import org.architect.multitenantappointmentsystem.exception.NotFoundException;
import org.architect.multitenantappointmentsystem.repository.*;
import org.architect.multitenantappointmentsystem.security.TenantContext;
import org.architect.multitenantappointmentsystem.service.interfaces.AuthService;
import org.architect.multitenantappointmentsystem.service.interfaces.StaffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {
    private final StaffRepository staffRepository;
    private final StaffScheduleRepository scheduleRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final CurrentStaffService currentStaffService;

    @Override
    @Transactional
    public StaffResponse createStaff(CreateStaffRequest request) {
        Long tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException(
                        "Tenant topilmadi: " + tenantId));
        Long currentUserId = AuthService.getCurrentUserId();
        Staff currentStaff = (Staff) staffRepository
                .findByTenantIdAndUserId(tenantId, currentUserId).orElseThrow(
                        () -> new NotFoundException("Staff topilmadi")
                );
        if (currentStaff.getRole() != StaffRole.OWNER) {
            throw new AccessDeniedException("Ruxsat yo‘q");
        }
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException(
                        "User topilmadi: " + request.userId()));

        if (staffRepository.existsByUserIdAndTenantId(request.userId(), tenantId)) {
            throw new BusinessException("Bu user allaqachon shu tenant da staff");
        }
        Staff staff = new Staff();
        staff.setTenant(tenant);
        staff.setUser(user);
        staff.setRole(request.role());
        staff.setDisplayName(request.displayName());
        staff.setPosition(request.position());
        staff.setIsActive(true);

        staff = staffRepository.save(staff);
        if (request.schedule() != null) {
            for (CreateStaffScheduleRequest scheduleRequest : request.schedule()) {
                createOrUpdateSchedule(staff.getId(), scheduleRequest);
            }
        }
        return StaffResponse.fromEntity(staff);
    }

    /**
     * Staff ma'lumotlarini olish (ID bo'yicha)
     *
     * @param id
     */
    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Staff staff = staffRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() ->
                        new NotFoundException("Bu " + id + " ga oid xodim topilmadi"));

        return StaffResponse.fromEntity(staff);
    }



    /**
     * Staff batafsil ma'lumotlarini olish (schedules va services bilan)
     */
    @Override
    @Transactional
    public StaffDetailResponse getStaffDetailById(Long staffId) {
        Long tenantId = TenantContext.getTenantId();

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Bu " + staffId + " ga oid Staff topilmadi"));

        if (!staff.getTenant().getId().equals(tenantId)) {
            throw new BadRequestException("Siz bu staffga kirish huquqiga ega emassiz");
        }
        return StaffDetailResponse.fromEntity(staff);
    }
    /**
     * Staff ma'lumotlarini yangilash
     *
     * @param id
     * @param request
     */
    @Override
    @Transactional
    public StaffResponse updateStaff(Long id, UpdateStaffRequest request) {
        Long tenantId = TenantContext.getTenantId();

        Long currentUserId = AuthService.getCurrentUserId();
        Staff currentStaff = (Staff) staffRepository.findByTenantIdAndUserId(tenantId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Hozirgi staff topilmadi"));

        if (currentStaff.getRole() != StaffRole.OWNER && currentStaff.getRole() != StaffRole.MANAGER) {
            throw new AccessDeniedException("Sizda bu amalni bajarish huquqi yo‘q");
        }

        Staff staffToUpdate = staffRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yangilanishi kerak bo‘lgan staff topilmadi: " + id));

        if (!staffToUpdate.getTenant().getId().equals(tenantId)) {
            throw new AccessDeniedException("Bu staff boshqa tenantga tegishli");
        }

        if (request.role() != null) {
            staffToUpdate.setRole(request.role());
        }
        if (request.displayName() != null && !request.displayName().isBlank()) {
            staffToUpdate.setDisplayName(request.displayName());
        }
        if (request.position() != null && !request.position().isBlank()) {
            staffToUpdate.setPosition(request.position());
        }
        if (request.isActive() != null) {
            staffToUpdate.setIsActive(request.isActive());
        }
        staffToUpdate = staffRepository.save(staffToUpdate);
        return StaffResponse.fromEntity(staffToUpdate);
    }

    @Override
    public List<StaffResponse> getAllStaffByTenant() {
        Long tenantId = TenantContext.getTenantId();
        Staff currentStaff = currentStaffService.getCurrentStaff();
        if (currentStaff.getRole() != StaffRole.OWNER && currentStaff.getRole() != StaffRole.MANAGER) {
            throw new AccessDeniedException("Ruxsat yo‘q");
        }

        return staffRepository.findByTenantId(tenantId)
                .stream()
                .map(StaffResponse::fromEntity)
                .toList();
    }

    @Override
    public List<StaffResponse> getActiveStaffByTenant() {
        Long tenantId = TenantContext.getTenantId();
        return staffRepository.findByTenantId(tenantId)
                .stream()
                .filter(Staff::getIsActive)
                .map(StaffResponse::fromEntity)
                .toList();
    }

    /**
     * Tenant va role bo'yicha stafflarni olish
     *
     * @param role
     */
    @Override
    public List<StaffResponse> getStaffByTenantAndRole(StaffRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role bo'sh bo'lishi mumkin emas");
        }
        Long tenantId = TenantContext.getTenantId();
        return staffRepository.findByTenantIdAndRole(tenantId, role)
                .stream()
                .map(StaffResponse::fromEntity)
                .toList();
    }

    /**
     * Employement bo'yicha stafflarni olish
     *
     * @param serviceId
     */
    @Override
    public List<StaffResponse> getStaffByService(Long serviceId) {
        List<Staff> staff = staffRepository.findActiveStaffByServiceId(serviceId);
        if (staff.isEmpty()) {
            throw new NotFoundException("Staff topilmadi !!!");
        }
        return staff
                .stream()
                .map(StaffResponse::fromEntity)
                .toList();
    }

    /**
     * Tenant bo'yicha stafflarni pagination bilan olish
     *
     * @param activeOnly
     * @param pageable
     */
    public Page<StaffResponse> getStaffByTenantPaginated(Boolean activeOnly, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Staff> staffPage = activeOnly != null && activeOnly
                ? staffRepository.findByTenantIdAndIsActive(tenantId, true, pageable)
                : staffRepository.findByTenantId(tenantId, pageable);

        return staffPage.map(StaffResponse::fromEntity);
    }
    /**
     * Staff schedule yaratish yoki yangilash
     *
     * @param staffId
     * @param request
     */
    @Override
    @Transactional
    public StaffScheduleResponse createOrUpdateSchedule(
            Long staffId,
            CreateStaffScheduleRequest request) {

        Long tenantId = TenantContext.getTenantId();
        Long currentUserId = AuthService.getCurrentUserId();
        Staff currentStaff = (Staff) staffRepository
                .findByTenantIdAndUserId(tenantId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("Staff topilmadi"));

        if (currentStaff.getRole() != StaffRole.OWNER &&
                currentStaff.getRole() != StaffRole.MANAGER) {
            throw new AccessDeniedException("Ruxsat yo‘q");
        }
        Staff staff = staffRepository
                .findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));

        LocalTime start = LocalTime.parse(request.startTime());
        LocalTime end = LocalTime.parse(request.endTime());

        if (end.isBefore(start)) {
            throw new BusinessException("EndTime startTime dan katta bo‘lishi kerak");
        }
        Optional<StaffSchedule> existingSchedule =
                scheduleRepository.findByStaffIdAndDayOfWeek(staffId, request.dayOfWeek());

        StaffSchedule schedule;

        if (existingSchedule.isPresent()) {
            schedule = existingSchedule.get();
            schedule.setStartTime(start);
            schedule.setEndTime(end);
            schedule.setIsAvailable(request.isAvailable());
        } else {
            schedule = new StaffSchedule();
            schedule.setStaff(staff);
            schedule.setDayOfWeek(request.dayOfWeek());
            schedule.setStartTime(start);
            schedule.setEndTime(end);
            schedule.setIsAvailable(request.isAvailable());
        }

        return StaffScheduleResponse.fromEntity(schedule);
    }



    @Override
    @Transactional
    public void deleteStaff(Long id) {

        Long tenantId = TenantContext.getTenantId();
        Staff currentStaff = currentStaffService.getCurrentStaff();

        if (currentStaff.getRole() != StaffRole.OWNER) {
            throw new AccessDeniedException("Faqat OWNER o‘chira oladi");
        }

        Staff staff = staffRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + id));

        staff.setIsActive(false);
    }


    /**
     * Staff aktivlashtirish
     *
     * @param id
     */
    @Override
    @Transactional
    public StaffResponse activateStaff(Long id) {

        Long tenantId = TenantContext.getTenantId();

        Staff staff = staffRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + id));

        staff.setIsActive(true);

        return StaffResponse.fromEntity(staff);
    }

    @Override
    @Transactional
    public StaffResponse deactivateStaff(Long id) {

        Long tenantId = TenantContext.getTenantId();

        Staff staff = staffRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + id));

        staff.setIsActive(false);

        return StaffResponse.fromEntity(staff);
    }



    @Override
    @Transactional(readOnly = true)
    public List<StaffScheduleResponse> getStaffSchedules(Long staffId) {

        Long tenantId = TenantContext.getTenantId();

        Staff staff = staffRepository
                .findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));

        return scheduleRepository.findByStaffId(staff.getId())
                .stream()
                .map(StaffScheduleResponse::fromEntity)
                .toList();
    }


    /**
     * Staff bitta kunning schedule ni olish
     *
     * @param staffId
     * @param dayOfWeek
     */
    @Override
    @Transactional(readOnly = true)
    public StaffScheduleResponse getStaffScheduleByDay(
            Long staffId,
            Integer dayOfWeek) {

        Long tenantId = TenantContext.getTenantId();

        StaffSchedule schedule = scheduleRepository
                .findByStaffIdAndDayOfWeekAndStaffTenantId(
                        staffId,
                        dayOfWeek,
                        tenantId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Schedule topilmadi: staff=" + staffId + ", day=" + dayOfWeek));

        return StaffScheduleResponse.fromEntity(schedule);
    }

    @Override
    @Transactional
    public StaffScheduleResponse updateSchedule(Long staffId,
                                                Integer dayOfWeek,
                                                UpdateStaffScheduleRequest request) {

        Long tenantId = TenantContext.getTenantId();

        StaffSchedule schedule = scheduleRepository
                .findByStaffIdAndDayOfWeekAndStaff_TenantId(
                        staffId,
                        dayOfWeek,
                        tenantId)
                .orElseThrow(() ->
                        new NotFoundException("Schedule topilmadi yoki sizga tegishli emas"));

        if (request.startTime().isAfter(request.endTime())) {
            throw new IllegalArgumentException("Start time end time dan katta bo'lishi mumkin emas");
        }

        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setIsAvailable(request.isAvailable());

        return StaffScheduleResponse.fromEntity(schedule);
    }

    /**
     * Staff schedule o'chirish (isAvailable = false qilish)
     *
     * @param staffId
     * @param dayOfWeek
     */
    @Override
    @Transactional
    public void deleteSchedule(Long staffId, Integer dayOfWeek) {
        Long tenantId = TenantContext.getTenantId();
        StaffSchedule schedule = scheduleRepository
                .findByStaffIdAndDayOfWeekAndStaffTenantId(
                        staffId,
                        dayOfWeek,
                        tenantId
                )
                .orElseThrow(() -> new NotFoundException("Schedule topilmadi"));

        schedule.setIsAvailable(false);
    }


    /**
     * Tenant bo'yicha barcha staff schedules ni olish
     **/
    @Override
    @Transactional(readOnly = true)
    public List<StaffScheduleResponse> getAllSchedulesByTenant() {
        Long tenantId =TenantContext.getTenantId();
        return scheduleRepository
                .findByStaffTenantId(tenantId)
                .stream()
                .map(StaffScheduleResponse::fromEntity)
                .toList();
    }

    /**
     * Staff ga service biriktirish
     *
     * @param staffId
     * @param serviceId
     */
    @Override
    @Transactional
    public StaffResponse assignServiceToStaff(Long staffId, Long serviceId) {

        Long tenantId = TenantContext.getTenantId();

        Staff staff = staffRepository
                .findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));

        var service = serviceRepository
                .findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi"));

        staff.addService(service);

        return StaffResponse.fromEntity(staff);
    }


    /**
     * Staff dan service olib tashlash
     *
     * @param staffId
     * @param serviceId
     */
    @Override
    @Transactional
    public StaffResponse removeServiceFromStaff(Long staffId, Long serviceId) {

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + staffId));

        Employement employement = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + serviceId));

        staff.getEmployements().remove(employement);
        employement.getStaff().remove(staff);
        staff = staffRepository.save(staff);

        return StaffResponse.fromEntity(staff);
    }

    /**
     * Staff ga bir nechta service biriktirish
     *
     * @param staffId
     * @param serviceIds
     */
    @Override
    @Transactional
    public StaffResponse assignServicesToStaff(Long staffId, List<Long> serviceIds) {

        Long tenantId = TenantContext.getTenantId();

        Staff staff = staffRepository
                .findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));

        List<Employement> employements =
                serviceRepository.findAllById(serviceIds);

        if (employements.size() != serviceIds.size()) {
            throw new NotFoundException("Ba'zi servicelar topilmadi");
        }

        employements.forEach(staff::addService);

        return StaffResponse.fromEntity(staff);
    }


    /**
     * Tenant bo'yicha staff statistikasi
     *
     */
    @Override
    @Transactional(readOnly = true)
    public StaffStatisticsResponse getStaffStatistics() {

        Long tenantId = TenantContext.getTenantId();

        long totalStaff = staffRepository.countByTenantId(tenantId);
        long activeStaff = staffRepository.countByTenantIdAndIsActive(tenantId, true);
        long inactiveStaff = totalStaff - activeStaff;

        long totalSchedules = scheduleRepository.countByStaffTenantId(tenantId);
        long availableSchedules =
                scheduleRepository.countByStaffTenantIdAndIsAvailableTrue(tenantId);

        return new StaffStatisticsResponse(
                tenantId,
                totalStaff,
                activeStaff,
                inactiveStaff,
                totalSchedules,
                availableSchedules
        );
    }
}
