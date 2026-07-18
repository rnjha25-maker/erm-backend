package ermorg.erm.serviceimpl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.ErmMaturityResponse;
import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import ermorg.erm.dto.response.RiskAssessmentResponse;
import ermorg.erm.dto.response.RiskControlResponse;
import ermorg.erm.dto.response.RiskRegisterPage;
import ermorg.erm.dto.response.RiskRegisterRow;
import ermorg.erm.dto.response.RiskResponse;
import ermorg.erm.dto.response.RiskResponseTreatmentResponse;
import ermorg.erm.dto.response.RiskReviewResponseDtoResponse;
import ermorg.erm.model.ERMMaturityAssessment;
import ermorg.erm.model.KpaKpiReview;
import ermorg.erm.model.KriKpiReview;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskAssessment;
import ermorg.erm.model.RiskControl;
import ermorg.erm.model.RiskResponseTreatment;
import ermorg.erm.model.RiskReview;
import ermorg.erm.repository.ErmMaturityRepository;
import ermorg.erm.repository.KpaKpiReviewRepository;
import ermorg.erm.repository.KriKpiRiskRepository;
import ermorg.erm.repository.RiskAsessmentRepository;
import ermorg.erm.repository.RiskControlRepository;
import ermorg.erm.repository.RiskResponseTreatmentRepository;
import ermorg.erm.repository.RiskReviewRepository;
import ermorg.erm.service.IFieldService;
import ermorg.erm.util.mapper.CustomResponseMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskRegisterService {

	private final RiskAsessmentRepository riskAssessmentRepository;
	private final RiskControlRepository riskControlRepository;
	private final RiskResponseTreatmentRepository riskResponseTreatmentRepository;
	private final RiskReviewRepository riskReviewRepository;
	private final KriKpiRiskRepository kriKpiRiskRepository;
	private final ErmMaturityRepository ermMaturityRepository;
	private final KpaKpiReviewRepository kpaKpiReviewRepository;
	private final KpaKpiReviewService kpaKpiReviewService;
	private final CustomResponseMapper customResponseMapper;
	private final IFieldService fieldService;

	@Transactional(readOnly = true)
	public RiskRegisterPage buildPage(Long organizationId, List<Risk> risks, Date startDate, Date endDate,
			Long functionId, boolean scopeByDepartment, List<Long> scopeDepartmentIds, int page, int size) {

		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 500);
		RegisterData data = loadData(organizationId, risks, startDate, endDate, functionId, scopeByDepartment,
				scopeDepartmentIds);
		long total = countRows(data);
		long from = Math.min((long) safePage * safeSize, total);
		long to = Math.min(from + safeSize, total);
		List<RiskRegisterRow> content = new ArrayList<>((int) (to - from));
		forEachRow(data, from, to, content::add);
		return RiskRegisterPage.of(content, safePage, safeSize, total);
	}

	@Transactional(readOnly = true)
	public byte[] exportCsv(Long organizationId, List<Risk> risks, Date startDate, Date endDate, Long functionId,
			boolean scopeByDepartment, List<Long> scopeDepartmentIds) {

		RegisterData data = loadData(organizationId, risks, startDate, endDate, functionId, scopeByDepartment,
				scopeDepartmentIds);
		List<String> headers = csvHeaders();
		StringBuilder csv = new StringBuilder();
		appendCsvLine(csv, headers);
		forEachRow(data, 0, Long.MAX_VALUE, row -> {
			Map<String, String> flat = flatten(row);
			appendCsvLine(csv, headers.stream().map(flat::get).toList());
		});
		return csv.toString().getBytes(StandardCharsets.UTF_8);
	}

	private List<String> csvHeaders() {
		List<String> headers = new ArrayList<>();
		addConfiguredHeaders(headers, "risk", "risk");
		addConfiguredHeaders(headers, "riskAssessment", "riskAssessment");
		addConfiguredHeaders(headers, "riskControl", "riskControl");
		addConfiguredHeaders(headers, "riskResponse", "riskTreatment");
		addConfiguredHeaders(headers, "riskReview", "riskReview");
		addConfiguredHeaders(headers, "kri", "kriKpiReview");
		addConfiguredHeaders(headers, "ermMaturity", "ermMaturity");
		addConfiguredHeaders(headers, "kpaKpiReview", "kpaKpiReview");
		return headers;
	}

	private void addConfiguredHeaders(List<String> headers, String prefix, String tableName) {
		try {
			fieldService.getCustomFieldResponse(1L, tableName).stream()
					.filter(field -> Boolean.TRUE.equals(field.getShowGridColumn()))
					.map(CustomFieldResponse::getFieldName)
					.map(fieldName -> prefix + "." + fieldName)
					.forEach(headers::add);
		} catch (ermorg.erm.exception.ResourceNotFoundException ignored) {
			// A module without configured grid fields contributes no CSV columns.
		}
	}

	private RegisterData loadData(Long organizationId, List<Risk> risks, Date startDate, Date endDate,
			Long functionId, boolean scopeByDepartment, List<Long> scopeDepartmentIds) {

		if (risks.isEmpty()) {
			return RegisterData.empty();
		}
		List<Long> riskIds = risks.stream().map(Risk::getId).toList();
		List<Long> companyIds = risks.stream().map(Risk::getCompanyId).filter(id -> id != null)
				.distinct().toList();

		List<RiskAssessment> assessments = riskAssessmentRepository.findForRiskRegister(organizationId, riskIds,
				startDate, endDate);
		List<RiskControl> controls = riskControlRepository.findForRiskRegister(organizationId, riskIds, startDate,
				endDate);
		List<RiskResponseTreatment> responses = riskResponseTreatmentRepository.findForRiskRegister(organizationId,
				riskIds, startDate, endDate);
		List<RiskReview> reviews = riskReviewRepository.findForRiskRegister(organizationId, riskIds, startDate,
				endDate);
		List<KriKpiReview> kris = kriKpiRiskRepository.findForRiskRegister(organizationId, riskIds, startDate, endDate);

		List<ERMMaturityAssessment> maturity = companyIds.isEmpty() ? Collections.emptyList()
				: ermMaturityRepository.findForRiskRegister(organizationId, companyIds, startDate, endDate, functionId);
		if (scopeByDepartment) {
			Set<Long> allowed = Set.copyOf(scopeDepartmentIds);
			maturity = maturity.stream().filter(item -> item.getDepartmentIds() == null
					|| item.getDepartmentIds().isEmpty()
					|| item.getDepartmentIds().stream().anyMatch(allowed::contains)).toList();
		}
		List<KpaKpiReview> kpaKpi = companyIds.isEmpty() ? Collections.emptyList()
				: kpaKpiReviewRepository.findForRiskRegister(organizationId, companyIds, startDate, endDate);

		return new RegisterData(risks, groupByRisk(assessments), groupByRisk(controls), groupByRisk(responses),
				groupByRisk(reviews), groupByRisk(kris), groupMaturityByCompany(maturity),
				groupKpaKpiByCompany(kpaKpi));
	}

	private long countRows(RegisterData data) {
		long total = 0;
		for (Risk risk : data.risks()) {
			long count = 1;
			count = safeMultiply(count, dimensionSize(data.assessments().get(risk.getId())));
			count = safeMultiply(count, dimensionSize(data.controls().get(risk.getId())));
			count = safeMultiply(count, dimensionSize(data.responses().get(risk.getId())));
			count = safeMultiply(count, dimensionSize(data.reviews().get(risk.getId())));
			count = safeMultiply(count, dimensionSize(data.kris().get(risk.getId())));
			count = safeMultiply(count, dimensionSize(data.maturity().get(risk.getCompanyId())));
			count = safeMultiply(count, dimensionSize(data.kpaKpi().get(risk.getCompanyId())));
			total = safeAdd(total, count);
		}
		return total;
	}

	private void forEachRow(RegisterData data, long from, long to, Consumer<RiskRegisterRow> consumer) {
		long index = 0;
		for (Risk risk : data.risks()) {
			for (RiskAssessment assessment : orNull(data.assessments().get(risk.getId()))) {
				for (RiskControl control : orNull(data.controls().get(risk.getId()))) {
					for (RiskResponseTreatment response : orNull(data.responses().get(risk.getId()))) {
						for (RiskReview review : orNull(data.reviews().get(risk.getId()))) {
							for (KriKpiReview kri : orNull(data.kris().get(risk.getId()))) {
								for (ERMMaturityAssessment maturity : orNull(data.maturity().get(risk.getCompanyId()))) {
									for (KpaKpiReview kpaKpi : orNull(data.kpaKpi().get(risk.getCompanyId()))) {
										if (index >= from && index < to) {
											consumer.accept(mapRow(risk, assessment, control, response, review, kri,
													maturity, kpaKpi));
										}
										index++;
										if (index >= to) {
											return;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private RiskRegisterRow mapRow(Risk risk, RiskAssessment assessment, RiskControl control,
			RiskResponseTreatment response, RiskReview review, KriKpiReview kri, ERMMaturityAssessment maturity,
			KpaKpiReview kpaKpi) {

		RiskRegisterRow row = new RiskRegisterRow();
		row.setRisk(map("risk", new RiskResponse(risk)));
		if (assessment != null) {
			row.setRiskAssessment(map("riskAssessment", new RiskAssessmentResponse(assessment)));
		}
		if (control != null) {
			row.setRiskControl(map("riskControl", new RiskControlResponse(control)));
		}
		if (response != null) {
			row.setRiskResponse(map("riskTreatment", new RiskResponseTreatmentResponse(response)));
		}
		if (review != null) {
			row.setRiskReview(map("riskReview", new RiskReviewResponseDtoResponse(review)));
		}
		if (kri != null) {
			row.setKri(map("kriKpiReview", new KriKpiReviewResponseDTO(kri)));
		}
		if (maturity != null) {
			row.setErmMaturity(map("ermMaturity", new ErmMaturityResponse(maturity)));
		}
		if (kpaKpi != null) {
			row.setKpaKpiReview(map("kpaKpiReview", kpaKpiReviewService.toResponse(kpaKpi)));
		}
		return row;
	}

	private List<CustomResponse> map(String tableName, Object dto) {
		return customResponseMapper.map(tableName, 1L, dto, true);
	}

	private LinkedHashMap<String, String> flatten(RiskRegisterRow row) {
		LinkedHashMap<String, String> values = new LinkedHashMap<>();
		addFields(values, "risk", row.getRisk());
		addFields(values, "riskAssessment", row.getRiskAssessment());
		addFields(values, "riskControl", row.getRiskControl());
		addFields(values, "riskResponse", row.getRiskResponse());
		addFields(values, "riskReview", row.getRiskReview());
		addFields(values, "kri", row.getKri());
		addFields(values, "ermMaturity", row.getErmMaturity());
		addFields(values, "kpaKpiReview", row.getKpaKpiReview());
		return values;
	}

	private void addFields(Map<String, String> target, String prefix, List<CustomResponse> fields) {
		for (CustomResponse field : fields) {
			target.put(prefix + "." + field.getFieldName(), field.getValue());
		}
	}

	private void appendCsvLine(StringBuilder csv, List<String> values) {
		for (int i = 0; i < values.size(); i++) {
			if (i > 0) {
				csv.append(',');
			}
			csv.append(escapeCsv(values.get(i)));
		}
		csv.append("\r\n");
	}

	private String escapeCsv(String value) {
		if (value == null) {
			return "";
		}
		boolean quoted = value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\r') >= 0
				|| value.indexOf('\n') >= 0;
		String escaped = value.replace("\"", "\"\"");
		return quoted ? "\"" + escaped + "\"" : escaped;
	}

	private static <T> List<T> orNull(List<T> values) {
		return values == null || values.isEmpty() ? Collections.singletonList(null) : values;
	}

	private static int dimensionSize(List<?> values) {
		return values == null || values.isEmpty() ? 1 : values.size();
	}

	private static long safeMultiply(long left, long right) {
		if (left > Long.MAX_VALUE / right) {
			return Long.MAX_VALUE;
		}
		return left * right;
	}

	private static long safeAdd(long left, long right) {
		return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
	}

	private static <T> Map<Long, List<T>> groupByRisk(List<T> values) {
		return values.stream().collect(Collectors.groupingBy(RiskRegisterService::riskId,
				LinkedHashMap::new, Collectors.toList()));
	}

	private static Long riskId(Object value) {
		if (value instanceof RiskAssessment item) return item.getRisk().getId();
		if (value instanceof RiskControl item) return item.getRisk().getId();
		if (value instanceof RiskResponseTreatment item) return item.getRisk().getId();
		if (value instanceof RiskReview item) return item.getRisk().getId();
		if (value instanceof KriKpiReview item) return item.getRisk().getId();
		throw new IllegalArgumentException("Unsupported risk register type: " + value.getClass().getName());
	}

	private static Map<Long, List<ERMMaturityAssessment>> groupMaturityByCompany(
			List<ERMMaturityAssessment> values) {
		return values.stream().filter(item -> item.getCompany() != null)
				.collect(Collectors.groupingBy(item -> item.getCompany().getId(), LinkedHashMap::new,
						Collectors.toList()));
	}

	private static Map<Long, List<KpaKpiReview>> groupKpaKpiByCompany(List<KpaKpiReview> values) {
		return values.stream().filter(item -> item.getCompany() != null)
				.collect(Collectors.groupingBy(item -> item.getCompany().getId(), LinkedHashMap::new,
						Collectors.toList()));
	}

	private record RegisterData(List<Risk> risks, Map<Long, List<RiskAssessment>> assessments,
			Map<Long, List<RiskControl>> controls, Map<Long, List<RiskResponseTreatment>> responses,
			Map<Long, List<RiskReview>> reviews, Map<Long, List<KriKpiReview>> kris,
			Map<Long, List<ERMMaturityAssessment>> maturity, Map<Long, List<KpaKpiReview>> kpaKpi) {

		private static RegisterData empty() {
			return new RegisterData(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(),
					Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
					Collections.emptyMap());
		}
	}
}
