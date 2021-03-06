package com.gifola.customfonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class MyAutoCompleteText extends AppCompatAutoCompleteTextView {

    public MyAutoCompleteText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyAutoCompleteText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyAutoCompleteText(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Quicksand-Light.ttf");  //Ubuntu-L
            setTypeface(tf);
        }
    }

}