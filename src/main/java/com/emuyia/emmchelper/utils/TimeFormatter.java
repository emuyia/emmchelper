package com.emuyia.emmchelper.utils;

import java.util.concurrent.TimeUnit;

public class TimeFormatter {

    public static String formatMillis(long millis) {
        if (millis < 0) {
            return "0s";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder();
        boolean hasContent = false;

        if (days > 0) {
            sb.append(days).append("d");
            hasContent = true;
        }
        if (hours > 0) {
            if (hasContent) sb.append(" ");
            sb.append(hours).append("h");
            hasContent = true;
        }
        if (minutes > 0) {
            if (hasContent) sb.append(" ");
            sb.append(minutes).append("m");
            hasContent = true;
        }
        // Always show seconds if it's the only unit or if other units are present
        if (seconds > 0 || !hasContent) {
            if (hasContent) sb.append(" ");
            sb.append(seconds).append("s");
        }
        
        if (sb.length() == 0) { // Should only happen if input millis was 0
            return "0s";
        }

        return sb.toString();
    }
}
