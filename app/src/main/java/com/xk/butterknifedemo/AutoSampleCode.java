package com.xk.butterknifedemo;

import com.xk.butterknifelib.ViewBinder;

/**
 * Created by xuekai on 2017/11/21.
 */

public class AutoSampleCode<T extends MainActivity> implements ViewBinder<T>{
    @Override
    public void bind(T activity) {
//        activity.textView=activity.findViewById(R.id.textView);
    }
}
