package ermorg.erm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Risk;
import ermorg.erm.model.SubRisk;

public final class SubRiskSelectionUtil {

	private SubRiskSelectionUtil() {
	}

	public static List<SubRisk> resolveSelectedSubRisks(Risk risk, List<Long> requestedIds)
			throws ResourceNotFoundException {
		if (requestedIds == null || requestedIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> invalidIds = requestedIds.stream()
				.filter(id -> id == null || id <= 0)
				.toList();
		if (!invalidIds.isEmpty()) {
			throw new ResourceNotFoundException("Please select valid sub risk.");
		}

		Set<Long> uniqueIds = new LinkedHashSet<>(requestedIds);
		Map<Long, SubRisk> subRisksById = safeSubRisks(risk).stream()
				.filter(subRisk -> !Boolean.TRUE.equals(subRisk.getDeleted()))
				.collect(Collectors.toMap(SubRisk::getId, Function.identity(), (left, right) -> left));

		List<Long> missingIds = uniqueIds.stream()
				.filter(id -> !subRisksById.containsKey(id))
				.toList();
		if (!missingIds.isEmpty()) {
			throw new ResourceNotFoundException("Selected sub risk does not belong to the selected risk.");
		}

		return uniqueIds.stream()
				.map(subRisksById::get)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private static List<SubRisk> safeSubRisks(Risk risk) {
		if (risk == null || risk.getSubRisk() == null) {
			return Collections.emptyList();
		}
		return risk.getSubRisk();
	}
}
