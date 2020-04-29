package com.elletrudgett.cards.cah;

import java.io.Serializable;
import java.security.MessageDigest;

public class MD5Helper implements Serializable {
    public static String md5Hex(String message) {
        try {
            MessageDigest md =
                    MessageDigest.getInstance("MD5");
            return hex(md.digest(message.getBytes("CP1252")));
        } catch (Exception e) {
            return null;
        }
    }

    private static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }
}