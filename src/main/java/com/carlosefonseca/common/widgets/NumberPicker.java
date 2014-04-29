package com.carlosefonseca.common.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.carlosefonseca.common.R;

public class NumberPicker extends LinearLayout {

    private final EditText textView;
    private Button minusBT, plusBT;
    private final NumberPicker view;
    int value = 0;
    int input;
    int filteredInput;

    int min = 0;
    int max = Integer.MAX_VALUE;

    int id = Integer.MIN_VALUE;
    NumberPickerDelegate delegate;

    public NumberPicker(Context context) {
        this(context, null);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = (NumberPicker) layoutInflater.inflate(R.layout.cf_number_picker, this);

        minusBT = (Button) view.findViewById(R.id.minus);
        plusBT = (Button) view.findViewById(R.id.plus);

        if (!view.isInEditMode()) {
            minusBT.setOnClickListener(minus_action);
            plusBT.setOnClickListener(plus_action);
        }

        textView = (EditText) findViewById(R.id.value);
        if (!view.isInEditMode()) {
            textView.addTextChangedListener(text_action);
            textView.setOnKeyListener(key_listener);

            // Digits only
            textView.setFilters(new InputFilter[]{filter});
        }
    }


    /*
        CONFIG
     */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NumberPicker setDelegate(NumberPickerDelegate delegate) {
        this.delegate = delegate;
        return this;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        updateNumberDisplay();
    }


    /*
        ACTIONS
     */

    OnClickListener minus_action = new OnClickListener() {
        @Override
        public void onClick(View view) {
            value = Math.max(--value, min);
            updateNumberDisplay();
            notifyValueChange();
        }
    };

    OnClickListener plus_action = new OnClickListener() {
        @Override
        public void onClick(View view) {
            value = Math.min(++value, max);
            updateNumberDisplay();
            notifyValueChange();
        }
    };

    TextWatcher text_action = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            textChanged(arg0);
        }


        @Override
        public void afterTextChanged(Editable editable) {
            if (input != filteredInput) {
                editable.clear();
                editable.append(""+ filteredInput);
                filteredInput = 0;
                input = 0;
            }
        }
    };

    private void textChanged(CharSequence text) {
        if (text.length() == 0) {
            value = 0;
        } else {
            input = Integer.parseInt(String.valueOf(text));
            filteredInput = Math.min(input, max);
            filteredInput = Math.max(filteredInput, min);
            value = filteredInput;
        }
        notifyValueChange();
    }

    /**
     * Handles Backspace.
     */
    OnKeyListener key_listener = new OnKeyListener() {
        @Override
        public boolean onKey(final View view, int keyCode, KeyEvent keyEvent) {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textChanged(((EditText) view).getText());
                    }
                },10);
            }
            return false;
        }
    };

    private void updateNumberDisplay() {
        textView.setText(""+value);
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        minusBT.setEnabled(enabled);
        plusBT.setEnabled(enabled);
        textView.setEnabled(enabled);
    }

    /*
            DELEGATE
         */
    public interface NumberPickerDelegate {
        public void valueChanged(int value);
    }

    public void notifyValueChange() {
        if (delegate != null) {
            delegate.valueChanged(value);
        }
    }

    /*
        STUFF
     */

    final InputFilter filter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

}