package ermorg.erm.erm_command_organization.util;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import ermorg.erm.erm_command_organization.model.Branch;
import ermorg.erm.erm_command_organization.model.Department;
import ermorg.erm.erm_command_organization.util.DataScopeUtil.DataScope;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * DataScopeSpecification: Reusable JPA Specification for filtering by branch/department.
 * 
 * USAGE PATTERN:
 * // In repository method or service
 * List<Entity> results = repository.findAll(
 *     DataScopeSpecification.byBranches(userDataScope.allowedBranchIds)
 *     .and(DataScopeSpecification.byDepartments(userDataScope.allowedDepartmentIds))
 *     .and(otherFilters)
 * );
 * 
 * FIELDS REQUIRED IN TARGET ENTITY:
 * - branch_id (Long): Foreign key to branch
 * - department_id (Long): Foreign key to department
 * - branch (ManyToOne): Association to Branch entity
 * - department (ManyToOne): Association to Department entity
 */
public class DataScopeSpecification {

    /**
     * Create specification to filter by allowed branch IDs.
     * WHERE branch_id IN (:branchIds)
     */
    public static <T> Specification<T> byBranches(List<Long> branchIds) {
        return (root, query, cb) -> {
            if (branchIds == null || branchIds.isEmpty()) {
                // No branches allowed = deny all
                return cb.or();
            }
            return root.get("branch").get("id").in(branchIds);
        };
    }

    /**
     * Create specification to filter by allowed department IDs.
     * WHERE department_id IN (:departmentIds)
     */
    public static <T> Specification<T> byDepartments(List<Long> departmentIds) {
        return (root, query, cb) -> {
            if (departmentIds == null || departmentIds.isEmpty()) {
                // No departments allowed = deny all
                return cb.or();
            }
            return root.get("department").get("id").in(departmentIds);
        };
    }

    /**
     * Create specification for branch OR department access.
     * Useful when entity belongs to a branch OR department (not both required).
     * WHERE branch_id IN (...) OR department_id IN (...)
     */
    public static <T> Specification<T> byBranchesOrDepartments(
            List<Long> branchIds, List<Long> departmentIds) {
        return (root, query, cb) -> {
            Predicate branchPredicate = root.get("branch").get("id").in(branchIds);
            Predicate deptPredicate = root.get("department").get("id").in(departmentIds);
            return cb.or(branchPredicate, deptPredicate);
        };
    }

    /**
     * Create specification combining branch AND department filters.
     * WHERE branch_id IN (...) AND department_id IN (...)
     */
    public static <T> Specification<T> byBranchesAndDepartments(
            List<Long> branchIds, List<Long> departmentIds) {
        return (root, query, cb) -> {
            Predicate branchPredicate = root.get("branch").get("id").in(branchIds);
            Predicate deptPredicate = root.get("department").get("id").in(departmentIds);
            return cb.and(branchPredicate, deptPredicate);
        };
    }

    /**
     * Create specification from DataScope object.
     * Applies both branch and department filters using AND logic.
     */
    public static <T> Specification<T> fromDataScope(DataScope dataScope) {
        return (root, query, cb) -> {
            Predicate branchPredicate = root.get("branch").get("id")
                    .in(dataScope.allowedBranchIds);
            Predicate deptPredicate = root.get("department").get("id")
                    .in(dataScope.allowedDepartmentIds);
            return cb.and(branchPredicate, deptPredicate);
        };
    }

    /**
     * Create specification for a single branch.
     * WHERE branch_id = :branchId
     */
    public static <T> Specification<T> byBranch(Long branchId) {
        return (root, query, cb) -> root.get("branch").get("id").in(branchId);
    }

    /**
     * Create specification for a single department.
     * WHERE department_id = :departmentId
     */
    public static <T> Specification<T> byDepartment(Long departmentId) {
        return (root, query, cb) -> root.get("department").get("id").in(departmentId);
    }

    /**
     * Create specification for soft delete check (active records only).
     * WHERE deleted = false
     */
    public static <T> Specification<T> notDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    /**
     * Combining helper: Apply data scope + soft delete check
     */
    public static <T> Specification<T> scopedAndActive(DataScope dataScope) {
        return DataScopeSpecification.fromDataScope(dataScope)
                .and(notDeleted());
    }
}
