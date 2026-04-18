package ermorg.erm.erm_command_organization.util;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static boolean isTesting = true;
    public static String generate(int length) {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        StringBuilder password = new StringBuilder("Password2.0");
		if(!isTesting){
		
		    for (int i = 0; i < length; i++) {
		        int index = random.nextInt(allowedChars.length());
		        password.append(allowedChars.charAt(index));
		    }
		}

        return password.toString();
    }
}

