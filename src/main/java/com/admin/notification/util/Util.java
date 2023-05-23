package com.admin.notification.util;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Util{

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getUniqueID(final String text) {
        return text.concat("_").concat(getUUID());
    }
}
