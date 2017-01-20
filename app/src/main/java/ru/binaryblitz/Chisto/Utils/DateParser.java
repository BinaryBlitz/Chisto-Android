package ru.binaryblitz.Chisto.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParser {
    private static final String DIVIDER = "Z";
    private static final String DEFAULT_TIME_ZONE = "GMT-00:00";
    private static final String GMT = "GMT";
    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";
    private static final int TIME_ZONE_LENGTH = 6;

    public static Date parse(String input) throws java.text.ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT);

        if (input.endsWith(DIVIDER)) {
            input = input.substring(0, input.length() - 1) + DEFAULT_TIME_ZONE;
        } else {
            int timeZoneLength = TIME_ZONE_LENGTH;

            String date = input.substring(0, input.length() - timeZoneLength);
            String timeZone = input.substring(input.length() - timeZoneLength, input.length());

            input = date + GMT + timeZone;
        }

        return dateFormat.parse(input);
    }
}
