package org.architect.multitenantappointmentsystem.service;

import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.entity.Staff;
import org.architect.multitenantappointmentsystem.entity.StaffRole;
import org.architect.multitenantappointmentsystem.exception.AccessDeniedException;
import org.architect.multitenantappointmentsystem.exception.NotFoundException;
import org.architect.multitenantappointmentsystem.repository.StaffRepository;
import org.architect.multitenantappointmentsystem.security.TenantContext;
import org.architect.multitenantappointmentsystem.service.interfaces.AuthService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentStaffService {

    private final StaffRepository staffRepository;

    public Staff getCurrentStaff() {
        Long currentUserId = AuthService.getCurrentUserId();
        Long tenantId = TenantContext.getTenantId();

        return (Staff) staffRepository
                .findByTenantIdAndUserId(tenantId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));
    }

    public void requireOwnerOrManager() {
        Staff staff = getCurrentStaff();

        if (staff.getRole() != StaffRole.OWNER &&
            staff.getRole() != StaffRole.MANAGER) {
            throw new AccessDeniedException("Ruxsat yo‘q");
        }
    }
}
