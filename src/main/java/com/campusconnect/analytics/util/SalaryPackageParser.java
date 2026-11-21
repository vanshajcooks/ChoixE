package com.campusconnect.analytics.util;

import com.campusconnect.analytics.dto.SalaryPackageStats;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalaryPackageParser {

    private static final Pattern RANGE_LPA_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:-|to)\\s*(\\d+(?:\\.\\d+)?)\\s*(?:lpa|lakh|lakhs)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SINGLE_LPA_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:lpa|lakh|lakhs)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MONTHLY_PATTERN = Pattern.compile("(\\d+(?:[\\d,]*\\d+)?)\\s*(?:/\\s*month|pm|per\\s*month)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RAW_NUMBER_PATTERN = Pattern.compile("^\\s*(\\d+(?:[\\d,]*\\d+)?)\\s*$");

    public static Optional<Double> parseLpa(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String clean = raw.trim();

        Matcher rangeMatcher = RANGE_LPA_PATTERN.matcher(clean);
        if (rangeMatcher.find()) {
            try {
                double low = Double.parseDouble(rangeMatcher.group(1));
                double high = Double.parseDouble(rangeMatcher.group(2));
                return Optional.of(Math.round(((low + high) / 2.0) * 100.0) / 100.0);
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher lpaMatcher = SINGLE_LPA_PATTERN.matcher(clean);
        if (lpaMatcher.find()) {
            try {
                return Optional.of(Double.parseDouble(lpaMatcher.group(1)));
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher monthlyMatcher = MONTHLY_PATTERN.matcher(clean);
        if (monthlyMatcher.find()) {
            try {
                String numStr = monthlyMatcher.group(1).replace(",", "");
                double monthly = Double.parseDouble(numStr);
                double annualLpa = (monthly * 12.0) / 100000.0;
                return Optional.of(Math.round(annualLpa * 100.0) / 100.0);
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher rawMatcher = RAW_NUMBER_PATTERN.matcher(clean);
        if (rawMatcher.find()) {
            try {
                String numStr = rawMatcher.group(1).replace(",", "");
                double num = Double.parseDouble(numStr);
                if (num >= 50000) {
                    return Optional.of(Math.round((num / 100000.0) * 100.0) / 100.0);
                } else if (num > 0 && num <= 150) {
                    return Optional.of(num);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return Optional.empty();
    }

    public static SalaryPackageStats computeStats(List<String> salaryStrings) {
        if (salaryStrings == null || salaryStrings.isEmpty()) {
            return new SalaryPackageStats(null, null, null, 0, 0, 0, 0);
        }

        List<Double> lpas = new ArrayList<>();
        for (String s : salaryStrings) {
            parseLpa(s).ifPresent(lpas::add);
        }

        if (lpas.isEmpty()) {
            return new SalaryPackageStats(null, null, null, 0, 0, 0, 0);
        }

        Collections.sort(lpas);

        double highest = lpas.get(lpas.size() - 1);
        double sum = 0.0;
        long above10 = 0;
        long between6And10 = 0;
        long below6 = 0;

        for (double val : lpas) {
            sum += val;
            if (val >= 10.0) {
                above10++;
            } else if (val >= 6.0) {
                between6And10++;
            } else {
                below6++;
            }
        }

        double average = Math.round((sum / lpas.size()) * 100.0) / 100.0;

        double median;
        int n = lpas.size();
        if (n % 2 == 1) {
            median = lpas.get(n / 2);
        } else {
            median = Math.round(((lpas.get(n / 2 - 1) + lpas.get(n / 2)) / 2.0) * 100.0) / 100.0;
        }

        return new SalaryPackageStats(
                highest,
                average,
                median,
                above10,
                between6And10,
                below6,
                lpas.size()
        );
    }
}
