package ermorg.user_setup.util;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generate(int length) {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(allowedChars.length());
            password.append(allowedChars.charAt(index));
        }

        return "Password2.0";//password.toString();
    }
}

