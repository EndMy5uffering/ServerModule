package com.server.packageing.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(CONSTRUCTOR)
public @interface DataPackageConstructor {
	public boolean ID() default false;
	public boolean LENGTH() default false;
	public boolean DYNAMIC() default false;
	public boolean DATA() default false;
}
