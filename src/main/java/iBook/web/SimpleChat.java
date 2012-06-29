package iBook.web;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;


public class SimpleChat {
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    static String decodeUTF8(byte[] bytes) {
        return new String(bytes, UTF8_CHARSET);
    }

    static byte[] encodeUTF8(String string) {
        return string.getBytes(UTF8_CHARSET);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        byte[] s = encodeUTF8("Brasília");


        System.out.println(new String("Brasília".getBytes("UTF-8"), "UTF-8"));
    }
}