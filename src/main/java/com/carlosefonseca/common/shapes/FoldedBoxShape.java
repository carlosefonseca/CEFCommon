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

    public static final short FOLD_DOWN = 0;
    public static final short FOLD_UP = 1;
    public static final short FOLD_DOWN_BOX_ONLY = 2;
    public static final short FOLD_UP_BOX_ONLY = 3;

    public FoldedBoxShape() { this(FOLD_DOWN); }

    public FoldedBoxShape(short option) { mOption = option;}

    //  All percentages are relative to the total height of the canvas.

    //  ####################      |
    //  ####################      |
    //  ####################      |
    //  ####################      |
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
        if (mOption == FOLD_DOWN || mOption == FOLD_DOWN_BOX_ONLY) {
            int point = dp2px(1);
            int h1 = (int) (height * 0.25);
            int hb = (int) (height - h1 - point);

            path = new Path();
            path.moveTo(0, 0);

            path.lineTo(width, 0);                  // right side
            path.lineTo(width, hb);                 // right side down
            if (mOption == FOLD_DOWN) {
                path.lineTo(height * 0.1f, hb);         // middle top
                path.lineTo(height * 0.1f, hb + point); // middle bottom
                path.lineTo(height * .3f, hb + point);  // lower right top
                path.lineTo(height * .3f, height);      // lower right bottom
            }
            path.lineTo(0, hb);                     // lower left
            path.close();
        } else {
            int point = dp2px(1);
            int h1 = (int) (height * 0.25);
            int hb = (int) (height - h1 - point);

            //noinspection UnnecessaryLocalVariable
            float iWidth = width;
            //noinspection UnnecessaryLocalVariable
            float iHeight = height;

            path = new Path();
            path.moveTo(iWidth - 0, iHeight - 0);

            path.lineTo(iWidth - width, iHeight - 0);                  // right side
            path.lineTo(iWidth - width, iHeight - hb);                 // right side down
            if (mOption == FOLD_UP) {
                path.lineTo(iWidth - height * 0.1f, iHeight - hb);         // middle top
                path.lineTo(iWidth - height * 0.1f, iHeight - (hb + point)); // middle bottom
                path.lineTo(iWidth - height * .3f, iHeight - (hb + point));  // lower right top
                path.lineTo(iWidth - height * .3f, iHeight - height);      // lower right bottom
            }
            path.lineTo(iWidth - 0, iHeight - hb);                     // lower left
            path.close();
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(path, paint);
    }

    public static int getPaddingForHeight(float height) {
        return (int) (height * 0.25) + dp2px(1);
    }
}
