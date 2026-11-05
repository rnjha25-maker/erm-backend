package ermorg.erm.serviceimpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.model.Notification;
import ermorg.erm.repository.NotificationRepository;

@Service
public class NotificationPersistenceService {

    private final NotificationRepository notificationRepository;

    public NotificationPersistenceService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Notification notification) {
        notificationRepository.save(notification);
    }
}
