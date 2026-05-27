package ermorg.erm.erm_api_gateway.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_api_gateway.dto.response.RightResponse;
import ermorg.erm.erm_api_gateway.dto.response.RoleResponse;
import ermorg.erm.erm_api_gateway.dto.response.TokenResponseDTO;
import ermorg.erm.erm_api_gateway.dto.response.UserResponse;
import ermorg.erm.erm_api_gateway.exception.PasswordNotMatchedException;
import ermorg.erm.erm_api_gateway.model.Role;
import ermorg.erm.erm_api_gateway.model.User;
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
	public UserResponse getUserByUsername(String username) {

	    User user = userRepository.findByEmailWithRoles(username)
	    		 .filter(u -> u.getEmail().equals(username))
	            .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

	    List<RoleResponse> roles = user.getRoles().stream()
	            .map(this::mapToRoleResponse)
	            .toList();

	    return new UserResponse(user, roles);
	}
	
	private RoleResponse mapToRoleResponse(Role role) {
	    List<RightResponse> rights = role.getRoleRights().stream()
	            .map(RightResponse::new)
	            .toList();

	    return new RoleResponse(role, rights);
	}
}
