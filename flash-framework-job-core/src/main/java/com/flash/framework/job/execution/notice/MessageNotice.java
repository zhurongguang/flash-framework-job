package com.flash.framework.job.execution.notice;


import com.flash.framework.job.execution.JobExecutionLog;

/**
 * 异常信息通知接口
 *
 * @author zhurg
 * @date 2019/4/30 - 下午2:32
 */
public interface MessageNotice {

    void notice(JobExecutionLog jobExecutionLog, String mailGroup);
}