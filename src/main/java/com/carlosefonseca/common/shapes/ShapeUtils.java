package com.carlosefonseca.common.shapes;

import android.graphics.drawable.shapes.RoundRectShape;

public final class ShapeUtils {
    private ShapeUtils() {}

    public static RoundRectShape makeRoundRectShape(int radius) {
        return new RoundRectShape(new float[]{radius, radius, radius, radius, radius, radius, radius, radius},
                                  null,
                                  null);
    }
}
