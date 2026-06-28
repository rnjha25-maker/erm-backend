package ermorg.erm.util;

import java.math.BigDecimal;
import java.util.Collection;

public final class MaturityLevelResolver {

	private MaturityLevelResolver() {
	}

	public static double parseMarksAchieved(String marksAchieved) {
		if (marksAchieved == null || marksAchieved.isBlank()) {
			return 0;
		}
		return Double.parseDouble(marksAchieved.trim());
	}

	public static BigDecimal sumMarksAchieved(Collection<String> marksAchievedValues) {
		if (marksAchievedValues == null || marksAchievedValues.isEmpty()) {
			return BigDecimal.ZERO;
		}
		double total = marksAchievedValues.stream().mapToDouble(MaturityLevelResolver::parseMarksAchieved).sum();
		return BigDecimal.valueOf(total);
	}

	public static String resolveMaturityLabel(double totalScore) {
		if (totalScore < 20) {
			return "Nascent";
		}
		if (totalScore < 40) {
			return "Emerging";
		}
		if (totalScore < 60) {
			return "Developed";
		}
		if (totalScore < 80) {
			return "Integrated";
		}
		return "Advanced";
	}
}
