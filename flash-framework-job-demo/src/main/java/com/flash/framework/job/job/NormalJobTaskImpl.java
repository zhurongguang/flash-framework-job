package com.flash.framework.job.job;

import com.alibaba.fastjson.JSON;
import com.flash.framework.job.task.Job;
import com.flash.framework.job.task.JobTask;
import com.flash.framework.job.task.JobTaskContext;

/**
 * 正常任务测试
 *
 * @author zhurg
 * @date 2020/2/21 - 3:00 PM
 */
@Job(name = "NormalJobTaskImpl", cron = "0 0/1 * * * ?", desc = "正常任务实现")
public class NormalJobTaskImpl extends JobTask {

    @Override
    public void execute(JobTaskContext jobTaskContext) {
        System.out.println("NormalJobTaskImpl execute,context : " + JSON.toJSONString(jobTaskContext));
    }
}
