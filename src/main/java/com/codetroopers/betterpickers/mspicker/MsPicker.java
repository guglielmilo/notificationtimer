package com.codetroopers.betterpickers.mspicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codetroopers.betterpickers.R;

public class MsPicker extends LinearLayout implements Button.OnClickListener, Button.OnLongClickListener {

    protected int mInputSize = 4;
    protected final Button mNumbers[] = new Button[10];
    protected int mInput[] = new int[mInputSize];
    protected int mInputPointer = -1;
    protected ImageButton mDelete;
    protected Button mLeft, mRight;
    protected MsView mEnteredMs;
    protected final Context mContext;

    private TextView mMinutesLabel, mSecondsLabel;
    private Button mSetButton;

    protected View mDivider;
    private ColorStateList mTextColor;
    private int mKeyBackgroundResId;
    private int mButtonBackgroundResId;
    private int mDividerColor;
    private int mDeleteDrawableSrcResId;
    private int mTheme = -1;

    private int mSign;
    public static final int SIGN_POSITIVE = 0;
    public static final int SIGN_NEGATIVE = 1;

    /**
     * Instantiates an MsPicker object
     *
     * @param context the Context required for creation
     */
    public MsPicker(Context context) {
        this(context, null);
    }

    /**
     * Instantiates an MsPicker object
     *
     * @param context the Context required for creation
     * @param attrs   additional attributes that define custom colors, selectors, and backgrounds.
     */
    public MsPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(getLayoutId(), this);

