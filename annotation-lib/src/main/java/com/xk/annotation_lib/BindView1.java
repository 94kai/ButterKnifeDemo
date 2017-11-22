package com.xk.annotation_lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在field上的注解，为一个view变量赋值
 * Created by xuekai on 2017/11/20.
 */
@Target({ElementType.FIELD,ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface BindView1 {
    int value();
}
