package ermorg.erm.repository;
import ermorg.erm.dashboard.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.*;
import ermorg.erm.model.*;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.util.*;

class ChartScopeTest {
    private final UserRepository users=mock(UserRepository.class);
    private final ChartScope.Resolver resolver=new ChartScope.Resolver(users);
    private final User user=new User();
    private final Organization org=new Organization();
    @BeforeEach void setup() {
        org.setId(10L);user.setId(30L);user.setOrganization(org);
        Company company=new Company();company.setId(20L);user.setCompany(company);
        OrganizationContext.setOrganization(org);UserContext.seetUser(user);
        when(users.findActiveByIdWithRolesAndCompany(30L)).thenReturn(Optional.of(user));
    }
    @AfterEach void clear() {OrganizationContext.clear();UserContext.clear();}
    void role(String code) {
        RoleType type=new RoleType();type.setCode(code);
        Role role=new Role();role.setRoleType(type);user.setRoles(List.of(role));
    }
    @Test void companyCannotEscapeAssignedCompany() throws Exception {
        role("COMPANY_ADMIN");
        assertThat(resolver.resolve(null,null,null).companyId()).isEqualTo(20L);
        assertThatThrownBy(()->resolver.resolve(21L,null,null)).hasMessageContaining("outside user scope");
    }
    @Test void organizationAdminStillNeedsMatchingOrganization() throws Exception {
        role("ORG_ADMIN");assertThat(resolver.resolve(null,null,null).companyId()).isNull();
        Organization other=new Organization();other.setId(11L);user.setOrganization(other);
        assertThatThrownBy(()->resolver.resolve(null,null,null)).hasMessageContaining("outside the organization");
    }
    @Test void basicUserIsCreatorRestricted() throws Exception {
        role("BASIC_USER");assertThat(resolver.resolve(null,null,null).creatorId()).isEqualTo(30L);
    }
    @Test void advancedWithoutAssignmentsFailsClosed() throws Exception {
        role("ADVANCED_USER");var scope=resolver.resolve(null,null,null);
        var repository=new DashboardChartRepository(null);
        assertThat(repository.filter(scope,"org","company","creator","branch","dept").sql()).contains("AND (1=0)");
    }
    @Test void filtersAreIntersectedWithAdvancedScope() throws Exception {
        role("ADVANCED_USER");
        Branch branch=new Branch();branch.setId(40L);user.setBranch(branch);
        Department department=new Department();department.setId(50L);user.setDepartment(department);
        var filter=new DashboardChartRepository(null).filter(resolver.resolve(20L,41L,51L),"org","company","creator","branch","dept");
        assertThat(filter.sql()).contains("org = :org","company = :company","branch = :branch","dept = :department",
            "AND (branch = :assignedBranch OR dept = :assignedDepartment)");
        assertThat(filter.params().getValue("branch")).isEqualTo(41L);
    }
    @Test void missingRoleOrInvalidFilterRejected() {
        assertThatThrownBy(()->resolver.resolve(null,null,null)).isInstanceOf(IllegalArgumentException.class);
        role("ORG_ADMIN");
        assertThatThrownBy(()->resolver.resolve(null,-1L,null)).isInstanceOf(IllegalArgumentException.class);
        UserContext.clear();assertThatThrownBy(()->resolver.resolve(null,null,null)).hasMessageContaining("Authenticated");
    }
}
