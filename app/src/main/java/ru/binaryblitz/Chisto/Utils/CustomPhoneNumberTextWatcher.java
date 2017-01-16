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

    private boolean mSelfChange = false;
    private AsYouTypeFormatter mFormatter;

    public CustomPhoneNumberTextWatcher() {
        this(Locale.getDefault().getCountry());
    }

    public CustomPhoneNumberTextWatcher(String countryCode) {
        if (countryCode == null) throw new IllegalArgumentException();
        mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public synchronized void afterTextChanged(Editable s) {
        if (mSelfChange) return;

        String formatted = reformat(s, Selection.getSelectionEnd(s));
        if (formatted != null) {
            int rememberedPos = mFormatter.getRememberedPosition();
            mSelfChange = true;
            s.replace(0, s.length(), formatted, 0, formatted.length());
            if (formatted.equals(s.toString())) Selection.setSelection(s, rememberedPos);
            mSelfChange = false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PhoneNumberUtils.addTtsSpan(s, 0, s.length());
    }

    private String reformat(CharSequence s, int cursor) {
        int curIndex = cursor - 1;
        String formatted = null;
        mFormatter.clear();
        char lastNonSeparator = 0;
        boolean hasCursor = false;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor);
                    hasCursor = false;
                }
                lastNonSeparator = c;
            }
            if (i == curIndex) hasCursor = true;
        }
        if (lastNonSeparator != 0) formatted = getFormattedNumber(lastNonSeparator, hasCursor);

        return formatted;
    }

    private String getFormattedNumber(char lastNonSeparator, boolean hasCursor) {
        return hasCursor ? mFormatter.inputDigitAndRememberPosition(lastNonSeparator) : mFormatter.inputDigit(lastNonSeparator);
    }
}