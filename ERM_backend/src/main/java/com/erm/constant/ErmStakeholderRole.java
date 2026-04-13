package com.erm.constant;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ErmStakeholderRole {

	RISK_OWNER("Risk Owner", "risk-owner"),
	RISK_CHAMPION("Risk Champion", "risk-champion"),
	CONTROL_OWNER("Control Owner", "control-owner"),
	PRIMARY_RESPONSIBLE("Primary Responsible", "primary-responsible"),
	APPROVER("Approver", "approver"),
	RISK_REPORTING("Risk Reporting", "risk-reporting");

	private final String displayName;
	private final String apiKey;

	private static final Map<String, ErmStakeholderRole> BY_KEY = Stream.of(values())
			.collect(Collectors.toUnmodifiableMap(r -> r.apiKey.toLowerCase(Locale.ROOT), r -> r));

	ErmStakeholderRole(String displayName, String apiKey) {
		this.displayName = displayName;
		this.apiKey = apiKey;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getApiKey() {
		return apiKey;
	}

	public static ErmStakeholderRole fromApiKey(String roleKey) {
		if (roleKey == null || roleKey.isBlank()) {
			throw new IllegalArgumentException("roleKey is required.");
		}
		String normalized = roleKey.trim().toLowerCase(Locale.ROOT);
		ErmStakeholderRole resolved = BY_KEY.get(normalized);
		if (resolved == null) {
			throw new IllegalArgumentException("Invalid role key: " + roleKey);
		}
		return resolved;
	}
}
