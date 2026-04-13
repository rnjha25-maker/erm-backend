package com.user_setup.dto.response;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.user_setup.modal.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

	private Long userId;
	private Long orgId;
	private String email;
	private String passKey;
    private String firstName;
    private String lastName;
    private String phone;
    private String profileImageUrl;
    
    @JsonDeserialize(using = UserResponse.RoleResponseDTOSetDeserializer.class)
    private List<RoleResponse> roles;

	public UserResponse(User user, List<RoleResponse> roles) {
		super();
		this.userId = user.getId();
		this.email = user.getEmail();
		this.orgId = user.getUserDetail().getOrganization().getId();
		this.firstName = user.getUserDetail().getFirstName();
		this.lastName = user.getUserDetail().getLastName();
		this.phone = user.getUserDetail().getPhone();
		this.profileImageUrl = user.getUserDetail().getProfileImageUrl();
		this.passKey = user.getPassword();
		this.roles = roles;
	}
	
	public static class RoleResponseDTOSetDeserializer extends JsonDeserializer<Set<RoleResponse>> {
		
		
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
