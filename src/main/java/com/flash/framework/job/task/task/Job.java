package com.flash.framework.job.task.task;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author zhurg
 * @date 2019/8/19 - 上午11:15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Job {

    /**
     * 任务名称
     *
     * @return
     */
    String name();

    /**
     * 任务描述
     *
     * @return
     */
    String desc() default "";

    /**
     * cron 表达式
     *
     * @return
     */
    String cron();

    /**
     * 任务参数
     *
     * @return
     */
    String jobParameter() default "";

    /**
     * 是否进行失败补偿
     *
     * @return
     */
    boolean compensate() default false;

    /**
     * 是否记录日志
     *
     * @return
     */
    boolean executionLog() default true;

    /**
     * 分片参数（ElasticJob使用）
     *
     * @return
     */
    String shardingItemParameters() default "";

    /**
     * 分片数量（ElasticJob使用）
     *
     * @return
     */
    String shardingTotalCount() default "1";

    /**
     * 异常邮件组
     *
     * @return
     */
    String mailGroup() default "";
}