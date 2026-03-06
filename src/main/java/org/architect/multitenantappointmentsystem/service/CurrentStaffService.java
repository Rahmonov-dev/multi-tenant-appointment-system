package org.architect.multitenantappointmentsystem.service;

import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.entity.Staff;
import org.architect.multitenantappointmentsystem.entity.StaffRole;
import org.architect.multitenantappointmentsystem.exception.AccessDeniedException;
import org.architect.multitenantappointmentsystem.exception.NotFoundException;
import org.architect.multitenantappointmentsystem.repository.StaffRepository;
import org.architect.multitenantappointmentsystem.service.interfaces.AuthService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentStaffService {

    private final StaffRepository staffRepository;

    public Staff getCurrentStaff(UUID tenantId) {
        UUID currentUserId = AuthService.getCurrentUserId();

        return staffRepository
                .findByTenantIdAndUserId(tenantId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Staff topilmadi"));
    }

    public void requireOwnerOrManager(UUID tenantId) {
        Staff staff = getCurrentStaff(tenantId);

        if (staff.getRole() != StaffRole.OWNER &&
            staff.getRole() != StaffRole.MANAGER) {
            throw new AccessDeniedException("Ruxsat yo‘q");
        }
    }

    public Staff requireActiveStaff(UUID tenantId) {
        Staff staff = getCurrentStaff(tenantId);

        if (staff.getIsActive() == null || !staff.getIsActive()) {
            throw new AccessDeniedException("Xodim aktiv emas");
        }

        return staff;
    }

    public void requireStaffRole(UUID tenantId) {
        Staff staff = getCurrentStaff(tenantId);

        if (staff.getRole() != StaffRole.STAFF&& staff.getRole() != StaffRole.OWNER &&
                staff.getRole() != StaffRole.MANAGER) {
            throw new AccessDeniedException("Ruxsat yo‘q");
        }
    }
}
