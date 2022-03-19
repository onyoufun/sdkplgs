package com.linxcool.sdkface.sample;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huchanghai on 2017/9/5.
 */

public class FunctionViewFactory {

    static Set<FunctionViewAdapter> adapters = new HashSet<>();

    public static Set<FunctionViewAdapter> getAdapters() {
        return adapters;
    }

    public static void registAdapter(FunctionViewAdapter adapter) {
        adapters.add(adapter);
    }

    public static View newView(Activity activity, final FunctionViewAdapter adapter) {
        Button button = new Button(activity);
        button.setText(adapter.getFunctionText());
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.onClick(v);
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.leftMargin = dip2px(activity, 12);
        params.rightMargin = dip2px(activity, 12);
        button.setLayoutParams(params);
        return button;
    }

    /**
     * 根据手机的分辨率从 DP 的单位 转成为 PX(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
