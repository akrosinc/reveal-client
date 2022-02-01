package org.smartregister.view.customcontrols;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import org.smartregister.CoreLibrary;
import org.smartregister.R;
import org.smartregister.util.Cache;
import org.smartregister.util.CacheableData;

public class CustomFontTextView extends TextView {

    private Cache<Typeface> cache;

    @SuppressWarnings("UnusedDeclaration")
    public CustomFontTextView(Context context) {
        this(context, null, 0);
    }

    @SuppressWarnings("UnusedDeclaration")
    public CustomFontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 0);
        cache = CoreLibrary.getInstance().context().typefaceCache();
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.org_ei_drishti_view_customControls_CustomFontTextView, 0, defStyle);
        int variant = attributes
                .getInt(R.styleable
                                .org_ei_drishti_view_customControls_CustomFontTextView_fontVariant,
                        0);
        attributes.recycle();

        setFontVariant(variant);
    }

    public void setFontVariant(int variant) {
        setFontVariant(FontVariant.tryParse(variant, FontVariant.REGULAR));
    }

    public void setFontVariant(final FontVariant variant) {
        setTypeface(cache.get(variant.name(), new CacheableData<Typeface>() {
            @Override
            public Typeface fetch() {
                return Typeface.createFromAsset(
                        CoreLibrary.getInstance().context().applicationContext().getAssets(),
                        variant.fontFile());

            }
        }));
    }
}
