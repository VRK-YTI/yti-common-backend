package fi.vm.yti.common.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CommonUtils {

    private CommonUtils() {
        // Util class
    }

     public static String encode(String param) {
        return URLEncoder.encode(param, StandardCharsets.UTF_8);
    }

}
