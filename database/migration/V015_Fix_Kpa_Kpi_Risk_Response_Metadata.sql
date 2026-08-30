-- Normalize remaining ERM dynamic-field metadata used by KPA/KPI, KRI/KPI,
-- Risk Review, and Risk Response & Treatment screens.
-- Safe to re-run.

UPDATE fields f
JOIN category c ON c.id = f.category_id
SET f.mapped_with = 'businessFunctionalOwner',
    f.updated_at = NOW(6)
WHERE c.mapped_with_table = 'kpaKpiReview'
  AND c.deleted = b'0'
  AND f.deleted = b'0'
  AND REGEXP_REPLACE(LOWER(f.field_name), '[^a-z0-9]', '') IN
      ('functionalowner', 'businessfunctionalowner');

UPDATE fields f
JOIN category c ON c.id = f.category_id
SET f.mapped_with = 'evaluationBy',
    f.updated_at = NOW(6)
WHERE c.mapped_with_table = 'kpaKpiReview'
  AND c.deleted = b'0'
  AND f.deleted = b'0'
  AND REGEXP_REPLACE(LOWER(f.field_name), '[^a-z0-9]', '') IN
      ('evaluationby', 'evaluationbyno', 'kpievaluationby');

UPDATE fields f
JOIN category c ON c.id = f.category_id
SET f.mapped_with = 'departmentName',
    f.updated_at = NOW(6)
WHERE c.mapped_with_table = 'kpaKpiReview'
  AND c.deleted = b'0'
  AND f.deleted = b'0'
  AND REGEXP_REPLACE(LOWER(f.field_name), '[^a-z0-9]', '') IN
      ('department', 'departmentname', 'departmentfunction');

UPDATE fields f
JOIN category c ON c.id = f.category_id
SET f.mapped_with = 'unitOfMeasurement',
    f.updated_at = NOW(6)
WHERE c.mapped_with_table IN ('kpaKpiReview', 'kriKpiReview')
  AND c.deleted = b'0'
  AND f.deleted = b'0'
  AND REGEXP_REPLACE(LOWER(f.field_name), '[^a-z0-9]', '') IN
      ('unitofmeasurement', 'measurementunit', 'levelofmeasurement');

UPDATE fields f
JOIN category c ON c.id = f.category_id
SET f.mapped_with = 'residualRiskRating',
    f.updated_at = NOW(6)
WHERE c.mapped_with_table = 'riskReview'
  AND c.deleted = b'0'
  AND f.deleted = b'0'
  AND REGEXP_REPLACE(LOWER(f.field_name), '[^a-z0-9]', '') IN
      ('residualriskrating', 'residualriskratingcriteria');

INSERT INTO system_fields (id, client_ip, created_at, deleted, updated_at, field,
                           display_label, field_type, is_required, is_read_only,
                           is_hidden, display_order, created_by_id, updated_by_id,
                           system_table_id)
SELECT (SELECT COALESCE(MAX(sf2.id), 0) + 1 FROM system_fields sf2),
       NULL, NOW(6), b'0', NOW(6), 'supportingEvidenceDocument',
       'Supporting Evidence Upload', 'FILE_UPLOAD', b'0', b'0',
       b'0', NULL, NULL, NULL, st.id
FROM system_tables st
WHERE st.table_name = 'riskTreatment'
  AND NOT EXISTS (
      SELECT 1
      FROM system_fields sf
      WHERE sf.system_table_id = st.id
        AND sf.field = 'supportingEvidenceDocument'
        AND sf.deleted = b'0'
  );

INSERT INTO fields (id, client_ip, created_at, deleted, updated_at, field_name,
                    field_type, mapped_with, required, tab_name, created_by_id,
                    updated_by_id, category_id, `system-field-id`, module_id,
                    disabled, field_behavior, field_order, show_grid_column,
                    show_in_view)
SELECT
    (SELECT COALESCE(MAX(f2.id), 0) FROM fields f2) + (@row := @row + 1),
    NULL, NOW(6), b'0', NOW(6), 'Supporting Evidence Upload',
    'File Upload', 'supportingEvidenceDocument', b'0', NULL, NULL,
    NULL, c.id, sf.id, NULL, b'0', NULL,
    COALESCE((SELECT MAX(f3.field_order) FROM fields f3 WHERE f3.category_id = c.id), 0) + 1,
    b'1', b'1'
FROM (SELECT @row := 0) init,
     category c
JOIN system_tables st ON st.table_name = c.mapped_with_table
JOIN system_fields sf ON sf.system_table_id = st.id
                     AND sf.field = 'supportingEvidenceDocument'
WHERE c.mapped_with_table = 'riskTreatment'
  AND c.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1
      FROM fields f
      WHERE f.category_id = c.id
        AND f.`system-field-id` = sf.id
        AND COALESCE(f.deleted, b'0') = b'0'
  );
