package com.flash.framework.job.task.execution;

import com.flash.framework.job.task.JobTaskConstants;
import com.flash.framework.job.task.autoconfigure.JobTaskConfigure;
import com.flash.framework.job.task.execution.notice.MessageNotice;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author zhurg
 * @date 2018/11/5 - 下午4:49
 */
@Slf4j
public class JobExecutionHandler {

    @Autowired
    private JobTaskConfigure jobTaskConfigure;

    @Autowired(required = false)
    private MessageNotice messageNotice;

    @Autowired
    private JobExecutionService jobExecutionService;

    public Object executionLog(ProceedingJoinPoint joinPoint) throws Throwable {
        JobExecutionLog jobExecutionLog = JobExecutionLog.builder()
                .startTime(new Date())
                .jobClass(joinPoint.getTarget().getClass().getCanonicalName())
                .jobName(joinPoint.getTarget().getClass().getSimpleName())
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            Object rs = joinPoint.proceed();
            jobExecutionLog.setStatus(JobTaskConstants.JOB_SUCCESS);
            return rs;
        } catch (Throwable e) {
            jobExecutionLog.setStatus(JobTaskConstants.JOB_ERROR);
            jobExecutionLog.setErrMsg(StringUtils.substring(e.getMessage(), 0, e.getMessage().length() >= 1000 ? 1000 : e.getMessage().length()));
            if (jobTaskConfigure.isErrorMessageNotice()) {
                messageNotice.notice(jobExecutionLog, jobTaskConfigure.getMailGroup());
            }
            log.error("[Job Task] Job {} execute failed,cause:", joinPoint.getTarget().getClass().getCanonicalName(), e);
            throw e;
        } finally {
            stopwatch.stop();
            jobExecutionLog.setConsumeTime(stopwatch.elapsed(TimeUnit.MILLISECONDS));
            jobExecutionLog.setEndTime(new Date());
            executionLog(jobExecutionLog);
        }
    }

    public void executionLog(JobExecutionLog jobDump) {
        jobExecutionService.saveJobExecutionLog(jobDump);
    }
}