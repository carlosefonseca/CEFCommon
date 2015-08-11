package com.carlosefonseca.common.utils;

import android.graphics.Color;

import java.util.Random;

public class HSVColor {

    private float[] hsv;

    short HUE = 0;
    short SAT = 1;
    short VAL = 2;

    public HSVColor(int color) {
        hsv = new float[3];
        Color.colorToHSV(color, hsv);
    }

    public HSVColor(Random random) {
        this(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
    }

    /**
     * Adds value to each of the RGB channels. Keeps values inside the 0xFF limit.
     * @param value Value to add to all RGB channels. [0..255].
     */
    public HSVColor addRGB(int value) {
        int color = color();
        int argb = Color.argb(Color.alpha(color),
                              Math.min(0xFF, Color.red(color) + value),
                              Math.min(0xFF, Color.green(color) + value),
                              Math.min(0xFF, Color.blue(color) + value));
        Color.colorToHSV(argb, hsv);
        return this;
    }

    /**
     * @param value [0..359]
     */
    public HSVColor addHue(double value) {
        return add(HUE, value);
    }

    /**
     * @param value [0..1]
     */
    public HSVColor addSat(double value) {
        return add(SAT, value);
    }

    /**
     * @param value [0..1]
     */
    public HSVColor addValue(double value) {
        return add(VAL, value);
    }

    public HSVColor mulHue(double value) {
        return mul(HUE, value);
    }

    public HSVColor mulSat(double value) {
        return mul(SAT, value);
    }

    public HSVColor mulValue(double value) {
        return mul(VAL, value);
    }

    private HSVColor add(short field, double value) {
        hsv[field] = (float) Math.min(Math.max(0, hsv[field] + value), field == HUE ? 359 : 1);
        return this;
    }


    private HSVColor mul(short field, double value) {
        hsv[field] = (float) Math.min(Math.max(hsv[field] * value, 0), field == HUE ? 359 : 1);
        return this;
    }

    public float getHue() {
        return hsv[HUE];
    }

    public float getSat() {
        return hsv[SAT];
    }

    public float getValue() {
        return hsv[VAL];
    }

    public int color() {
        return Color.HSVToColor(hsv);
    }

    public HSVColor avoidWhite() {
        if (getValue() > 0.9 && getSat() < 0.1) {
            addSat(0.1);
            addValue(-0.1);
        }
        return this;
    }
}
