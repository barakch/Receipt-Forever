package com.receipt.forever.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;


public class HintAdapter extends ArrayAdapter<String> {

    public HintAdapter(Context theContext, int theLayoutResId, String[] data) {
        super(theContext, theLayoutResId, data);
    }

    @Override
    public int getCount() {
        // don't display last item. It is used as hint.
        int count = super.getCount();
        return count > 0 ? count - 1 : count;
    }
}