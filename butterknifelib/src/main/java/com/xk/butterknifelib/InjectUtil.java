package com.xk.butterknifelib;

import android.app.Activity;

/**
 * Created by xuekai on 2017/11/21.
 */

public class InjectUtil {
    public static void bind(Activity activity) {
        try {
            Class<?> viewBinderClazz = Class.forName(activity.getClass().getCanonicalName() + "$$ViewBinder");
            ViewBinder viewBinder = (ViewBinder) viewBinderClazz.newInstance();
            viewBinder.bind(activity);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }
}
