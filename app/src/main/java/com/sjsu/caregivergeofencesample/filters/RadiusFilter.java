package com.sjsu.caregivergeofencesample.filters;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

/**
 * Filter class to set the min and max values for the radius
 * Created by Savio on 4/17/2017.
 */

public class RadiusFilter implements InputFilter{

    private static final int MIN = 15,
                             MAX = 100;

    private final String TAG = getClass().getSimpleName();


    /**
     * This method is called when the buffer is going to replace the
     * range <code>dstart &hellip; dend</code> of <code>dest</code>
     * with the new text from the range <code>start &hellip; end</code>
     * of <code>source</code>.  Return the CharSequence that you would
     * like to have placed there instead, including an empty string
     * if appropriate, or <code>null</code> to accept the original
     * replacement.  Be careful to not to reject 0-length replacements,
     * as this is what happens when you delete text.  Also beware that
     * you should not attempt to make any changes to <code>dest</code>
     * from this method; you may only examine it for context.
     * <p>
     * Note: If <var>source</var> is an instance of {@link Spanned} or
     * {@link android.text.Spannable}, the span objects in the <var>source</var> should be
     * copied into the filtered result (i.e. the non-null return value).
     * {@link android.text.TextUtils#copySpansFrom} can be used for convenience.
     *
     * @param source
     * @param start
     * @param end
     * @param dest
     * @param dstart
     * @param dend
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                               int dstart, int dend) {
        try {
            //TODO fix the values received in the filter
            Log.d(TAG, "Filtered value : "+dest.toString());
            int input = Integer.parseInt(dest.toString());
            if (!isInRange(input))
                return "";
            return dest.toString();
        } catch (NumberFormatException nfe) {
            Log.e(TAG, nfe.getMessage());
        }
        return "";
    }

    private boolean isInRange(int c) {
        return c >= MIN && c <= MAX;
    }
}
