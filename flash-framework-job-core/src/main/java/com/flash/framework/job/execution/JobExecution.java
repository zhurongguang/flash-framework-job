package com.flash.framework.job.execution;

import java.lang.annotation.*;

/**
 * 定时任务异常记录
 *
 * @author zhurg
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobExecution {

}