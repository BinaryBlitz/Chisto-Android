package ru.binaryblitz.Chisto.Utils;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import java.util.Locale;

public class CustomPhoneNumberTextWatcher implements TextWatcher {

    private boolean selfChange = false;
    private AsYouTypeFormatter formatter;

    public CustomPhoneNumberTextWatcher() {
        this(Locale.getDefault().getCountry());
    }

    CustomPhoneNumberTextWatcher(String countryCode) {
        if (countryCode == null) throw new IllegalArgumentException();
        formatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public synchronized void afterTextChanged(Editable s) {
        if (selfChange) return;

        String formatted = new Formatter(s, Selection.getSelectionEnd(s)).reformat();
        if (formatted != null) process(s, formatted);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PhoneNumberUtils.addTtsSpan(s, 0, s.length());
        }
    }

    private void process(Editable s, String formatted) {
        int rememberedPos = formatter.getRememberedPosition();
        selfChange = true;
        s.replace(0, s.length(), formatted, 0, formatted.length());
        if (formatted.equals(s.toString())) Selection.setSelection(s, rememberedPos);
        selfChange = false;
    }

    private class Formatter {
        int currentIndex;
        CharSequence s;
        char lastNonSeparator = 0;
        boolean hasCursor = false;
        int length;

        private Formatter(CharSequence s, int cursor) {
            this.s = s;
            currentIndex = cursor - 1;
            length = s.length();
        }

        private String reformat() {
            String formatted;
            formatter.clear();
            lastNonSeparator = 0;
            hasCursor = false;

            if (s.length() == 1 && s.charAt(0) != '+') {
                formatted = formatStartSymbol();
            } else {
                formatted = formatBasicString();
            }

            return formatted;
        }

        private String formatStartSymbol() {
            String formatted;
            formatter.inputDigitAndRememberPosition('+');
            formatted = formatter.inputDigitAndRememberPosition(s.charAt(0));

            return formatted;
        }

        private String formatBasicString() {
            String result = null;
            for (int i = 0; i < length; i++) {
                result = processSymbol(s.charAt(i), i, result);
            }

            if (lastNonSeparator != 0) {
                result = getFormattedNumber(lastNonSeparator, hasCursor);
            }

            return result;
        }

        private String processSymbol(char c, int i, String current) {
            String result = current;
            if (PhoneNumberUtils.isNonSeparator(c)) {
                result = processSeparator(c, current);
            }

            if (i == currentIndex) {
                hasCursor = true;
            }

            return result;
        }

        private String processSeparator(char c, String current) {
            String result = current;
            if (lastNonSeparator != 0) {
                result = getFormattedNumber(lastNonSeparator, hasCursor);
                hasCursor = false;
            }
            lastNonSeparator = c;

            return result;
        }

        private String getFormattedNumber(char lastNonSeparator, boolean hasCursor) {
            return hasCursor ? formatter.inputDigitAndRememberPosition(lastNonSeparator) : formatter.inputDigit(lastNonSeparator);
        }
    }
}
