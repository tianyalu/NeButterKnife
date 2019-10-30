package com.sty.ne.butterknife.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tian on 2019/10/30.
 */

// SOURCE 注解仅在源码中保留，class文件中不存在
// CLASS 注解在源码和class文件中都存在，但运行时不存在
// RUNTIME 注解在源码，class文件中存在，且运行时可以通过反射机制获取到
@Target(ElementType.METHOD)   //该注解作用在属性之上
@Retention(RetentionPolicy.CLASS) //注解在编译器
public @interface OnClick {
    int value();
}
