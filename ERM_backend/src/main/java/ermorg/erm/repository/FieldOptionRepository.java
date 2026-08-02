package ermorg.erm.repository;

import ermorg.erm.model.FieldOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldOptionRepository extends JpaRepository<FieldOption, Long> {

    /**
     * All active options for a system field identified by its property name
     * and parent table name. Used by the field-options API endpoint.
     */
    @Query("""
            SELECT fo FROM FieldOption fo
            JOIN fo.systemField sf
            JOIN sf.systemTable st
            WHERE sf.field    = :fieldName
              AND st.tableName = :tableName
              AND fo.deleted   = false
              AND fo.isActive  = true
            ORDER BY fo.displayOrder ASC NULLS LAST, fo.optionLabel ASC
            """)
    List<FieldOption> findActiveByFieldNameAndTable(
            @Param("fieldName")  String fieldName,
            @Param("tableName")  String tableName);

    /**
     * All active options for a system field by its ID.
     * Used when building SystemTableResponse with options.
     */
    @Query("""
            SELECT fo FROM FieldOption fo
            WHERE fo.systemField.id = :systemFieldId
              AND fo.deleted  = false
              AND fo.isActive = true
            ORDER BY fo.displayOrder ASC NULLS LAST, fo.optionLabel ASC
            """)
    List<FieldOption> findActiveBySystemFieldId(@Param("systemFieldId") Long systemFieldId);

    /**
     * Resolve the display label for a stored value.
     * Used by the generic value resolver to convert "RS" → "Rs." at read time.
     */
    @Query("""
            SELECT fo.optionLabel FROM FieldOption fo
            JOIN fo.systemField sf
            WHERE sf.field       = :fieldName
              AND fo.optionValue  = :optionValue
              AND fo.deleted      = false
              AND fo.isActive     = true
            """)
    String findLabelByFieldNameAndValue(
            @Param("fieldName")   String fieldName,
            @Param("optionValue") String optionValue);
}
