package com.raizes.nordeste.pedidos.util;

public class MaskingUtil {

    public static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) return "***.***.***-**";
        String clean = cpf.replaceAll("\\D", "");
        if (clean.length() == 11) {
            return "***.***." + clean.substring(6, 9) + "-" + clean.substring(9);
        }
        return "***.***.***-**";
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***@***.com";
        int atIndex = email.indexOf('@');
        String name = email.substring(0, atIndex);
        if (name.length() <= 2) {
            return "**" + email.substring(atIndex);
        }
        return name.charAt(0) + "****" + name.charAt(name.length() - 1) + email.substring(atIndex);
    }
}