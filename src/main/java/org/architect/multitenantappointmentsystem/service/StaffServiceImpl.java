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
import org.architect.multitenantappointmentsystem.service.interfaces.AuthService;
import org.architect.multitenantappointmentsystem.service.interfaces.StaffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public StaffResponse createStaff(UUID tenantId, CreateStaffRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException(
                        "Tenant topilmadi: " + tenantId));
        UUID currentUserId = AuthService.getCurrentUserId();
        Staff currentStaff = staffRepository
                .findByTenantIdAndUserId(tenantId, currentUserId).orElseThrow(
                        () -> new NotFoundException("Staff topilmadi"));
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
                createOrUpdateSchedule(tenantId, staff.getId(), scheduleRequest);
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
    public StaffResponse getStaffById(UUID tenantId, UUID id) {
        Staff staff = staffRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Bu " + id + " ga oid xodim topilmadi"));

        return StaffResponse.fromEntity(staff);
    }

    /**
     * Staff batafsil ma'lumotlarini olish (schedules va services bilan)
     */
    @Override
    @Transactional
    public StaffDetailResponse getStaffDetailById(UUID tenantId, UUID staffId) {

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
    public StaffResponse updateStaff(UUID tenantId, UUID id, UpdateStaffRequest request) {

        UUID currentUserId = AuthService.getCurrentUserId();
        Staff currentStaff = staffRepository.findByTenantIdAndUserId(tenantId, currentUserId)
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
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaffByTenant(UUID tenantId) {
        Staff currentStaff = currentStaffService.getCurrentStaff(tenantId);
        if (currentStaff.getRole() != StaffRole.OWNER && currentStaff.getRole() != StaffRole.MANAGER) {
            throw new AccessDeniedException("Ruxsat yo‘q");
        }

        return staffRepository.findByTenantId(tenantId)
                .stream()
                .map(StaffResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getActiveStaffByTenant(UUID tenantId) {
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
    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByTenantAndRole(UUID tenantId, StaffRole role) {
        // Role null check removed, handled by controller validation
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
    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByService(UUID tenantId, UUID serviceId) {
        List<Staff> staff = staffRepository.findActiveStaffByServiceIdAndTenantId(serviceId, tenantId);
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
    @Transactional(readOnly = true)
    public Page<StaffResponse> getStaffByTenantPaginated(UUID tenantId, Boolean activeOnly, Pageable pageable) {
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
            UUID tenantId,
            UUID staffId,
            CreateStaffScheduleRequest request) {

        UUID currentUserId = AuthService.getCurrentUserId();
        Staff currentStaff = staffRepository
                .findByTenantIdAndUserId(tenantId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("Staff topilmadi"));

        if (currentStaff.getRole() != StaffRole.OWNER &&
                currentStaff.getRole() != StaffRole.MANAGER) {
            throw new AccessDeniedException("Ruxsat yo‘q");
        }
        Staff staff = staffRepository
                .findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));

        LocalTime start = request.startTime();
        LocalTime end = request.endTime();

        if (end.isBefore(start)) {
            throw new BusinessException("EndTime startTime dan katta bo‘lishi kerak");
        }
        Optional<StaffSchedule> existingSchedule = scheduleRepository.findByStaffIdAndDayOfWeek(staffId,
                request.dayOfWeek());

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
    public void deleteStaff(UUID tenantId, UUID id) {

        Staff currentStaff = currentStaffService.getCurrentStaff(tenantId);

        if (currentStaff.getRole() != StaffRole.OWNER) {
            throw new AccessDeniedException("Faqat OWNER o‘chira oladi");
        }

        Staff staff = staffRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + id));

        staff.setIsActive(false);
    }

    @Override
    @Transactional
    public StaffResponse activateStaff(UUID tenantId, UUID id) {

        Staff staff = staffRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + id));

        staff.setIsActive(true);

        return StaffResponse.fromEntity(staff);
    }

    @Override
    @Transactional
    public StaffResponse deactivateStaff(UUID tenantId, UUID id) {

        Staff staff = staffRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + id));

        staff.setIsActive(false);

        return StaffResponse.fromEntity(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffScheduleResponse> getStaffSchedules(UUID tenantId, UUID staffId) {

        Staff staff = staffRepository
                .findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));

        return scheduleRepository.findByStaffId(staff.getId())
                .stream()
                .map(StaffScheduleResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffScheduleResponse getStaffScheduleByDay(
            UUID tenantId,
            UUID staffId,
            Integer dayOfWeek) {

        StaffSchedule schedule = scheduleRepository
                .findByStaffIdAndDayOfWeekAndStaffTenantId(
                        staffId,
                        dayOfWeek,
                        tenantId)
                .orElseThrow(() -> new NotFoundException(
                        "Schedule topilmadi: staff=" + staffId + ", day=" + dayOfWeek));

        return StaffScheduleResponse.fromEntity(schedule);
    }

    @Override
    @Transactional
    public StaffScheduleResponse updateSchedule(UUID tenantId, UUID staffId,
            Integer dayOfWeek,
            UpdateStaffScheduleRequest request) {

        StaffSchedule schedule = scheduleRepository
                .findByStaffIdAndDayOfWeekAndStaff_TenantId(
                        staffId,
                        dayOfWeek,
                        tenantId)
                .orElseThrow(() -> new NotFoundException("Schedule topilmadi yoki sizga tegishli emas"));

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
    public void deleteSchedule(UUID tenantId, UUID staffId, Integer dayOfWeek) {
        StaffSchedule schedule = scheduleRepository
                .findByStaffIdAndDayOfWeekAndStaffTenantId(
                        staffId,
                        dayOfWeek,
                        tenantId)
                .orElseThrow(() -> new NotFoundException("Schedule topilmadi"));

        schedule.setIsAvailable(false);
    }

    /**
     * Tenant bo'yicha barcha staff schedules ni olish
     **/
    @Override
    @Transactional(readOnly = true)
    public List<StaffScheduleResponse> getAllSchedulesByTenant(UUID tenantId) {
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
    public StaffResponse assignServiceToStaff(UUID tenantId, UUID staffId, UUID serviceId) {

        Staff staff = staffRepository
                .findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));

        var service = serviceRepository
                .findByIdAndTenantId(serviceId, tenantId)
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
    public StaffResponse removeServiceFromStaff(UUID tenantId, UUID staffId, UUID serviceId) {

        Staff staff = staffRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + staffId));

        Employement employement = serviceRepository.findByIdAndTenantId(serviceId, tenantId)
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
    public StaffResponse assignServicesToStaff(UUID tenantId, UUID staffId, List<UUID> serviceIds) {

        Staff staff = staffRepository
                .findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));

        List<Employement> employements = serviceRepository.findByIdInAndTenantId(serviceIds, tenantId);

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
    public StaffStatisticsResponse getStaffStatistics(UUID tenantId) {

        long totalStaff = staffRepository.countByTenantId(tenantId);
        long activeStaff = staffRepository.countByTenantIdAndIsActive(tenantId, true);
        long inactiveStaff = totalStaff - activeStaff;

        long totalSchedules = scheduleRepository.countByStaffTenantId(tenantId);
        long availableSchedules = scheduleRepository.countByStaffTenantIdAndIsAvailableTrue(tenantId);

        return new StaffStatisticsResponse(
                tenantId,
                totalStaff,
                activeStaff,
                inactiveStaff,
                totalSchedules,
                availableSchedules);
    }
}
