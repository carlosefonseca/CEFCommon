package com.carlosefonseca.common.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import org.apache.commons.lang3.StringUtils;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * CheckBox that binds itself to a static boolean field on a class, like flags on a config class.
 * <p/>
 * To use it, just set the class and the field name in the android:tag xml attribute in the format 'com.example.Class:fieldName'.
 * The widget will initialize itself with the current value of the field and will update the field when the checked status
 * changes.
 * <p/>
 * The widget tries to find getter and setter methods for the field in the formats isFieldName or getFieldName and setFieldName.
 * It will use the ones that are found and perform direct field access when it can't find the method.
 * <p/>
 * If the {@link #setOnCheckedChangeListener(android.widget.CompoundButton.OnCheckedChangeListener)} is set, it will be executed
 * after field is set.
 */
public class BindedCheckBox extends CheckBox {
    private static final String TAG = CodeUtils.getTag(BindedCheckBox.class);
    private Field field;
    private Method setMethod;
    private Method getMethod;
    private Boolean currentState;
    private OnCheckedChangeListener userListener;

    public BindedCheckBox(Context context) {
        super(context);
        init();
    }

    public BindedCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BindedCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (isInEditMode()) return;
        String[] split = new String[0];
        try {
            split = ((String) getTag()).split(":");
        } catch (Exception e) {
            Log.e(TAG, "The tag must be in the format 'com.example.Class:fieldName'. Currently it's " + getTag(), e);
            return;
        }

        try {
            Class<?> aClass = Class.forName(split[0]);
            field = aClass.getDeclaredField(split[1]);

            String capitalized = StringUtils.capitalize(split[1]);
            try {
                setMethod = aClass.getMethod("set" + capitalized, Boolean.TYPE);
            } catch (NoSuchMethodException e) {
                try {
                    setMethod = aClass.getMethod("set" + capitalized, Boolean.class);
                } catch (NoSuchMethodException e1) {
//                    Log.e(TAG, "" + e1.getMessage(), e1);
                }
//                Log.w(TAG, "Setter for " + split[1] + " wasn't found. Will access field.");
            }


            try {
                getMethod = aClass.getMethod("is" + capitalized);
            } catch (NoSuchMethodException e1) {
                try {
                    getMethod = aClass.getMethod("get" + capitalized);
                } catch (NoSuchMethodException e2) {
                    try {
                        getMethod = aClass.getMethod(split[1]);
                    } catch (NoSuchMethodException e3) {
//                    Log.w(TAG, "Getter for " + split[1] + " wasn't found. Will access field.");
                    }
                }
            }

            setInitialState();

            super.setOnCheckedChangeListener(listener);

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "" + e.getMessage(), e);
        }
    }

    private void setInitialState() {
        if (field != null) {
            try {
                if (getMethod != null) {
                    currentState = (Boolean) getMethod.invoke(null, (Object[]) null);
                } else {
                    currentState = (Boolean) field.get(null);
                }
                setChecked(currentState);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "" + e.getMessage(), e);
            } catch (InvocationTargetException e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }
    }


    private OnCheckedChangeListener listener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            currentState = b;
            if (field != null) {
                try {
                    if (setMethod != null) {
                        setMethod.invoke(null, b);
                    } else {
                        field.setBoolean(null, b);
                    }
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "" + e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    Log.e(TAG, "" + e.getMessage(), e);
                }
            }
            if (userListener != null) {
                userListener.onCheckedChanged(compoundButton, b);
            }
        }
    };

    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.userListener = listener;
    }
}
