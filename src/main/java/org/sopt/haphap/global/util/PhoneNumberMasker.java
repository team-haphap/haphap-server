package org.sopt.haphap.global.util;

import org.springframework.stereotype.Component;

@Component
public class PhoneNumberMasker {

    public String mask(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            return rawPhoneNumber;
        }
        String digitsOnly = rawPhoneNumber.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 4) {
            return "****";
        }
        String last4 = digitsOnly.substring(digitsOnly.length() - 4);
        return "***-****-" + last4;
    }
}