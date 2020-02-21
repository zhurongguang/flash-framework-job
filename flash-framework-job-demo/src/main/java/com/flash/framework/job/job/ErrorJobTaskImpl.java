package com.flash.framework.job.job;

import com.alibaba.fastjson.JSON;
import com.flash.framework.job.task.Job;
import com.flash.framework.job.task.JobTask;
import com.flash.framework.job.task.JobTaskContext;

/**
 * 补偿任务测试
 *
 * @author zhurg
 * @date 2020/2/21 - 3:28 PM
 */
@Job(name = "ErrorJobTaskImpl", cron = "${error.job.cron}", jobParameter = "${error.job.params}",
        compensate = true, compensateCron = "${error.job.compensateCron}",mailGroup = "${error.job.mails}")
public class ErrorJobTaskImpl extends JobTask {

    @Override
    public void execute(JobTaskContext jobTaskContext) {
        System.out.println("ErrorJobTaskImpl execute,context : " + JSON.toJSONString(jobTaskContext));
        //模拟异常
        int num = 1 / 0;
    }
}
