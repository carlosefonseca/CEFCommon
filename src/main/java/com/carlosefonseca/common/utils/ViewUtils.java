package com.carlosefonseca.common.utils;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public final class ViewUtils {
    private ViewUtils() {}

    public static void setPadding(int px, View... views) {
        for (View view : views) view.setPadding(px, px, px, px);
    }

    public static void setPaddingTop(View view, int value) {
        view.setPadding(view.getPaddingLeft(), value, view.getPaddingRight(), view.getPaddingBottom());
    }

    public static void setPaddingLeft(View view, int value) {
        view.setPadding(value, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
    }

    public static void setPaddingRight(View view, int value) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), value, view.getPaddingBottom());
    }

    public static void setPaddingBottom(View view, int value) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), value);
    }

    /**
     * Add value to Padding. Does getPaddingX() + X.
     */
    public static void setPaddingRelative(View view, int left, int top, int right, int bottom) {
        view.setPadding(view.getPaddingLeft() + left,
                        view.getPaddingTop() + top,
                        view.getPaddingRight() + right,
                        view.getPaddingBottom() + bottom);
    }

    public static class ButtonHighlighterOnTouchListener implements View.OnTouchListener {

        private static final int TRANSPARENT_GREY = Color.argb(0, 185, 185, 185);
        private static final int FILTERED_GREY = Color.argb(155, 185, 185, 185);

        ImageView imageView;
        TextView textView;

        public ButtonHighlighterOnTouchListener(final ImageView imageView) {
            super();
            this.imageView = imageView;
        }

        public ButtonHighlighterOnTouchListener(final TextView textView) {
            super();
            this.textView = textView;
        }

        @Override
        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            if (imageView != null) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    imageView.setColorFilter(FILTERED_GREY);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    imageView.setColorFilter(TRANSPARENT_GREY); // or null
                }
            } else {
                Drawable background = textView.getBackground();
                if (background != null) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        background.setColorFilter(FILTERED_GREY, PorterDuff.Mode.SRC_ATOP);
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        background.setColorFilter(TRANSPARENT_GREY, PorterDuff.Mode.SRC_ATOP); // or null
                    }
                }
                for (final Drawable compoundDrawable : textView.getCompoundDrawables()) {
                    if (compoundDrawable != null) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            // we use PorterDuff.Mode. SRC_ATOP as our filter color is already transparent
                            // we should have use PorterDuff.Mode.LIGHTEN with a non transparent color
                            compoundDrawable.setColorFilter(FILTERED_GREY, PorterDuff.Mode.SRC_ATOP);
                        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            compoundDrawable.setColorFilter(TRANSPARENT_GREY, PorterDuff.Mode.SRC_ATOP); // or null
                        }
                    }
                }
            }
            return false;
        }

    }

    public static class ButtonDarkenOnTouchListener implements View.OnTouchListener {

        private static final int NORMAL = Color.TRANSPARENT;
        private static final int FILTERED_GREY = Color.argb(155, 0, 0, 0);

        final ImageView imageView;
        final TextView textView;
        final View view;

        public ButtonDarkenOnTouchListener(ImageView imageView) {
            super();
            this.imageView = imageView;
            textView = null;
            view = null;
        }

        public ButtonDarkenOnTouchListener(TextView textView) {
            super();
            this.textView = textView;
            imageView = null;
            view = null;
        }

        public ButtonDarkenOnTouchListener(View view) {
            super();
            this.view = view;
            imageView = null;
            textView = null;
        }

        @Override
        public boolean onTouch(final View ignored, final MotionEvent motionEvent) {
            if (imageView != null) {
                changeColorFilter(motionEvent, imageView);
            } else if (textView != null) {
                changeBackground(motionEvent, textView);
                changeCompoundDrawables(motionEvent, textView);
            } else if (view != null) {
                changeBackground(motionEvent, view);
            }
            return false;
        }

        protected static void changeColorFilter(MotionEvent motionEvent, ImageView imageView) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setColorFilter(FILTERED_GREY);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP ||
                       motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                imageView.setColorFilter(NORMAL); // or null
            }
        }

        protected static void changeBackground(MotionEvent motionEvent, View view) {
            Drawable background = view.getBackground();
            if (background != null) {
                changeDrawable(motionEvent, background);
            }
        }

        protected static void changeCompoundDrawables(MotionEvent motionEvent, TextView textView) {
            for (final Drawable compoundDrawable : textView.getCompoundDrawables()) {
                if (compoundDrawable != null) changeDrawable(motionEvent, compoundDrawable);
            }
        }

        private static void changeDrawable(MotionEvent motionEvent, Drawable background) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                background.setColorFilter(FILTERED_GREY, PorterDuff.Mode.SRC_ATOP);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP ||
                       motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                background.setColorFilter(null); // or null
            }
        }

    }
}
