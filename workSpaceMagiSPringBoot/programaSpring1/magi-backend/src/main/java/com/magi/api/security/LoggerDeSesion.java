package com.magi.api.security;

import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

@Component
public class LoggerDeSesion {

    private static final Path LOG_FILE = Paths.get("login.log");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_DATE;      
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    //AQUUI SE ESCUCHA EL LOGIN
    @EventListener
    public void onLogin(AuthenticationSuccessEvent e) {
        UserDetails u = (UserDetails) e.getAuthentication().getPrincipal();
        writeLine(u.getUsername(), "LOGIN");
    }

    //AQUI SE ESCUCHA EL LOGGOUT
    @EventListener
    public void onLogout(LogoutSuccessEvent e) {
        UserDetails u = (UserDetails) e.getAuthentication().getPrincipal();
        writeLine(u.getUsername(), "LOGOUT");
    }

   
    private void writeLine(String dni, String action) {
        String line = String.format("%s|%s|%s|%s%n",
                dni,
                LocalDate.now().format(DATE),
                LocalTime.now().format(TIME),
                action);

        try {
            Files.writeString(LOG_FILE, line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (Exception ex) {
            //SE INTENTA NO PARAR LA APP SOLO SE MUESTRA TRAZA
            ex.printStackTrace();
        }
    }
}
