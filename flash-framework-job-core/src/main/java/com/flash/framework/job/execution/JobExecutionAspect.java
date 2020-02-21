package com.flash.framework.job.execution;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Job执行拦截
 *
 * @author zhurg
 * @date 2018/11/4 - 下午8:50
 */
@Aspect
public class JobExecutionAspect {

    @Autowired
    private JobExecutionHandler jobExecutionHandler;

    @Around(value = "@annotation(com.flash.framework.job.execution.JobExecution) || @annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object jobDump(ProceedingJoinPoint joinPoint) throws Throwable {
        return jobExecutionHandler.executionLog(joinPoint);
    }

}