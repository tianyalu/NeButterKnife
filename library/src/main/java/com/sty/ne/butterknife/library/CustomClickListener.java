package com.sty.ne.butterknife.library;

import android.view.View;
import android.view.View.OnClickListener;

/**
 * Created by tian on 2019/10/30.
 */

public abstract class CustomClickListener implements OnClickListener {
    @Override
    public void onClick(View v) {
        doClick(v);
    }

    public abstract void doClick(View v);
}
