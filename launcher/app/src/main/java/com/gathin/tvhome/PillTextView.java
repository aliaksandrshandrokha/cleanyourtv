package com.gathin.tvhome;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * API 22 port: android:drawableTint is Android 6, so on Android 5 a pill colours its own icons. The tint
 * follows the pill's state (dark on the focused white pill) and survives the icon being swapped, the
 * way the platform attribute does. From Android 6 this is a plain TextView.
 */
public class PillTextView extends TextView {
    private static final boolean OWN_TINT = Build.VERSION.SDK_INT < 23;

    // No initialisers: TextView's constructor sets the drawables before this class's fields would be assigned.
    private ColorStateList tint;
    private boolean tintChosen;

    public PillTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTint();
    }

    // pill_text is a colour selector that upstream keeps under res/drawable.
    @android.annotation.SuppressLint("ResourceType")
    ColorStateList tint() {
        if (!tintChosen) {
            tint = getResources().getColorStateList(R.drawable.pill_text);
            tintChosen = true;
        }
        return tint;
    }

    void setTint(ColorStateList value) {
        tint = value;
        tintChosen = true;
        applyTint();
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        applyTint();
    }

    @Override
    public void setCompoundDrawablesRelative(Drawable start, Drawable top, Drawable end, Drawable bottom) {
        super.setCompoundDrawablesRelative(start, top, end, bottom);
        applyTint();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        applyTint();
    }

    private void applyTint() {
        if (!OWN_TINT) return;
        ColorStateList list = tint();
        for (Drawable[] set : new Drawable[][] {getCompoundDrawablesRelative(), getCompoundDrawables()}) {
            for (Drawable d : set) {
                if (d == null) continue;
                if (list == null) {
                    d.clearColorFilter();
                } else {
                    d.mutate().setColorFilter(list.getColorForState(getDrawableState(), list.getDefaultColor()),
                            PorterDuff.Mode.SRC_IN);
                }
            }
        }
    }
}
