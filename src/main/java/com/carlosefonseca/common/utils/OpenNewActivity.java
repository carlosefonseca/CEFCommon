package com.carlosefonseca.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import com.carlosefonseca.common.R;
import org.jetbrains.annotations.Nullable;

/**
 * Creates a View.OnClickListener that starts a new activity.
 * <p/>
 * Sample usage:<br/>
 * {@code button.setOnClickListener(new OpenNewActivity(this, NewActivity.class);}
 * <p/>
 * It can also be used inside other code by calling<br/>
 * {@code new OpenNewActivity(this, NewActivity.class).go();} or start() or finishAndStart()
 */
@SuppressWarnings("UnusedDeclaration")
public class OpenNewActivity implements View.OnClickListener {

    private static final String TAG = CodeUtils.getTag(OpenNewActivity.class);
    private Integer flags;

    public enum TransitionAnimation {
        /**
         * The system transition.
         */
        DEFAULT,
        /**
         * A fade in/out transition.
         */
        FADE,
        /**
         * Don't use this. It's the internal indication for using custom enter/exit animations.
         */
        CUSTOM
    }

    private Context context;
    private Activity activity;
    private final Class aClass;
    private final Test test;
    private final boolean finish;

    private final TransitionAnimation animation;

    private int enterAnim;
    private int exitAnim;

    public interface Test {
        boolean isValid(@Nullable View view);
    }

    /** A convenience method for just opening an activity, quick and simple. */
    public static void start(Activity context, Class aClass) {
        new OpenNewActivity(context, aClass, false).go(null);
    }

    /** A convenience method for just opening an activity, quick and simple, and finishing the current one. */
    public static void finishAndStart(Activity activityToFinish, Class aClass) {
        new OpenNewActivity(activityToFinish, aClass, true).go(null);
    }

    /** A convenience method for just opening an activity, quick and simple, and finishing the current one. */
    public static void finishAndStartFade(Activity activityToFinish, Class aClass) {
        new OpenNewActivity(activityToFinish, aClass, true, TransitionAnimation.FADE).go(null);
    }

    /**
     * Creates a View.OnClickListener that starts a new activity.
     * <p/>
     * Uses the default system transition and doesn't finish the current activity.
     *
     * @param aClass The class of the new activity.
     */
    public OpenNewActivity(Class aClass) {
        this(null, aClass, null);
    }

    /**
     * Creates a View.OnClickListener that starts a new activity.
     * <p/>
     * Uses the default system transition and doesn't finish the current activity.
     *
     * @param aClass The class of the new activity.
     */
    public OpenNewActivity(Class aClass, Test test) {
        this(null, aClass, test);
    }

    /**
     * Creates a View.OnClickListener that starts a new activity.
     * <p/>
     * Uses the default system transition and doesn't finish the current activity.
     * <p/>
     * You can use #OpenNewActivity(Class class) instead of this.
     * 
     * @param activity The current activity.
     * @param aClass   The class of the new activity.
     */
    public OpenNewActivity(Activity activity, Class aClass) {
        this(activity, aClass, null);
    }

    /**
     * Creates a View.OnClickListener that starts a new activity.
     * <p/>
     * Uses the default system transition and doesn't finish the current activity.
     * <p/>
     * You can use #OpenNewActivity(Class class) instead of this.
     *
     * @param activity The current activity.
     * @param aClass   The class of the new activity.
     */
    public OpenNewActivity(@Nullable Activity activity, Class aClass, @Nullable Test test) {
        this.activity = activity;
        this.aClass = aClass;
        this.test = test;
        this.animation = TransitionAnimation.DEFAULT;
        this.finish = false;
    }

    /**
     * Creates a View.OnClickListener that starts a new activity.
     * <p/>
     * Uses the default system transition. The current activity can be automatically finished for you.
     *
     * @param activity The current activity.
     * @param aClass   The class of the new activity.
     * @param finish   Whether to finish the current activity or not.
     */
    public OpenNewActivity(Activity activity, Class aClass, boolean finish) {
        this.activity = activity;
        this.aClass = aClass;
        this.finish = finish;
        this.animation = TransitionAnimation.DEFAULT;
        test = null;
    }

    /**
     * Creates a View.OnClickListener that starts a new activity.
     * <p/>
     * A built-in transition animation can be specified. The current activity can be automatically finished for you.
     *
     * @param activity  The current activity.
     * @param aClass    The class of the new activity.
     * @param finish    Whether to finish the current activity or not.
     * @param animation A built-in transition.
     * @see TransitionAnimation
     */
    public OpenNewActivity(Activity activity, Class aClass, boolean finish, TransitionAnimation animation) {
        this.activity = activity;
        this.aClass = aClass;
        this.finish = finish;
        this.animation = animation;
        test = null;
    }

    /**
     * Creates a View.OnClickListener that starts a new activity.
     * <p/>
     * A custom transition animation can be specified. The current activity can be automatically finished for you.
     *
     * @param activity  The current activity.
     * @param aClass    The class of the new activity.
     * @param finish    Whether to finish the current activity or not.
     * @param enterAnim The {@code anim} resource file to use for the enter animation.
     * @param exitAnim  The {@code anim} resource file to use for the exit animation.
     * @see TransitionAnimation
     */
    public OpenNewActivity(Activity activity, Class aClass, boolean finish, int enterAnim, int exitAnim) {
        this.activity = activity;
        this.aClass = aClass;
        this.finish = finish;
        this.enterAnim = enterAnim;
        this.exitAnim = exitAnim;
        this.animation = TransitionAnimation.CUSTOM;
        test = null;
    }

    public OpenNewActivity flags(int flags) {
        this.flags = flags;
        return this;
    }

    @Override
    public void onClick(@Nullable View view) {
        if (activity == null && view != null && view.getContext() != null) {
            context = view.getContext();
        } else {
            context = activity;
        }
        go(view);
    }

    /**
     * Simpler way to immediately run the transition.
     * Performs the same as the {@link #onClick(android.view.View)}, it's just smaller.
     * @param view
     */
    @SuppressLint("NewApi")
    public void go(@Nullable View view) {
        if (context == null && activity == null) Log.e(TAG, "No context!");
        if (context == null) context = activity;

        if (test != null && !test.isValid(view)) {
            Log.i(TAG, "Test failed");
            return;
        }

        Intent intent = new Intent(context, aClass);
        if (flags != null) intent.addFlags(flags);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (context != null) {
                Bundle bundle = null;
                switch (animation) {
                    case FADE:
                        bundle = ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
                                                .toBundle();
                        break;
                    case CUSTOM:
                        activity.overridePendingTransition(enterAnim, exitAnim);
                        bundle = ActivityOptions.makeCustomAnimation(context, enterAnim, exitAnim).toBundle();
                        break;
                    case DEFAULT:
                    default:
                }

                if (finish && activity != null) activity.finish();

                if (bundle != null) {
                    context.startActivity(intent, bundle);
                } else {
                    context.startActivity(intent);
                }
            }
        } else {
            // PRE JELLY BEAN
            context.startActivity(intent);
            if (activity != null) {
                if (finish) activity.finish();
                switch (animation) {
                    case FADE:
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        break;
                    case CUSTOM:
                        activity.overridePendingTransition(enterAnim, exitAnim);
                        break;
                    case DEFAULT:
                    default:
                }
            }
        }
    }


    public static final View.OnClickListener back = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getContext() != null) {
                ((Activity) v.getContext()).onBackPressed();
            }
        }
    };
}
