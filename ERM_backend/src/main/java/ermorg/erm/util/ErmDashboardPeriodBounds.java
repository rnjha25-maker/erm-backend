package ermorg.erm.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import ermorg.erm.constant.ErmDashboardPeriodType;
import lombok.Getter;

@Getter
public final class ErmDashboardPeriodBounds {

	private final Date startInclusive;
	private final Date endInclusive;

	private ErmDashboardPeriodBounds(Date startInclusive, Date endInclusive) {
		this.startInclusive = startInclusive;
		this.endInclusive = endInclusive;
	}

	public static ErmDashboardPeriodBounds forYearAndPeriod(int year, ErmDashboardPeriodType periodType, ZoneId zoneId) {
		LocalDate start;
		LocalDate end;
		switch (periodType) {
		case Q1:
			start = LocalDate.of(year, 1, 1);
			end = LocalDate.of(year, 3, 31);
			break;
		case Q2:
			start = LocalDate.of(year, 4, 1);
			end = LocalDate.of(year, 6, 30);
			break;
		case Q3:
			start = LocalDate.of(year, 7, 1);
			end = LocalDate.of(year, 9, 30);
			break;
		case Q4:
			start = LocalDate.of(year, 10, 1);
			end = LocalDate.of(year, 12, 31);
			break;
		case H1:
			start = LocalDate.of(year, 1, 1);
			end = LocalDate.of(year, 6, 30);
			break;
		case H2:
			start = LocalDate.of(year, 7, 1);
			end = LocalDate.of(year, 12, 31);
			break;
		case YEAR:
			start = LocalDate.of(year, 1, 1);
			end = LocalDate.of(year, 12, 31);
			break;
		}
		ZonedDateTime startZdt = start.atStartOfDay(zoneId);
		ZonedDateTime endZdt = end.atTime(LocalTime.MAX).atZone(zoneId);
		return new ErmDashboardPeriodBounds(Date.from(startZdt.toInstant()), Date.from(endZdt.toInstant()));
	}
}
