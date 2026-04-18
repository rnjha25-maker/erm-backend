package ermorg.erm.erm_api_gateway.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_api_gateway.dto.response.RightResponse;
import ermorg.erm.erm_api_gateway.dto.response.RoleResponse;
import ermorg.erm.erm_api_gateway.dto.response.TokenResponseDTO;
import ermorg.erm.erm_api_gateway.dto.response.UserResponse;
import ermorg.erm.erm_api_gateway.exception.PasswordNotMatchedException;
import ermorg.erm.erm_api_gateway.model.Role;
import ermorg.erm.erm_api_gateway.model.UserDetail;
import ermorg.erm.erm_api_gateway.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService implements IUserService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public TokenResponseDTO generateToken(String username, String password)
			throws UsernameNotFoundException, PasswordNotMatchedException {
		return null;
	}

	@Override
	@Transactional
//	@Cacheable(value = "user", key = "#username")
	public UserResponse getUserByUsername(String username) {
		ermorg.erm.erm_api_gateway.model.User user = userRepository.findUserByEmail(username);

		if (user == null)
			throw new UsernameNotFoundException("User not found!");
UserDetail userDetail = user.getUserDetail();
		List<Role> roles2 = user.getRoles();

		List<RoleResponse> roles = user.getRoles().stream()
		        .map(role -> new RoleResponse(role, role.getRoleRights().stream()
		                .map(rt -> new RightResponse(rt))
		                .collect(Collectors.toList())))
		        .collect(Collectors.toList());

//		return roleResponse;
//		List<RightResponse> rights = user.getRole() != null ? user.getRole().getRoleRights().stream()
//				.map(roleRight -> new RightResponse(roleRight)).collect(Collectors.toList()) : new ArrayList<>();
//
//
//		
//		List<RoleResponse> roles = new ArrayList<>();
//		roles = user.getRole() != null ? user.getRole().getRoleRights()
//				.stream()
//				.map(rt-> new RoleResponse(rt.getRole(), rt.getRights()))
//				.distinct()
//				.collect(Collectors.toList()) : new ArrayList<>();
//		
//		System.out.println( "role size " + roles.size());
//		
//		
//		
		UserResponse userResponse = new UserResponse(user, roles);
		return userResponse;
//		return null;
	}
}
