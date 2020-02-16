package com.flash.framework.job.task.quartz;

import com.alibaba.fastjson.JSON;
import com.flash.framework.job.task.task.Job;
import com.flash.framework.job.task.task.JobTask;
import com.flash.framework.job.task.task.JobTaskContext;

/**
 * @author zhurg
 * @date 2019/8/19 - 下午5:09
 */
@Job(name = "QuartzJobDemo1", desc = "QuartzJobDemo1", cron = "0/1 * * * * ?", compensate = true)
public class QuartzJobDemo1 extends JobTask {


    @Override
    public void execute(JobTaskContext jobTaskContext) {
        System.out.println("QuartzJobDemo1 execute:" + JSON.toJSONString(jobTaskContext));
    }
}