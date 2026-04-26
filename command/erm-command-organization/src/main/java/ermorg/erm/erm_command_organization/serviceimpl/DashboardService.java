package ermorg.erm.erm_command_organization.serviceimpl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.responseDTO.DashboardDto;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.Organization;
import ermorg.erm.erm_command_organization.model.Plan;
import ermorg.erm.erm_command_organization.model.User;
import ermorg.erm.erm_command_organization.repository.OrganizationRepository;
import ermorg.erm.erm_command_organization.repository.UserRepository;
import ermorg.erm.erm_command_organization.service.IDashboardService;

@Service
public class DashboardService implements IDashboardService {

	@Autowired
	private OrganizationRepository organizationRepository;
	
	private String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	@Autowired
	private UserRepository userRepository;
	@Override
	public DashboardDto getDashboardData(String period) throws ResourceNotFoundException {
		LocalDate now = LocalDate.now();
		LocalDate startDate = null;
		LocalDate endDate = null;

		switch (period) {
		case "today":
			startDate = now;
			endDate = now;
			break;
		case "thisMonth":
			startDate = now.withDayOfMonth(1);
			endDate = now.withDayOfMonth(now.lengthOfMonth());
			break;
		case "thisWeek":
			startDate = now.with(DayOfWeek.MONDAY);
			endDate = now.with(DayOfWeek.SUNDAY);
			break;
		case "thisYear":
			startDate = now.withMonth(1);
			endDate = now.withMonth(12);
			break;
		default:
			startDate = null;
			endDate = null;
			break;
		}
		List<Organization> organizations;
		List<User> users;
		if(startDate != null && endDate != null) {
	        Date startDateInDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	        Date endtDateInDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

			organizations = organizationRepository.findAllByCreatedAtBetween(startDateInDate, endtDateInDate);
			users = userRepository.findAllByCreatedAtBetween(startDateInDate, endtDateInDate);
		}else {
			organizations = organizationRepository.findAll();
			users = userRepository.findAll();
		}
		
		long totalUsers = users.stream().filter(user -> user.getDeleted() != null && !user.getDeleted()).count();
		

		Map<String, Long> userGroupCount = users.stream()
		.filter(user -> user.getDeleted() != null && !user.getDeleted())
		.collect(Collectors.groupingBy(userGrp ->{
			Plan plan = userGrp.getOrganization() != null ? userGrp.getOrganization().getPlan() : null;
			if(plan == null) {

				return "Unknown";
			}
			
			return plan.getPlanName();
		} , Collectors.counting()));
		
		userGroupCount.put("total", totalUsers);
		Map<String, Long> orgGroupByStatusCount = organizations.stream()
		.collect(Collectors.groupingBy(org ->{
			if(org.getStatus() == null) {
				return "Unknown";
			}
			
			return org.getStatus();
		} , Collectors.counting()));
		
		
		Map<String, Long> orgGroupByPlanCount = organizations.stream()
				.collect(Collectors.groupingBy(org -> {
					Plan plan = org.getPlan();
					if(plan == null) {
						return "Unknown";
					}
					return plan.getPlanName();
				}, Collectors.counting()));
			
		Map<String, Long> orgGroupByCityCount = organizations.stream()
				.collect(Collectors.groupingBy(org ->{
					if(org.getState() != null) {
						return org.getState().getName();
					}
					return "Unknown";
				} , Collectors.counting()));

		Map<String, Long> userGroupByMonthCount = new LinkedHashMap<>();
		
		for(int i = 0; i< months.length; i++) {
			userGroupByMonthCount.put(months[i], 0l);
		}
		
		Map<String, Long> userGrowth = organizations.stream()
				.collect(Collectors.groupingBy(org ->{
					Date createdAt = org.getCreatedAt();
					if(createdAt == null) {
						
						Random rand = new Random();
						return  months[ rand.nextInt(12)];
					}
					return  months[ createdAt.getMonth()];
				}, Collectors.counting()));

		userGrowth.entrySet().stream().forEach(entry -> userGroupByMonthCount.put(entry.getKey(), entry.getValue()));
		DashboardDto dashboardDto = new DashboardDto(totalUsers, userGroupCount, orgGroupByStatusCount, orgGroupByPlanCount, orgGroupByCityCount, userGroupByMonthCount);
		return dashboardDto;
	}

}
