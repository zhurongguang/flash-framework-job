package com.flash.framework.job.execution.compensate.job;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.flash.framework.job.JobTaskConstants;
import com.flash.framework.job.autoconfigure.JobTaskConfigure;
import com.flash.framework.job.execution.compensate.CompensateHandler;
import com.flash.framework.job.execution.compensate.CompensateJobHelper;
import com.flash.framework.job.execution.compensate.CompensateJobInfo;
import com.flash.framework.job.task.JobTask;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 补偿任务
 *
 * @author zhurg
 * @date 2020/2/20 - 3:37 PM
 */
@Slf4j
@Data
public class CompensateJobTask extends QuartzJobBean implements SimpleJob {

    public static final String COMPENSATE_JOB = "CompensateJob";

    @Autowired
    private CompensateHandler compensateHandler;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JobTaskConfigure jobTaskConfigure;

    @Autowired
    private CompensateJobHelper compensateJobHelper;

    /**
     * Quartz 每次任务触发的时候，会重新创建一个新的任务bean来执行任务
     *
     * @param jobExecutionContext
     */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        execute(jobExecutionContext.getMergedJobDataMap().getString("jobName"));
    }

    @Override
    public void execute(ShardingContext shardingContext) {
        execute(JSON.parseObject(shardingContext.getJobParameter(), Map.class).get("jobName").toString());
    }

    /**
     * 执行补偿任务
     */
    private void execute(String jobName) {
        long pageNo = 1L;
        long pageSize = 100L;
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("jobTask", jobName);
        long total = compensateHandler.countFailedCompensate(criteria);
        long totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        while (pageNo <= totalPage) {
            List<CompensateJobInfo> compensateJobInfos = compensateHandler.pagingFailedCompensate((pageNo - 1) * pageSize, pageSize, criteria);
            if (CollectionUtils.isEmpty(compensateJobInfos)) {
                break;
            }
            pageNo++;
            compensateJobInfos.forEach(compensateJobInfo -> {
                if (compensateJobInfo.getFailedTimes().intValue() < jobTaskConfigure.getCompensateRetry()) {
                    JobTask jobTask;
                    try {
                        Class<?> clazz = Class.forName(compensateJobInfo.getJobBeanName());
                        jobTask = (JobTask) applicationContext.getBean(clazz);
                    } catch (Exception e) {
                        jobTask = (JobTask) applicationContext.getBean(compensateJobInfo.getJobBeanName());
                    }
                    //设置为补偿任务执行
                    compensateJobInfo.getJobTaskContext().setCompensate(true);
                    try {
                        jobTask.execute(compensateJobInfo.getJobTaskContext());
                        compensateJobInfo.setStatus(JobTaskConstants.JOB_SUCCESS);
                    } catch (Exception e) {
                        log.error("[Job Task] Job {} compensate execute failed,cause:{}", compensateJobInfo.getJobTask(), Throwables.getStackTraceAsString(e));
                        compensateJobInfo.setFailedTimes(compensateJobInfo.getFailedTimes() + 1);
                        compensateJobInfo.setStatus(JobTaskConstants.JOB_ERROR);
                        compensateJobInfo.setErrMsg(StringUtils.substring(e.getMessage(), 0, e.getMessage().length() >= 1000 ? 1000 : e.getMessage().length()));
                    } finally {
                        compensateJobInfo.setUpdatedAt(new Date());
                        compensateHandler.updateCompensateJobInfo(compensateJobInfo);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("[Job Task] Job {} compensate execute passed max retry times", compensateJobInfo.getJobTask());
                    }
                }
            });
        }
    }
}