package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Log;
import com.carlosefonseca.common.utils.MSDrawable;
import com.carlosefonseca.common.utils.ResourceUtils;
import org.apache.commons.lang3.StringUtils;

import static com.carlosefonseca.common.utils.ImageUtils.dp2px;

public class CFNumberKeyboard extends TableLayout {

    private static final String TAG = CodeUtils.getTag(CFNumberKeyboard.class);
    public static int MARGIN = 1;

    private ImageButton backspace;

    public CFNumberKeyboard(Context context) {
        super(context);
        init();
    }

    public CFNumberKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private TextView textView;

    private void init() {
        if (!isInEditMode()) MARGIN = -dp2px(1);
        this.setStretchAllColumns(true);
        setBackgroundColor(Color.WHITE);

        // Numbers
        int number = 1;
        for (int i = 0; i < 3; i++) {
            TableRow tableRow = new TableRow(getContext());
            this.addView(tableRow);
            for (int j = 0; j < 3; j++) {
                addKey(number++, tableRow);
            }
        }

        // Empty
        TableRow tableRow = new TableRow(getContext());
        this.addView(tableRow);
        View view = new View(getContext());
        view.setEnabled(false);
        view.setBackgroundResource(R.drawable.keyboard_other_background);
        addCell(tableRow, view);

        // Zero
        addKey(0, tableRow);

        // Backspace
        backspace = new ImageButton(getContext());

        final Drawable drawable = new MSDrawable(getContext()).icon(R.drawable.backspace)
                                                              .disabledColor(Color.GRAY)
                                                              .build();

        backspace.setImageDrawable(drawable);
        backspace.setBackgroundResource(R.drawable.keyboard_other_background);
        backspace.setOnClickListener(keyboardBackspaceClick);
        backspace.setOnLongClickListener(keyboardBackspaceLongClick);
        backspace.setEnabled(false);
        addCell(tableRow, backspace);
    }

    private void addKey(int number, TableRow tableRow) {
        final Button button = new Button(getContext());
        button.setBackgroundResource(R.drawable.keyboard_number_background);
        button.setText("" + number);
        ResourceUtils.setTextSize(button, R.dimen.keyboard_key_size);
        button.setOnClickListener(keyboardClick);
        addCell(tableRow, button);
    }

    private void addCell(TableRow tableRow, View cell) {
        tableRow.addView(cell);
        final ViewGroup.LayoutParams layoutParams1 = cell.getLayoutParams();
        final TableRow.LayoutParams layoutParams = (TableRow.LayoutParams) layoutParams1;
        layoutParams.height = dp2px(45);
        layoutParams.bottomMargin = MARGIN;
        layoutParams.rightMargin = MARGIN;
        cell.setLayoutParams(layoutParams);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final int oneDp = isInEditMode() ? 1 : dp2px(0.5);
        ((MarginLayoutParams) this.getLayoutParams()).leftMargin = -oneDp;
        ((MarginLayoutParams) this.getLayoutParams()).rightMargin = -oneDp;
    }

    private OnClickListener keyboardBackspaceClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (textView == null) {
                Log.w(TAG, "No textView associated");
                return;
            }
            final CharSequence text = textView.getText();
            if (text.length() > 0) textView.setText(new StringBuilder(text).deleteCharAt(text.length() - 1));
        }
    };

    OnLongClickListener keyboardBackspaceLongClick = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (textView == null) {
                Log.w(TAG, "No textView associated");
                return false;
            }
            textView.setText("");
            return true;
        }
    };

    OnClickListener keyboardClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (textView == null) {
                Log.w(TAG, "No textView associated");
                return;
            }
            final CharSequence text = textView.getText();
            final CharSequence number = ((Button) v).getText();
            if (StringUtils.isNotBlank(number)) textView.setText(new StringBuilder(text).append(number));
        }
    };

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        if (this.textView != null) {
            this.textView.removeTextChangedListener(textWatcher);
        }
        this.textView = textView;
        this.textView.addTextChangedListener(textWatcher);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            backspace.setEnabled(s.length() != 0);
        }
    };


    public void setTypeFace(Typeface typeFace) {
        final int rowCount = getChildCount();
        for (int r = 0; r < rowCount; r++) {
            final ViewGroup row = (ViewGroup) getChildAt(r);
            int colCount = row.getChildCount();
            for (int c = 0; c < colCount; c++) {
                final View cell = row.getChildAt(c);
                if (cell instanceof Button) {
                    ((Button) cell).setTypeface(typeFace);
                }
            }
        }
    }
}
