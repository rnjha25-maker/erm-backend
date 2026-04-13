package com.erm.erm_api_gateway.dto.response;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.erm.erm_api_gateway.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.jsonwebtoken.io.IOException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {
	private Long userId;
	private Long orgId;
	private String email;
	@JsonIgnore
	private String passKey;
	private String firstName;
	private String lastName;
	private String phone;
	private String profileImageUrl;
	private Long companyId;

	@JsonDeserialize(using = UserResponse.RoleResponseSetDeserializer.class)
	private List<RoleResponse> roles;

	public UserResponse(User user, List<RoleResponse> roles) {
		super();
		this.userId = user.getId();
		this.email = user.getEmail();
		this.orgId = user.getUserDetail() != null && user.getUserDetail() != null && user.getUserDetail().getOrganization() != null
				? user.getUserDetail().getOrganization().getId()
				: -1l;
		this.firstName = user.getUserDetail().getFirstName();
		this.lastName = user.getUserDetail().getLastName();
		this.phone = user.getUserDetail().getPhone();
		this.profileImageUrl = user.getUserDetail().getProfileImageUrl();
		this.passKey = user.getPassword();
		this.roles = roles;
		this.companyId = user.getCompay() != null ? user.getCompay().getId() : -1l;
	}

	public static class RoleResponseSetDeserializer extends JsonDeserializer<Set<RoleResponse>> {

		@Override
		public Set<RoleResponse> deserialize(JsonParser p, DeserializationContext ctxt) throws java.io.IOException {
			JsonNode node = p.getCodec().readTree(p);
			Set<RoleResponse> roles = new HashSet<>();
			for (JsonNode element : node) {
				RoleResponse role = p.getCodec().treeToValue(element, RoleResponse.class);
				roles.add(role);
			}
			return roles;
		}
	}

}
