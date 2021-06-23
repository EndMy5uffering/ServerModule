package com.server.packageing.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(CONSTRUCTOR)
public @interface DataPackageConstructor {
	public boolean ID() default true;
	public boolean LENGTH() default true;
	public boolean DYNAMIC() default true;
	public boolean DATA() default true;
}
