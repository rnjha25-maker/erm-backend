package com.erm.util;

import com.erm.model.User;

public class UserContext {

	private static final ThreadLocal<User> companyThreadLocal = new ThreadLocal<>();

    public static void seetUser(User user) {
    	companyThreadLocal.set(user);
    }

    public static User getUser() {
        return companyThreadLocal.get();
    }

    public static void clear() {
    	companyThreadLocal.remove();
    }
}
