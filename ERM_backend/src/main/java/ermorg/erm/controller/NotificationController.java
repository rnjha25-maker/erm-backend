package ermorg.erm.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.NotificationResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.serviceimpl.NotificationService;

@RestController
@RequestMapping("notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	public GeneralResponse<List<NotificationResponse>> myNotifications() throws ResourceNotFoundException {
		GeneralResponse<List<NotificationResponse>> response = new GeneralResponse<>();
		response.setData(notificationService.getMyNotifications());
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
}
