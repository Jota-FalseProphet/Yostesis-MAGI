// Utils.java
package com.magi.asistencia.util;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String format(long ms){
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(ms));
    }
    public static String read(InputStream in) throws Exception{
        BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(); String line;
        while((line=r.readLine())!=null) sb.append(line);
        return sb.toString();
    }
}
