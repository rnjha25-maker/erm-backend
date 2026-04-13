package com.user_setup.util;

import com.user_setup.modal.User;

public class CurrentUserContext {
	private static final ThreadLocal<User> currentUserThreadLocal = new ThreadLocal<>();

	public static void setCurrentUser(User user) {
		currentUserThreadLocal.set(user);
	}

	public static User getOrganization() {
	    return currentUserThreadLocal.get();
	}

	public static void clear() {
		currentUserThreadLocal.remove();
	}
}

