package org.architect.multitenantappointmentsystem.repository;

import org.architect.multitenantappointmentsystem.entity.BusinessType;
import org.architect.multitenantappointmentsystem.entity.Staff;
import org.architect.multitenantappointmentsystem.entity.StaffRole;
import org.architect.multitenantappointmentsystem.entity.Tenant;
import org.architect.multitenantappointmentsystem.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StaffRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StaffRepository staffRepository;

    @Test
    void findByTenantId_ShouldReturnStaffList(){
        Tenant tenant = new Tenant();
        tenant.setOrganizationName("Test Org");
        tenant.setBusinessType(BusinessType.BARBERSHOP);
        tenant.setEmail("tenant@example.com");
        tenant.setSlug("test-org");
        tenant.setPhone("+998901234567");
        tenant.setAddress("Test Address");
        tenant.setWorkingHoursStart(LocalTime.of(9, 0));
        tenant.setWorkingHoursEnd(LocalTime.of(18, 0));
        tenant.setSlotDuration(30);
        tenant.setAdvanceBookingDays(7);
        tenant.setTimezone("Asia/Tashkent");
        entityManager.persist(tenant);

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("+998901112233");
        entityManager.persist(user);

        Staff staff = new Staff();
        staff.setTenant(tenant);
        staff.setUser(user);
        staff.setIsActive(true);
        staff.setRole(StaffRole.MANAGER);
        staff.setDisplayName("John Doe");
        staff.setPosition("Manager");
        entityManager.persist(staff);

        entityManager.flush();
        List<Staff> foundStaff=staffRepository.findByTenantId(tenant.getId());

        assertThat(foundStaff).isNotEmpty();
        assertThat(foundStaff.getFirst().getTenant().getId()).isEqualTo(tenant.getId());
    }
}