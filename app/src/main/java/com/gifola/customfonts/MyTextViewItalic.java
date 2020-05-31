package com.gifola.customfonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;


public class MyTextViewItalic extends AppCompatTextView {

    public MyTextViewItalic(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyTextViewItalic(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextViewItalic(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/quicksand-italic_[allfont.net].ttf");
            setTypeface(tf);
        }
    }

}