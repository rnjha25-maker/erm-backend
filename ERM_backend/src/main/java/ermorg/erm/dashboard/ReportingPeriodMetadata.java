package ermorg.erm.dashboard;

/** Optional metadata: legacy clients may omit both fields, including on updates. */
public record ReportingPeriodMetadata(Integer year, ChartPeriod.YearType yearType) {
    public static ReportingPeriodMetadata resolve(Integer requestedYear, ChartPeriod.YearType requestedType,
            Integer existingYear, ChartPeriod.YearType existingType) {
        if (requestedYear == null && requestedType == null)
            return new ReportingPeriodMetadata(existingYear,existingType);
        if (requestedYear == null || requestedType == null)
            throw new IllegalArgumentException("Send reportingYear and reportingYearType together.");
        ChartPeriod.of(requestedYear,requestedType,null);
        if (existingYear != null && (!existingYear.equals(requestedYear) || existingType != requestedType))
            throw new IllegalArgumentException("Create a separate annual entry for a different reporting year.");
        return new ReportingPeriodMetadata(requestedYear,requestedType);
    }
    public void validateFrequency(String evaluation, String reporting) {
        if(year == null) return; // Retain existing clients' validation contract until explicitly tagged.
        if (evaluation == null || reporting == null || !evaluation.trim().equalsIgnoreCase(reporting.trim())
                || !(evaluation.trim().equalsIgnoreCase("MONTHLY") || evaluation.trim().equalsIgnoreCase("QUARTERLY")))
            throw new IllegalArgumentException("Year-tagged entries require matching Monthly or Quarterly evaluation and reporting frequencies.");
    }
}
