package org.architect.multitenantappointmentsystem.service;

import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.request.CreateServiceRequest;
import org.architect.multitenantappointmentsystem.dto.request.UpdateServiceRequest;
import org.architect.multitenantappointmentsystem.dto.response.ServiceDetailResponse;
import org.architect.multitenantappointmentsystem.dto.response.ServiceResponse;
import org.architect.multitenantappointmentsystem.dto.response.ServiceStatisticsResponse;
import org.architect.multitenantappointmentsystem.entity.Employement;
import org.architect.multitenantappointmentsystem.entity.Staff;
import org.architect.multitenantappointmentsystem.entity.StaffRole;
import org.architect.multitenantappointmentsystem.entity.Tenant;
import org.architect.multitenantappointmentsystem.exception.AccessDeniedException;
import org.architect.multitenantappointmentsystem.exception.BusinessException;
import org.architect.multitenantappointmentsystem.exception.NotFoundException;
import org.architect.multitenantappointmentsystem.repository.ServiceRepository;
import org.architect.multitenantappointmentsystem.repository.StaffRepository;
import org.architect.multitenantappointmentsystem.repository.TenantRepository;
import org.architect.multitenantappointmentsystem.service.interfaces.ServiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {
    private final ServiceRepository serviceRepository;
    private final TenantRepository tenantRepository;
    private final StaffRepository staffRepository;
    private final CurrentStaffService currentStaffService;
    /**
     * Employement yaratish
     *
     * @param request
     */
    @Override
    public ServiceResponse createService(UUID tenantId, CreateServiceRequest request) {
        currentStaffService.requireOwnerOrManager(tenantId);
        if (tenantId == null) {
            throw new BusinessException("Tenant ID talab qilinadi");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant topilmadi: " + tenantId));

        if (serviceRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new BusinessException("Bu nom bilan employement mavjud");
        }

        Employement employement = new Employement();
        employement.setTenant(tenant);
        employement.setName(request.name());
        employement.setDescription(request.description());
        employement.setDuration(request.duration());
        employement.setPrice(request.price());
        employement.setImageUrl(request.imageUrl());
        employement.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        employement.setIsActive(true);

        employement = serviceRepository.save(employement);

        return ServiceResponse.fromEntity(employement);
    }

    /**
     * Employement ma'lumotlarini olish (ID bo'yicha)
     *
     * @param id
     */
    @Override
    public ServiceResponse getServiceById(UUID tenantId, UUID id) {
        if (tenantId == null) {
            throw new BusinessException("Tenant ID talab qilinadi");
        }
        if (id == null) {
            throw new BusinessException("Service ID talab qilinadi");
        }

        Employement employement = serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + id));

        return ServiceResponse.fromEntity(employement);
    }

    /**
     * Employement batafsil ma'lumotlarini olish (staff bilan)
     *
     * @param id
     */
    @Override
    public ServiceDetailResponse getServiceDetailById(UUID tenantId, UUID id) {
        if (tenantId == null) {
            throw new BusinessException("Tenant ID talab qilinadi");
        }
        if (id == null) {
            throw new BusinessException("Service ID talab qilinadi");
        }

        Employement employement = serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + id));

        return ServiceDetailResponse.fromEntity(employement);
    }

    /**
     * Employement ma'lumotlarini yangilash
     *
     * @param id
     * @param request
     */
    @Override
    public ServiceResponse updateService(UUID tenantId, UUID id, UpdateServiceRequest request) {
        currentStaffService.requireOwnerOrManager(tenantId);

        Employement employement = serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + id));

        if (request.name() != null) {
            if (serviceRepository.existsByNameAndTenantIdAndIdNot(request.name(), employement.getTenant().getId(), id)) {
                throw new BusinessException("Bu nom bilan employement mavjud");
            }
            employement.setName(request.name());
        }
        if (request.description() != null) {
            employement.setDescription(request.description());
        }
        if (request.duration() != null) {
            employement.setDuration(request.duration());
        }
        if (request.price() != null) {
            employement.setPrice(request.price());
        }
        if (request.imageUrl() != null) {
            employement.setImageUrl(request.imageUrl());
        }
        if (request.isActive() != null) {
            employement.setIsActive(request.isActive());
        }
        if (request.displayOrder() != null) {
            employement.setDisplayOrder(request.displayOrder());
        }

        employement = serviceRepository.save(employement);

        return ServiceResponse.fromEntity(employement);
    }

    /**
     * Employement o'chirish (soft delete)
     *
     * @param id
     */
    @Override
    public void deleteService(UUID tenantId, UUID id) {
        currentStaffService.requireOwnerOrManager(tenantId);

        Employement employement = serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + id));
        employement.setIsActive(false);
        serviceRepository.save(employement);
    }

    /**
     * Employement aktivlashtirish
     *
     * @param id
     */
    @Override
    public ServiceResponse activateService(UUID tenantId, UUID id) {
        currentStaffService.requireOwnerOrManager(tenantId);

        Employement employement = serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + id));
        employement.setIsActive(true);
        employement = serviceRepository.save(employement);

        return ServiceResponse.fromEntity(employement);
    }

    /**
     * Employement deaktivlashtirish
     *
     * @param id
     */
    @Override
    public ServiceResponse deactivateService(UUID tenantId, UUID id) {
        currentStaffService.requireOwnerOrManager(tenantId);

        Employement employement = serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + id));
        employement.setIsActive(false);
        employement = serviceRepository.save(employement);

        return ServiceResponse.fromEntity(employement);
    }

    /**
     * Tenant bo'yicha barcha servicelarni olish
     */
    @Override
    public List<ServiceResponse> getAllServicesByTenant(UUID tenantId) {
        return serviceRepository.findByTenantId(tenantId)
                .stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    /**
     * Tenant bo'yicha aktiv servicelarni olish
     */
    @Override
    public List<ServiceResponse> getActiveServicesByTenant(UUID tenantId) {
        return serviceRepository.findByTenantIdAndIsActive(tenantId, true)
                .stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    /**
     * Tenant bo'yicha servicelarni tartiblangan holda olish
     * @param activeOnly
     */
    @Override
    public List<ServiceResponse> getServicesByTenantOrdered(UUID tenantId, Boolean activeOnly) {
        List<Employement> employements = activeOnly != null && activeOnly
                ? serviceRepository.findByTenantIdAndIsActiveOrderByDisplayOrder(tenantId, true)
                : serviceRepository.findByTenantIdOrderByDisplayOrder(tenantId);

        return employements.stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    /**
     * Tenant bo'yicha servicelarni pagination bilan olish
     * @param activeOnly
     * @param pageable
     */
    @Override
    public Page<ServiceResponse> getServicesByTenantPaginated(UUID tenantId, Boolean activeOnly, Pageable pageable) {

        Page<Employement> servicePage = activeOnly != null && activeOnly
                ? serviceRepository.findByTenantIdAndIsActive(tenantId, true, pageable)
                : serviceRepository.findByTenantId(tenantId, pageable);

        return servicePage.map(ServiceResponse::fromEntity);
    }

    /**
     * Employement qidirish (ism yoki tavsif bo'yicha
     * @param keyword
     * @param activeOnly
     */
    @Override
    public List<ServiceResponse> searchServices(UUID tenantId, String keyword, Boolean activeOnly) {
        List<Employement> employements = activeOnly != null && activeOnly
                ? serviceRepository.searchActiveByKeyword(tenantId, keyword)
                : serviceRepository.searchByKeyword(tenantId, keyword);

        return employements.stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    /**
     * Narx oralig'i bo'yicha servicelarni olish
     * @param minPrice
     * @param maxPrice
     */
    @Override
    public List<ServiceResponse> getServicesByPriceRange(UUID tenantId, BigDecimal minPrice, BigDecimal maxPrice) {
        return serviceRepository.findByPriceRange(tenantId, minPrice, maxPrice)
                .stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    /**
     * Maksimum davomiylik bo'yicha servicelarni olish
     *
     * @param maxDuration
     */
    @Override
    public List<ServiceResponse> getServicesByMaxDuration(UUID tenantId, Integer maxDuration) {
        return serviceRepository.findByMaxDuration(tenantId, maxDuration)
                .stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    /**
     * Staff bo'yicha servicelarni olish
     *
     * @param staffId
     */
    @Override
    public List<ServiceResponse> getServicesByStaff(UUID tenantId, UUID staffId) {
        Staff staff = staffRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + staffId));

        return serviceRepository.findActiveServicesByStaffId(staffId)
                .stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    /**
     * Mashhur servicelar (ko'p band qilingan)
     * @param limit
     */
    @Override
    public List<ServiceResponse> getPopularServices(UUID tenantId, Integer limit) {
        int size = limit != null && limit > 0 ? limit : 10;
        return serviceRepository.findPopularServices(tenantId, PageRequest.of(0, size))
                .stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    /**
     * Employement ga staff biriktirish
     *
     * @param serviceId
     * @param staffId
     */
    @Override
    public ServiceResponse assignStaffToService(UUID tenantId, UUID serviceId, UUID staffId) {
        currentStaffService.requireOwnerOrManager(tenantId);

        Employement employement = serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + serviceId));
        Staff staff = staffRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + staffId));

        staff.addService(employement);
        staffRepository.save(staff);

        return ServiceResponse.fromEntity(employement);
    }

    @Override
    public ServiceResponse removeStaffFromService(UUID tenantId, UUID serviceId, UUID staffId) {
        currentStaffService.requireOwnerOrManager(tenantId);

        Employement employement = serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + serviceId));
        Staff staff = staffRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi: " + staffId));

        staff.getEmployements().remove(employement);
        employement.getStaff().remove(staff);
        staffRepository.save(staff);

        return ServiceResponse.fromEntity(employement);
    }

    @Override
    public ServiceResponse assignStaffsToService(UUID tenantId, UUID serviceId, List<UUID> staffIds) {
        currentStaffService.requireOwnerOrManager(tenantId);

        Employement employement = serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + serviceId));

        List<Staff> staffList = staffRepository.findAllById(staffIds);
        if (staffList.size() != staffIds.size()) {
            throw new NotFoundException("Ba'zi staff topilmadi");
        }

        boolean allSameTenant = staffList.stream()
                .allMatch(staff -> staff.getTenant().getId().equals(tenantId));
        if (!allSameTenant) {
            throw new BusinessException("Ba'zi staff boshqa tenant ga tegishli");
        }

        staffList.forEach(staff -> staff.addService(employement));
        staffRepository.saveAll(staffList);

        return ServiceResponse.fromEntity(employement);
    }

    @Override
    public ServiceResponse removeAllStaffFromService(UUID tenantId, UUID serviceId) {
        currentStaffService.requireOwnerOrManager(tenantId);

        Employement employement = serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + serviceId));

        List<Staff> staffList = employement.getStaff();
        if (staffList != null && !staffList.isEmpty()) {
            staffList.forEach(staff -> staff.getEmployements().remove(employement));
            staffRepository.saveAll(staffList);
            employement.getStaff().clear();
        }

        return ServiceResponse.fromEntity(employement);
    }

    @Override
    public ServiceResponse updateDisplayOrder(UUID tenantId, UUID id, Integer displayOrder) {
        currentStaffService.requireOwnerOrManager(tenantId);

        Employement employement = serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Employement topilmadi: " + id));
        employement.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        employement = serviceRepository.save(employement);

        return ServiceResponse.fromEntity(employement);
    }

    @Override
    public void updateMultipleDisplayOrders(UUID tenantId, List<UUID> serviceIds) {
        currentStaffService.requireOwnerOrManager(tenantId);
        if (serviceIds == null || serviceIds.isEmpty()) {
            return;
        }

        List<Employement> employements = serviceRepository.findByIdInAndTenantId(serviceIds, tenantId);

        if (employements.size() != serviceIds.size()) {
            throw new BusinessException("Ba'zi servicelar topilmadi yoki boshqa tenant ga tegishli");
        }

        for (int i = 0; i < serviceIds.size(); i++) {
            UUID serviceId = serviceIds.get(i);
            int finalI = i;
            employements.stream()
                    .filter(service -> service.getId().equals(serviceId))
                    .findFirst()
                    .ifPresent(service -> service.setDisplayOrder(finalI));
        }
        serviceRepository.saveAll(employements);
    }

    /**
     * Tenant bo'yicha service statistikasi
     *
     */
    @Override
    public ServiceStatisticsResponse getServiceStatistics(UUID tenantId) {
        List<Employement> employements = serviceRepository.findByTenantId(tenantId);
        long totalServices = employements.size();
        long activeServices = employements.stream().filter(Employement::getIsActive).count();
        long inactiveServices = totalServices - activeServices;

        BigDecimal totalPrice = employements.stream()
                .map(Employement::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averagePrice = totalServices > 0
                ? totalPrice.divide(BigDecimal.valueOf(totalServices), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal minPrice = employements.stream()
                .map(Employement::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = employements.stream()
                .map(Employement::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        int totalDuration = employements.stream()
                .mapToInt(Employement::getDuration)
                .sum();
        int averageDuration = totalServices > 0 ? (int) Math.round((double) totalDuration / totalServices) : 0;
        int minDuration = employements.stream()
                .mapToInt(Employement::getDuration)
                .min()
                .orElse(0);
        int maxDuration = employements.stream()
                .mapToInt(Employement::getDuration)
                .max()
                .orElse(0);

        long totalAppointments = employements.stream()
                .mapToLong(service -> service.getAppointments() != null ? service.getAppointments().size() : 0)
                .sum();

        return new ServiceStatisticsResponse(
                tenantId,
                totalServices,
                activeServices,
                inactiveServices,
                averagePrice,
                minPrice,
                maxPrice,
                averageDuration,
                minDuration,
                maxDuration,
                totalAppointments
        );
    }
}
