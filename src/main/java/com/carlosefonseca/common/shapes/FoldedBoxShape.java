package com.carlosefonseca.common.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.Shape;

import static com.carlosefonseca.common.utils.ImageUtils.dp2px;


/**
 * Creates a balloon shape like those on certain messaging apps.
 */
public class FoldedBoxShape extends Shape {
    private final short mOption;
    private Path path;

    public static final short BL = 0;
    public static final short TR = 1;
    public static final short BR = 2;
    public static final short TL = 3;
    public static final short BOX_BOTTOM = 4;
    public static final short BOX_TOP = 5;

    public FoldedBoxShape() { this(BL); }

    public FoldedBoxShape(short option) { mOption = option;}

    //  All percentages are relative to the total height of the canvas.

    //  #################### |    |
    //  #################### |hb  |
    //  #################### |    |
    //  #################### |    |
    //   ##   |1dp                |height (100%)
    //    ##########  |           |
    //      ########  |           |
    //        ######  |h1 (25%)   |
    //          ####  |           |
    //            ##  |           |
    //  |--|10%
    //  |----------|30%

    @Override
    protected void onResize(float width, float height) {
        int point = dp2px(1);
        int hb = (int) (height * 0.75 - point); // box height

        boolean boxOnly = mOption == BOX_BOTTOM || mOption == BOX_TOP;

        boolean flipH = false;
        boolean flipV = false;

        switch (mOption) {
            case BR:
                flipH = true;
                break;
            case TR:
                flipH = true;
                flipV = true;
                break;
            case TL:
            case BOX_TOP:
                flipV = true;
                break;
            case BL:
            default:
        }

        path = new Path();
        path.moveTo(flipH ? width - 0 : 0, flipV ? height - 0 : 0);

        // @formatter:off
        path.lineTo(flipH ? width - width : width, flipV ? height - 0 : 0);     // right side
        path.lineTo(flipH ? width - width : width, flipV ? height - hb : hb);   // right side down
        if (!boxOnly) {
            path.lineTo(flipH ? width - height * 0.1f : height * 0.1f, flipV ? height - hb : hb);                     // middle top
            path.lineTo(flipH ? width - height * 0.1f : height * 0.1f, flipV ? height - (hb + point) : (hb + point)); // middle bottom
            path.lineTo(flipH ? width - height * .30f : height * .30f, flipV ? height - (hb + point) : (hb + point)); // lower right top
            path.lineTo(flipH ? width - height * .30f : height * .30f, flipV ? height - height : height);             // lower right bottom
        }
        path.lineTo(flipH ? width - 0 : 0, flipV ? height - hb : hb);               // lower left
        path.close();
        // @formatter:on
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(path, paint);
    }

    public static int getPaddingForHeight(float height) {
        return (int) (height * 0.25) + dp2px(1);
    }
}
