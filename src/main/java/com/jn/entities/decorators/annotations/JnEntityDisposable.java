package com.jn.entities.decorators.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;

@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface JnEntityDisposable {
	CcpEntityExpurgableOptions timeOption();
	Class<?> value();
}
