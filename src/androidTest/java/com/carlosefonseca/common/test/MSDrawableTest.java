package com.carlosefonseca.common.test;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.carlosefonseca.common.utils.MSDrawable;
import com.carlosefonseca.common.utils.ResourceUtils;
import com.carlosefonseca.common.widgets.TriangleShape;


public class MSDrawableTest extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final LinearLayout linearLayout0 = new LinearLayout(this);
        linearLayout0.setOrientation(LinearLayout.HORIZONTAL);

        final LinearLayout linearLayout1 = new LinearLayout(this);
        linearLayout0.addView(linearLayout1, 100, ViewGroup.LayoutParams.MATCH_PARENT);

        final LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout0.addView(linearLayout2, 100, ViewGroup.LayoutParams.MATCH_PARENT);


        MSDrawable msDrawable = new MSDrawable(this).normalColor(Color.RED).pressedColor(Color.GREEN).selectedColor(Color.BLUE);

        ImageButton imageButton = new ImageButton(this);
        ResourceUtils.setBackground(imageButton, msDrawable.build());
        linearLayout1.addView(imageButton);

        imageButton = new ImageButton(this);
        ResourceUtils.setBackground(imageButton, msDrawable.build());
        linearLayout2.addView(imageButton);

        msDrawable = new MSDrawable(this).icon(new TriangleShape(TriangleShape.UP))
                                         .normalColor(Color.RED)
                                         .pressedColor(Color.GREEN)
                                         .selectedColor(Color.BLUE);

        imageButton = new ImageButton(this);
        ResourceUtils.setBackground(imageButton, msDrawable.build());
        linearLayout1.addView(imageButton);

        imageButton = new ImageButton(this);
        ResourceUtils.setBackground(imageButton, msDrawable.build());
        linearLayout2.addView(imageButton);


        setContentView(linearLayout0);
    }

}
