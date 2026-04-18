package ermorg.erm.erm_api_gateway.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPResolver {
    public static String getHostIP() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();  
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to resolve local host IP", e);
        }
    }
}

