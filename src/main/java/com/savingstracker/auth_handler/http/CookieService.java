package com.savingstracker.auth_handler.http;

import org.springframework.stereotype.Service;

@Service
public class CookieService {
    public String createCookie(String name, String value, long maxAgeInSeconds) {
        return String.format(
            "%s=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None",
            name, value, maxAgeInSeconds
        );
    }
}
