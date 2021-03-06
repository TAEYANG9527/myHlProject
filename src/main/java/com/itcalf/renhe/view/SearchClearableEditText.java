package com.itcalf.renhe.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;

public class SearchClearableEditText extends EditText {
    public String defaultValue = "";
    final Drawable imgX =
            //	getResources().getDrawable(android.R.drawable.presence_offline ); // X
            getResources().getDrawable(R.drawable.icon_top_search_delete); // X

    public boolean isSearch = false;

    public SearchClearableEditText(Context context) {
        super(context);
        init(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public SearchClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SearchClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context ctx) {
        // Set bounds of our X button
        imgX.setBounds(0, 0, imgX.getIntrinsicWidth(), imgX.getIntrinsicHeight());

        // There may be initial text in the field, so we may need to display the
        // button
        manageClearButton();

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                SearchClearableEditText et = SearchClearableEditText.this;

                // Is there an X showing?
                if (et.getCompoundDrawables()[2] == null)
                    return false;
                // Only do this for up touches
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                // Is touch on our clear button?
                if (event.getX() > et.getWidth() - et.getPaddingRight() - imgX.getIntrinsicWidth()) {
                    et.setText("");
                    SearchClearableEditText.this.removeClearButton();
                }
                return false;
            }

        });

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                SearchClearableEditText.this.manageClearButton();
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        this.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                EditText editText = (EditText) arg0;
                if (arg1) {
                    if (editText.getText().toString().trim().length() > 0) {
                        if (isSearch) {
                            editText.setCompoundDrawablesWithIntrinsicBounds(
                                    getResources().getDrawable(R.drawable.icon_top_search), null,
                                    getResources().getDrawable(R.drawable.icon_top_search_delete), null);
                        } else {
                            editText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                    getResources().getDrawable(R.drawable.icon_top_search_delete), null);
                        }
                    }
                } else {
                    if (isSearch) {
                        editText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.icon_top_search),
                                null, null, null);
                    } else {
                        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    }
                }
            }
        });
        //修改为自定义字体
        if ((isInEditMode()) || (Constants.APP_TYPEFACE == null))
            return;
        setTypeface(Constants.APP_TYPEFACE);
    }

    void manageClearButton() {
        if (this.getText().toString().equals(""))
            removeClearButton();
        else
            addClearButton();
    }

    void addClearButton() {
        this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], imgX,
                this.getCompoundDrawables()[3]);
    }

    void removeClearButton() {
        this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null,
                this.getCompoundDrawables()[3]);
    }

    public boolean isSearch() {
        return isSearch;
    }

    public void setSearch(boolean isSearch) {
        this.isSearch = isSearch;
    }
}
