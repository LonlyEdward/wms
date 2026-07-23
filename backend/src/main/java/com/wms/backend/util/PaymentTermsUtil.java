package com.wms.backend.util;

import java.time.LocalDate;

public class PaymentTermsUtil {

    private PaymentTermsUtil() {}

    // Calculate due date based on payment terms string
    // IMMEDIATE = due today
    // NET_7 = due in 7 days
    // NET_14 = due in 14 days
    // NET_30 = due in 30 days
    // NET_60 = due in 60 days
    public static LocalDate calculateDueDate(String paymentTerms,
                                             LocalDate issueDate) {
        if (paymentTerms == null) {
            return issueDate.plusDays(30); // default to NET_30
        }

        return switch (paymentTerms.toUpperCase()) {
            case "IMMEDIATE" -> issueDate;
            case "NET_7"     -> issueDate.plusDays(7);
            case "NET_14"    -> issueDate.plusDays(14);
            case "NET_30"    -> issueDate.plusDays(30);
            case "NET_60"    -> issueDate.plusDays(60);
            default          -> issueDate.plusDays(30);
        };
    }

    // Return the number of days a payment term represents
    public static int getDays(String paymentTerms) {
        if (paymentTerms == null) return 30;
        return switch (paymentTerms.toUpperCase()) {
            case "IMMEDIATE" -> 0;
            case "NET_7"     -> 7;
            case "NET_14"    -> 14;
            case "NET_30"    -> 30;
            case "NET_60"    -> 60;
            default          -> 30;
        };
    }
}