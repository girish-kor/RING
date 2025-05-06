package com.ring.Ring.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class FilterUtils {
    private static final List<String> INAPPROPRIATE_WORDS = Arrays.asList(
            "profanity1", "profanity2", "profanity3", "slur1", "slur2"
            // Add more inappropriate words as needed
    );

    private static final Pattern PHONE_NUMBER_PATTERN =
            Pattern.compile("\\b(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");

    public boolean containsInappropriateText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerCaseText = text.toLowerCase();

        // Check for inappropriate words
        for (String word : INAPPROPRIATE_WORDS) {
            if (lowerCaseText.contains(word.toLowerCase())) {
                return true;
            }
        }

        // Check for personal information
        if (containsPersonalInformation(text)) {
            return true;
        }

        return false;
    }

    private boolean containsPersonalInformation(String text) {
        // Check for phone numbers
        if (PHONE_NUMBER_PATTERN.matcher(text).find()) {
            return true;
        }

        // Check for email addresses
        if (EMAIL_PATTERN.matcher(text).find()) {
            return true;
        }

        return false;
    }
}