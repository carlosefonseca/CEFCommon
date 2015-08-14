package com.carlosefonseca.common.widgets;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.os.Build;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.widget.TextView;
import bolts.Continuation;
import bolts.Task;

import java.util.*;

/**
 * from http://stackoverflow.com/a/17782522/1069444
 */
@SuppressWarnings("UnusedDeclaration")
public class AutoResizeTextView extends TextView {

    private static HashMap<Integer, Integer> sCache;

    private boolean mNeedsResize = true;

    public static void makeSameSizeOnNextResize(List<AutoResizeTextView> autoResizeTextViews) {
        AutoResizeTextView[] arr = new AutoResizeTextView[autoResizeTextViews.size()];
        makeSameSizeOnNextResize((AutoResizeTextView[]) autoResizeTextViews.toArray(arr));
    }

    public static void makeSameSizeOnNextResize(final AutoResizeTextView... textViews) {
        // This was made with tasks but could be done with a simple checker that ran on every listener call.
        // It would probably be faster lol

        final Float[] sizes = new Float[textViews.length];

        // create a task source for each TextView
        final ArrayList<Task<Void>.TaskCompletionSource> tasks = new ArrayList<>();
        for (AutoResizeTextView ignored : textViews) {
            tasks.add(Task.<Void>create());
        }

        // Assign a listener to each TextView
        for (int i = 0; i < textViews.length; i++) {
            final AutoResizeTextView textView = textViews[i];
            final int finalI = i;
            textView.setListener(new OnSizeCalculated() {
                @Override
                public void onSizeCalculated(float newSize) {
                    sizes[finalI] = newSize;
                    tasks.get(finalI).setResult(null);
                    textViews[finalI].setListener(null);
                }
            });
        }

        // Gets the tasks
        final ArrayList<Task<Void>> tasks2 = new ArrayList<>();
        for (Task<Void>.TaskCompletionSource task : tasks) tasks2.add(task.getTask());

        // When all tasks finish, all listeners have been called and all sizes where obtained.
        // Find the smallest size and set it on all TextViews
        Task.whenAll(tasks2).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                final float min = Collections.min(Arrays.asList(sizes));
                for (int i = 0; i < sizes.length; i++) {
                    textViews[i].setListener(null);
                    if (sizes[i] != min) {
                        textViews[i].setTextSize(min);
                    }
                }
                return null;
            }
        });
    }

    public static HashMap<Integer, Integer> getCache() {
        return sCache;
    }

    public static void setCache(HashMap<Integer, Integer> cache) {
        sCache = cache;
    }

    private interface SizeTester {
        /**
         * @param suggestedSize  Size of text to be tested
         * @param availableSpace available space in which text must fit
         * @return an integer < 0 if after applying {@code suggestedSize} to
         * text, it takes less space than {@code availableSpace}, > 0
         * otherwise
         */
        public int onTestSize(int suggestedSize, RectF availableSpace);
    }

    public interface OnSizeCalculated {
        void onSizeCalculated(float newSize);
    }

    private OnSizeCalculated mListener;

    private RectF mTextRect = new RectF();

    private RectF mAvailableSpaceRect;

    private SparseIntArray mTextCachedSizes;

    private TextPaint mPaint;

    private float mMaxTextSize;

    private float mSpacingMult = 1.0f;

    private float mSpacingAdd = 0.0f;

    private float mMinTextSize = 20;

    private int mWidthLimit;

    private static final int NO_LINE_LIMIT = -1;
    private int mMaxLines;

    private boolean mEnableSizeCache = true;
    private boolean mInitialized;

    public AutoResizeTextView(Context context) {
        super(context);
        initialize();
    }

    public AutoResizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        mPaint = new TextPaint(getPaint());
        mMaxTextSize = getTextSize();
        mAvailableSpaceRect = new RectF();
        mTextCachedSizes = new SparseIntArray();
        if (mMaxLines == 0) {
            // no value was assigned during construction
            mMaxLines = NO_LINE_LIMIT;
        }
        mInitialized = true;
    }

    @Override
    public void setTextSize(float size) {
        if (mMaxTextSize == size) return;
        mMaxTextSize = size;
        mTextCachedSizes.clear();
        mNeedsResize = true;
        requestLayout();
    }

    @Override
    public void setMaxLines(int maxlines) {
        mMaxLines = maxlines;
        mNeedsResize = true;
        super.setMaxLines(maxlines);
//        requestLayout();
    }

    @Override
    public int getMaxLines() {
        return mMaxLines;
    }

    @Override
    public void setSingleLine() {
        super.setSingleLine();
        setMaxLines(1);
    }

    @Override
    public void setSingleLine(boolean singleLine) {
        super.setSingleLine(singleLine);
        if (mMaxLines == 1) return;
        if (singleLine) {
            mMaxLines = 1;
        } else {
            mMaxLines = NO_LINE_LIMIT;
        }
        mNeedsResize = true;
        requestLayout();
    }

    @Override
    public void setLines(int lines) {
        if (mMaxLines == lines) return;
        mMaxLines = lines;
        mNeedsResize = true;
        super.setLines(lines);
//        requestLayout();
    }

    @Override
    public void setTextSize(int unit, float size) {
        Context c = getContext();
        Resources r;

        if (c == null) r = Resources.getSystem();
        else r = c.getResources();
        assert r != null;
        final float v = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
        if (v == mMaxTextSize) return;
        mMaxTextSize = v;
        mTextCachedSizes.clear();
        mNeedsResize = true;
        requestLayout();
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        mSpacingMult = mult;
        mSpacingAdd = add;
    }

    /**
     * Set the lower text size limit and invalidate the view
     */
    public void setMinTextSize(float minTextSize) {
        if (mMinTextSize == minTextSize) return;
        mMinTextSize = minTextSize;
        mNeedsResize = true;
        requestLayout();
    }

    public void setMinTextSize(int unit, float size) {
        Context c = getContext();
        Resources r = c == null ? Resources.getSystem() : c.getResources();
        assert r != null;
        setMinTextSize(TypedValue.applyDimension(unit, size, r.getDisplayMetrics()));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!isInEditMode() && (changed || mNeedsResize)) reAdjust();
        super.onLayout(changed, left, top, right, bottom);
    }

    private void reAdjust() {
        adjustTextSize(String.valueOf(getText()));
    }

    private void adjustTextSize(String string) {
        if (!mInitialized) {
            return;
        }
        int startSize = (int) mMinTextSize;
        int heightLimit = getMeasuredHeight() - getCompoundPaddingBottom() - getCompoundPaddingTop();
        mWidthLimit = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
        mAvailableSpaceRect.right = mWidthLimit;
        mAvailableSpaceRect.bottom = heightLimit;
        final int size = efficientTextSizeSearch(startSize, (int) mMaxTextSize, mSizeTester, mAvailableSpaceRect);
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        if (mListener != null) mListener.onSizeCalculated(size);
    }

    private final SizeTester mSizeTester = new SizeTester() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public int onTestSize(int suggestedSize, RectF availableSPace) {
            mPaint.setTextSize(suggestedSize);
            String text = String.valueOf(getText());
            boolean singleline = getMaxLines() == 1;
            if (singleline) {
                mTextRect.bottom = mPaint.getFontSpacing();
                mTextRect.right = mPaint.measureText(text);
            } else {
                StaticLayout layout = new StaticLayout(text, mPaint, mWidthLimit, Alignment.ALIGN_NORMAL, mSpacingMult,
                                                       mSpacingAdd, true);
                // return early if we have more lines
                if (getMaxLines() != NO_LINE_LIMIT && layout.getLineCount() > getMaxLines()) {
                    return 1;
                }
                mTextRect.bottom = layout.getHeight();
                int maxWidth = -1;
                for (int i = 0; i < layout.getLineCount(); i++) {
                    if (maxWidth < layout.getLineWidth(i)) {
                        maxWidth = (int) layout.getLineWidth(i);
                    }
                }
                mTextRect.right = maxWidth;
            }

            mTextRect.offsetTo(0, 0);
            if (availableSPace.contains(mTextRect)) {
                // may be too small, don't worry we will find the best match
                return -1;
            } else {
                // too big
                return 1;
            }
        }
    };

    /**
     * Enables or disables size caching, enabling it will improve performance
     * where you are animating a value inside TextView. This stores the font
     * size against getText().length() Be careful though while enabling it as 0
     * takes more space than 1 on some fonts and so on.
     *
     * @param enable enable font size caching
     */
    public void enableSizeCache(boolean enable) {
        mEnableSizeCache = enable;
        mTextCachedSizes.clear();
        adjustTextSize(String.valueOf(getText()));
    }

    @SuppressLint("UseSparseArrays")
    public static void enableGlobalSizeCache(boolean enable) {
        sCache = enable ? new HashMap<Integer, Integer>() : null;
    }

    private int efficientTextSizeSearch(int start, int end, SizeTester sizeTester, RectF availableSpace) {
        if (sCache != null || !mEnableSizeCache) {
            if (sCache == null) {
                return binarySearch(start, end, sizeTester, availableSpace);
            }

            int key = getText().toString().hashCode();
            Integer integer = sCache.get(key);
            if (integer != null) return integer;
            int i = binarySearch(start, end, sizeTester, availableSpace);
            sCache.put(key, i);
            return i;
        }
        String text = String.valueOf(getText());
        int key = text == null ? 0 : text.length();
        int size = mTextCachedSizes.get(key);
        if (size != 0) {
            return size;
        }
        size = binarySearch(start, end, sizeTester, availableSpace);
        mTextCachedSizes.put(key, size);
        return size;
    }

    private static int binarySearch(int start, int end, SizeTester sizeTester, RectF availableSpace) {
        int lastBest = start;
        int lo = start;
        int hi = end - 1;
        int mid;
        while (lo <= hi) {
            mid = (lo + hi) >>> 1;
            int midValCmp = sizeTester.onTestSize(mid, availableSpace);
            if (midValCmp < 0) {
                lastBest = lo;
                lo = mid + 1;
            } else if (midValCmp > 0) {
                hi = mid - 1;
                lastBest = hi;
            } else {
                return mid;
            }
        }
        // make sure to return last best
        // this is what should always be returned
        return lastBest;

    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        mNeedsResize = true;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        mTextCachedSizes.clear();
        super.onSizeChanged(width, height, oldwidth, oldheight);
        if (!isInEditMode() && (width != oldwidth || height != oldheight)) {
            reAdjust();
        }
    }

    public OnSizeCalculated getListener() {
        return mListener;
    }

    public void setListener(OnSizeCalculated mListener) {
        this.mListener = mListener;
    }
}
