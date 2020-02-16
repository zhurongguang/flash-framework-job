package com.flash.framework.job.task.execution;

/**
 * 任务dump信息记录
 *
 * @author zhurg
 * @date 2018/11/4 - 下午8:48
 */
public interface JobExecutionService {

    /**
     * 记录任务异常信息
     *
     * @param jobExecutionLog
     */
    void saveJobExecutionLog(JobExecutionLog jobExecutionLog);
}