        // Init defaults
        mTextColor = getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
        mKeyBackgroundResId = R.drawable.key_background_dark;
        mButtonBackgroundResId = R.drawable.button_background_dark;
        mDividerColor = getResources().getColor(R.color.default_divider_color_dark);
        mDeleteDrawableSrcResId = R.drawable.ic_backspace_dark;
    }

    protected int getLayoutId() {
        return com.simpleworkout.timer.R.layout.ms_picker_view;
    }

    /**
     * Change the theme of the Picker
     *
     * @param themeResId the resource ID of the new style
     */
    public void setTheme(int themeResId) {
        mTheme = themeResId;
        if (mTheme != -1) {
            TypedArray a = getContext().obtainStyledAttributes(themeResId, R.styleable.BetterPickersDialogFragment);
            mTextColor = a.getColorStateList(R.styleable.BetterPickersDialogFragment_bpTextColor);
            mKeyBackgroundResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpKeyBackground,
                    mKeyBackgroundResId);
            mButtonBackgroundResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpButtonBackground,
                    mButtonBackgroundResId);
            mDividerColor = a.getColor(R.styleable.BetterPickersDialogFragment_bpDividerColor, mDividerColor);
            mDeleteDrawableSrcResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpDeleteIcon,
                    mDeleteDrawableSrcResId);
        }

        restyleViews();
    }

    private void restyleViews() {
        for (Button number : mNumbers) {
            if (number != null) {
                number.setTextColor(mTextColor);
                number.setBackgroundResource(mKeyBackgroundResId);
            }
        }
        if (mDivider != null) {
            mDivider.setBackgroundColor(mDividerColor);
        }
        if (mMinutesLabel != null) {
            mMinutesLabel.setTextColor(mTextColor);
            mMinutesLabel.setBackgroundResource(mKeyBackgroundResId);
        }
        if (mSecondsLabel != null) {
            mSecondsLabel.setTextColor(mTextColor);
            mSecondsLabel.setBackgroundResource(mKeyBackgroundResId);
        }
        if (mDelete != null) {
            mDelete.setBackgroundResource(mButtonBackgroundResId);
            mDelete.setImageDrawable(getResources().getDrawable(mDeleteDrawableSrcResId));
        }
        if (mEnteredMs != null) {
            mEnteredMs.setTheme(mTheme);
        }
        if (mLeft != null) {
            mLeft.setTextColor(mTextColor);
            mLeft.setBackgroundResource(mKeyBackgroundResId);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View v1 = findViewById(R.id.first);
        View v2 = findViewById(R.id.second);
        View v3 = findViewById(R.id.third);
        View v4 = findViewById(R.id.fourth);
        mEnteredMs = (MsView) findViewById(com.simpleworkout.timer.R.id.ms_text);
        mDelete = (ImageButton) findViewById(R.id.delete);
        mDelete.setOnClickListener(this);
        mDelete.setOnLongClickListener(this);

        mNumbers[1] = (Button) v1.findViewById(R.id.key_left);
        mNumbers[2] = (Button) v1.findViewById(R.id.key_middle);
        mNumbers[3] = (Button) v1.findViewById(R.id.key_right);

        mNumbers[4] = (Button) v2.findViewById(R.id.key_left);
        mNumbers[5] = (Button) v2.findViewById(R.id.key_middle);
        mNumbers[6] = (Button) v2.findViewById(R.id.key_right);

        mNumbers[7] = (Button) v3.findViewById(R.id.key_left);
        mNumbers[8] = (Button) v3.findViewById(R.id.key_middle);
        mNumbers[9] = (Button) v3.findViewById(R.id.key_right);

        mLeft = (Button) v4.findViewById(R.id.key_left);
        mNumbers[0] = (Button) v4.findViewById(R.id.key_middle);
        mRight = (Button) v4.findViewById(R.id.key_right);
        setRightEnabled(false);

        for (int i = 0; i < 10; i++) {
            mNumbers[i].setOnClickListener(this);
            mNumbers[i].setText(String.format("%d", i));
            mNumbers[i].setTag(R.id.numbers_key, new Integer(i));
        }
        updateMs();

        Resources res = mContext.getResources();
        mLeft.setText(res.getString(R.string.number_picker_plus_minus));
        mLeft.setOnClickListener(this);

        mMinutesLabel = (TextView) findViewById(R.id.minutes_label);
        mSecondsLabel = (TextView) findViewById(R.id.seconds_label);
        mDivider = findViewById(R.id.divider);

        restyleViews();
        updateKeypad();
    }

    /**
     * Update the 0 button to determine whether it is able to be clicked.
     */
    public void updateZeroButton() {
        boolean enabled = mInputPointer >= 0;
        if (mNumbers[0] != null) {
            mNumbers[0].setEnabled(enabled);
            mNumbers[0].setAlpha(enabled? (float) 1: (float) 0.3);
        }
    }

    /**
     * Update the delete button to determine whether it is able to be clicked.
     */
    public void updateDeleteButton() {
        boolean enabled = mInputPointer != -1;
        if (mDelete != null) {
            mDelete.setEnabled(enabled);
            mDelete.setAlpha(enabled? (float) 1: (float) 0.3);
        }
    }

    @Override
    public void onClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        doOnClick(v);
        updateDeleteButton();
        updateZeroButton();
    }

    protected void doOnClick(View v) {
        Integer val = (Integer) v.getTag(R.id.numbers_key);
        // A number was pressed
        if (val != null) {
            addClickedNumber(val);
        } else if (v == mDelete) {
            if (mInputPointer >= 0) {
                for (int i = 0; i < mInputPointer; i++) {
                    mInput[i] = mInput[i + 1];
                }
                mInput[mInputPointer] = 0;
                mInputPointer--;
            }
        } else if (v == mLeft) {
            onLeftClicked();
        }
        updateKeypad();
    }

    private void onLeftClicked() {
        if (isNegative()) {
            mSign = SIGN_POSITIVE;
        } else {
            mSign = SIGN_NEGATIVE;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        if (v == mDelete) {
            mDelete.setPressed(false);

            reset();
            updateKeypad();
            return true;
        }
        return false;
    }

    /**
     * Reset all inputs and the minutes:seconds.
     */
    public void reset() {
        for (int i = 0; i < mInputSize; i++) {
            mInput[i] = 0;
        }
        mInputPointer = -1;
        updateMs();
    }

    private void updateKeypad() {
        // Update the h:m:s
        updateMs();
        // enable/disable the "set" key
        enableSetButton();
        // Update the backspace button
        updateDeleteButton();
        // Update the zero button
        updateZeroButton();
    }

    /**
     * Update the time displayed in the picker:
     * <p/>
     * Put "-" in digits that was not entered by passing -1
     */
    protected void updateMs() {
        mEnteredMs.setTime(isNegative(), mInput[3], mInput[2], mInput[1], mInput[0]);
    }

    private void addClickedNumber(int val) {
        if (mInputPointer < mInputSize - 1) {
            for (int i = mInputPointer; i >= 0; i--) {
                mInput[i + 1] = mInput[i];
            }
            mInputPointer++;
            mInput[0] = val;
        }
    }

    /**
     * Enable/disable the "Set" button
     */
    private void enableSetButton() {
        if (mSetButton == null) {
            return;
        }

        // Nothing entered - disable
        if (mInputPointer == -1) {
            mSetButton.setEnabled(false);
            return;
        }

        mSetButton.setEnabled(mInputPointer >= 0);
    }

    /**
     * Expose the set button to allow communication with the parent Fragment.
     *
     * @param b the parent Fragment's "Set" button
     */
    public void setSetButton(Button b) {
        mSetButton = b;
        enableSetButton();
    }

    /**
     * Returns the minutes as currently inputted by the user.
     *
     * @return the inputted minutes
     */
    public int getMinutes() {
        return mInput[3] * 10 + mInput[2];
    }

    /**
     * Return the seconds as currently inputted by the user.
     *
     * @return the inputted seconds
     */
    public int getSeconds() {
        return mInput[1] * 10 + mInput[0];
    }

    /**
     * Using View.GONE, View.VISIBILE, or View.INVISIBLE, set the visibility of the plus/minus indicator
     *
     * @param visibility an int using Android's View.* convention
     */
    public void setPlusMinusVisibility(int visibility) {
        if (mLeft != null) {
            mLeft.setVisibility(visibility);
        }
    }

    /**
     * Set the current minutes, and seconds on the picker.
     *
     * @param minutes the input minutes value
     * @param seconds the input seconds value
     */
    public void setTime(int minutes, int seconds) {
        mInput[3] = minutes / 10;
        mInput[2] = minutes % 10;
        mInput[1] = seconds / 10;
        mInput[0] = seconds % 10;

        for (int i = 3; i >= 0; i--) {
            if (mInput[i] > 0) {
                mInputPointer = i;
                break;
            }
        }

        updateKeypad();
    }


    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable parcel = super.onSaveInstanceState();
        final SavedState state = new SavedState(parcel);
        state.mInput = mInput;
        state.mInputPointer = mInputPointer;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mInputPointer = savedState.mInputPointer;
        mInput = savedState.mInput;
        if (mInput == null) {
            mInput = new int[mInputSize];
            mInputPointer = -1;
        }
        updateKeypad();
    }

    private static class SavedState extends BaseSavedState {

        int mInputPointer;
        int[] mInput;
        int mAmPmState;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mInputPointer = in.readInt();
            mInput = in.createIntArray();
            mAmPmState = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mInputPointer);
            dest.writeIntArray(mInput);
            dest.writeInt(mAmPmState);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<MsPicker.SavedState>() {
            public MsPicker.SavedState createFromParcel(Parcel in) {
                return new MsPicker.SavedState(in);
            }

            public MsPicker.SavedState[] newArray(int size) {
                return new MsPicker.SavedState[size];
            }
        };
    }

    /**
     * Returns the time in seconds
     *
     * @return an int representing the time in seconds
     */
    public int getTime() {
        return mInput[3] * 600 + mInput[2] * 60 + mInput[1] * 10 + mInput[0];
    }

    public void saveEntryState(Bundle outState, String key) {
        outState.putIntArray(key, mInput);
    }

    public void restoreEntryState(Bundle inState, String key) {
        int[] input = inState.getIntArray(key);
        if (input != null && mInputSize == input.length) {
            for (int i = 0; i < mInputSize; i++) {
                mInput[i] = input[i];
                if (mInput[i] != 0) {
                    mInputPointer = i;
                }
            }
            updateMs();
        }
    }

    protected void setRightEnabled(boolean enabled) {
        mRight.setEnabled(enabled);
        if (!enabled) {
            mRight.setContentDescription(null);
        }
    }

    public boolean isNegative() {
        return mSign == SIGN_NEGATIVE;
    }
}
