package com.carlosefonseca.common.widgets;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.carlosefonseca.common.R;
import com.carlosefonseca.common.utils.CodeUtils;
import com.carlosefonseca.common.utils.Log;
import com.carlosefonseca.common.utils.ResourceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Navigation Bar for MySight.
 * <p/>
 * A basic navigation bar can be added to any activity by adding a NavBar element on the XML and use the
 * {@code android:text} attribute to set the title of the bar (or use the {@link #setTitle(CharSequence)}).
 * A gray bar with a back button and the title will appear.
 * <p/>
 * The bar can have its background color changed by using the XML tag {@code android:color}, by using the
 * {@link #setColor(int)} method of by implementing the interface {@link CustomChromeColorDelegate} (the interface has
 * a requirement that might lower its interest, it's in testing). Setting the color will also automagically change the
 * onPressed color of the buttons to a darker color.
 * <p/>
 * Extra buttons can be added to the right side of the bar either by setting them on XML of by using the {@link
 * #addView(android.view.View, android.view.ViewGroup.LayoutParams)}. A 1dp white margin will be added to the element
 * as well as the background color.
 */
@SuppressWarnings("UnusedDeclaration")
public class NavBar extends LinearLayout {
    // The value to subtract to the color's HSV Value.
    public static final double DARKNESS = 0.25;
    private static final String TAG = CodeUtils.getTag(NavBar.class);
    public static final int NO_COLOR = 0;
    private static Translator translator;

    // Image for the back button
//    public static int BACK_BUTTON = R.drawable.back_button;
//    public static boolean GAP = true;
//    public static int DEFAULT_COLOR = R.color.DarkerGray;
//    public static int DEFAULT_PRESSED_COLOR = R.color.DimGray;

    private static Drawable defaultBackDrawable;
    private static Drawable defaultTitleDrawable;
    private static int defaultNormalColor = NO_COLOR;
    private static int defaultPressedColor = NO_COLOR;
    private static int defaultTextColor = NO_COLOR;

    // ELEMENTS
    private ImageButton backBt;
    private AutoResizeTextView titleView;

    // STYLE STUFF
    static Typeface typeface;
    // Normal color
    private int color = NO_COLOR;
    private int textColor = NO_COLOR;
    // Pressed state color
    private int pressedColor;

    private String xmlTitle;

    private LinearLayout root;
    private Drawable backDrawable;
    private Drawable titleDrawable;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int getColorForStatusBar() {
        if (color != NO_COLOR) return color;
        else if (defaultNormalColor != NO_COLOR) return defaultNormalColor;
        else {
            Drawable background = titleView.getBackground();
            if (background != null && background.getCurrent() instanceof ColorDrawable) {
                ColorDrawable current = (ColorDrawable) background.getCurrent();
                return current.getColor();
            } else {
                Log.w(TAG, new RuntimeException("Don't know what color to return."));
            }
        }
        return 0;
    }

    public static void setDefaultTitleDrawable(Drawable t) {
        defaultTitleDrawable = t;
    }

    /**
     * Implement this interface on the activities where you want a different color tint.
     * <p/>
     * It only works if it can tell the color BEFORE the activity calls {@code setContentView}.
     */
    public interface CustomChromeColorDelegate {
        int getColor();
    }

    public NavBar(Context context) {
        this(context, null, 0);
    }

    public NavBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) getXmlProperties(attrs);
        viewSetup(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public NavBar(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) getXmlProperties(attrs);
        viewSetup(context);
    }

    /**
     * Obtains the custom XML properties for the NavBar.
     */
    private void getXmlProperties(AttributeSet attrs) {
        assert getContext() != null;
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NavBar);

        if (a == null) return;

        xmlTitle = a.getString(R.styleable.NavBar_android_text);

        if (xmlTitle != null && !isInEditMode() && translator != null) {
            xmlTitle = translator.translate(xmlTitle);
        }

        textColor = a.getColor(R.styleable.NavBar_android_textColor, NO_COLOR);

        color = a.getColor(R.styleable.NavBar_android_color, NO_COLOR);
        if (color != NO_COLOR) {
            pressedColor = computeDarkerColor(color);
        }

        backDrawable = a.getDrawable(R.styleable.NavBar_android_drawableLeft);
        if (backDrawable == null) backDrawable = defaultBackDrawable;
//        if (defaultTitleDrawable != null) titleDrawable = defaultTitleDrawable;
        a.recycle();
    }

    /**
     * Loads the views and does some setup.
     */
    private void viewSetup(final Context context) {
        // Check if the context is an activity that can tell the chrome color.
        if (context instanceof CustomChromeColorDelegate) {
            this.color = ((CustomChromeColorDelegate) context).getColor();
            this.pressedColor = computeDarkerColor(color);
        }

        //Inflate and attach your child XML
        LayoutInflater.from(context).inflate(R.layout.nav_bar, this);

        this.root = (LinearLayout) getChildAt(0);

        // TITLE
        titleView = (AutoResizeTextView) findViewById(R.id.nav_title);
        if (titleView != null) {
            if (typeface != null) titleView.setTypeface(typeface);
            titleView.setMinTextSize(titleView.getTextSize() * 0.7f);

            if (textColor != NO_COLOR) titleView.setTextColor(textColor);
            else if (defaultTextColor != NO_COLOR) titleView.setTextColor(defaultTextColor);
        }
        if (xmlTitle != null) setTitle(xmlTitle);

        // BACK
        backBt = (ImageButton) findViewById(R.id.back_bt);
        if (backDrawable != null) setBackButtonDrawable(backDrawable);
        if (!isInEditMode()) {
            backBt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Activity) context).finish();
                }
            });
        }
        if ((color != NO_COLOR || defaultNormalColor != NO_COLOR) && defaultTitleDrawable == null) {
            setColor();
        } else if (defaultTitleDrawable != null) {
            if (defaultTitleDrawable instanceof ShapeDrawable) {
                setTitleDrawable(new ShapeDrawable(((ShapeDrawable) defaultTitleDrawable).getShape()));
            } else {
                setTitleDrawable(defaultTitleDrawable.getConstantState().newDrawable());
            }
            setColor();
        }
    }

    private void setTitleDrawable(Drawable titleDrawable) {
        this.titleDrawable = titleDrawable;
        ResourceUtils.setBackground(titleView, this.titleDrawable);
    }

    public void setBackButtonDrawable(Drawable drawable) {backBt.setImageDrawable(drawable);}

    public interface Translator {
        String translate(String text);
    }

    public static void setTranslator(Translator translator) {
        NavBar.translator = translator;
    }

    /**
     * The currently set TypeFace
     */
    public static Typeface getDefaultTypeface() {
        return typeface;
    }

    /**
     * Globally set the TypeFace for all the future NavBars.
     *
     * The class is currently using OmnesBold.
     * @param typeface The typeface to use.
     */
    public static void setDefaultTypeface(Typeface typeface) {
        NavBar.typeface = typeface;
    }

    /**
     * Sets the TypeFace for this instance of NavBar
     * @param typeface The typeface to use.
     */
    public void setTypeface(Typeface typeface) {
        titleView.setTypeface(typeface);
    }

    /**
     * Sets a new color tint for all current and future NavBar elements.
     * @param color A Color.
     */
    public NavBar setColor(int color) {
        this.color = color;
        this.pressedColor = computeDarkerColor(color);

        setColor();
        return this;
    }

    private void setColor() {
        final int color1 = color != NO_COLOR ? color : defaultNormalColor;
        final int color2 = color != NO_COLOR ? pressedColor : defaultPressedColor;

        if (color1 == NO_COLOR && color2 == NO_COLOR) {
            return;
        }

        if (titleDrawable != null) {
            if (titleDrawable instanceof ShapeDrawable) ((ShapeDrawable) titleDrawable).getPaint().setColor(color1);
        } else {
            titleView.setBackgroundColor(color1);
        }
        ResourceUtils.setDualColorBackground(backBt, color1, color2, ResourceUtils.PRESSED);

        for (int i = 2; i < root.getChildCount(); i++) {
            ResourceUtils.setDualColorBackground(root.getChildAt(i), color1, color2, ResourceUtils.PRESSED);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        root.setBackgroundColor(color);
    }

    public NavBar setTextColor(int color) {
        this.textColor = color;
        titleView.setTextColor(this.textColor);
        return this;
    }

    public TextView getTitleView() {
        return titleView;
    }

    /**
     * Takes a color, extracts it's HSV values and reduces the Value component by some amount.
     * @param color The original color.
     * @return The new, darker color.
     */
    private static int computeDarkerColor(int color) {
        return ResourceUtils.computeDarkerColor(color, DARKNESS);
    }

    /**
     * Returns the currently set color if set.
     * @return Returns the currently set color or NO_COLOR if the default color is being used.
     */
    public int getColor() {
        return color;
    }

    /**
     * Returns the color in use, either current color or default color.
     * @return Returns the currently set color or the default color if color is NO_COLOR
     */
    public int getColorInUse() {
        return color != NO_COLOR ? color : defaultNormalColor;
    }

    /**
     * Returns the currently set text color if set.
     * @return Returns the currently set text color or NO_COLOR if the default color is being used.
     */
    public int getTextColor() {
        return textColor;
    }



    /**
     * Adds a right side button to the navbar. The other "addView" methods do the same as this.
     * <p/>
     * REMOVED: "Sets the margin and color on the view and appends it to the end of the NavBar."<br/>
     * Style the button yourself. Use a style or something.
     * <p/>
     * Please remember that if you use images with background, the custom background might not be visible.
     */
    @Override
    public void addView(@NotNull View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() == 0 && child.getId() == R.id.nav_bar_root) {
            super.addView(child, index, params);
        } else {
            root.addView(child, index, params);
        }
    }

    public void setBackgroundOnView(View v) {
        final int color1 = color != NO_COLOR ? color : defaultNormalColor;
        final int color2 = color != NO_COLOR ? pressedColor : defaultPressedColor;

        ResourceUtils.setDualColorBackground(v, color1, color2, ResourceUtils.PRESSED);
    }

    /**
     * Sets the title text for this NavBar.
     */
    public NavBar setTitle(CharSequence title) {
        if (titleView != null) titleView.setText(title == null ? "" : title);
        return this;
    }

    public NavBar setBackButtonClickListener(OnClickListener listener) {
        backBt.setOnClickListener(listener);
        return this;
    }


    public static Drawable getDefaultBackDrawable() {
        return defaultBackDrawable;
    }

    public static void setDefaultBackDrawable(Drawable defaultBackDrawable) {
        NavBar.defaultBackDrawable = defaultBackDrawable;
    }

    public static int getDefaultNormalColor() {
        return defaultNormalColor;
    }

    public static void setDefaultColor(int normalColor) {
        setDefaultColors(normalColor, computeDarkerColor(normalColor));
    }

    public static void setDefaultColors(int normalColor, int pressedColor) {
        defaultNormalColor = normalColor;
        defaultPressedColor = pressedColor;
    }

    public static void setDefaultTextColor(int defaultTextColor) {
        NavBar.defaultTextColor = defaultTextColor;
    }
}
