package com.sty.ne.butterknife.library;

import android.app.Activity;

/**
 * Created by tian on 2019/10/30.
 */

public class ButterKnife {

    public static void bind(Activity activity) {

        //找到一个类：MainActivity_ViewBinder，调用这个类的构造方法
        String className = activity.getClass().getName() + "$ViewBinder";

        try {
            //接口 = 接口实现类
            Class<?> clazz = Class.forName(className);
            //假设这个类就是接口的接口实现类
            ViewBinder viewBinder = (ViewBinder) clazz.newInstance();
            viewBinder.bind(activity);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
