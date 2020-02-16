package com.flash.framework.job.task.task;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.util.env.IpUtils;
import com.flash.framework.job.task.JobTaskConstants;
import com.flash.framework.job.task.autoconfigure.JobTaskConfigure;
import com.flash.framework.job.task.execution.JobExecutionHandler;
import com.flash.framework.job.task.execution.JobExecutionLog;
import com.flash.framework.job.task.execution.compensate.CompensateHandler;
import com.flash.framework.job.task.execution.compensate.CompensateJobInfo;
import com.flash.framework.job.task.execution.notice.MessageNotice;
import com.google.common.base.Stopwatch;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author zhurg
 * @date 2019/8/19 - 下午2:01
 */
@Slf4j
@Data
public abstract class JobTask extends QuartzJobBean implements SimpleJob {

    @Autowired
    private JobTaskConfigure jobTaskConfigure;

    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private MessageNotice messageNotice;

    @Autowired(required = false)
    private CompensateHandler compensateHandler;

    @Autowired(required = false)
    private JobExecutionHandler jobExecutionHandler;

    private Job jobDetail;

    public JobTask() {
        this.jobDetail = AnnotationUtils.findAnnotation(this.getClass(), Job.class);
    }

    /**
     * 执行任务
     *
     * @param jobTaskContext
     */
    public abstract void execute(JobTaskContext jobTaskContext);

    @Override
    protected void executeInternal(JobExecutionContext context) {
        if (useQuartz()) {
            JobTaskContext jobTaskContext = JobTaskContext.builder()
                    .jobExecuteTime(new Date())
                    .jobParameter(context.getMergedJobDataMap())
                    .build();
            executeTask(jobTaskContext);
        }
    }

    @Override
    public void execute(ShardingContext shardingContext) {
        if (useElasticJob()) {
            JobTaskContext jobTaskContext = JobTaskContext.builder()
                    .jobExecuteTime(new Date())
                    .jobName(shardingContext.getJobName())
                    .taskId(shardingContext.getTaskId())
                    .shardingTotalCount(shardingContext.getShardingTotalCount())
                    .shardingItem(shardingContext.getShardingItem())
                    .shardingParameter(shardingContext.getShardingParameter())
                    .jobParameter(StringUtils.isNotBlank(shardingContext.getJobParameter()) ? JSON.parseObject(shardingContext.getJobParameter()) : null)
                    .build();
            executeTask(jobTaskContext);
        }
    }

    private void executeTask(JobTaskContext jobTaskContext) {
        Job jobDetail = getJobDetail();
        JobExecutionLog jobExecutionLog = JobExecutionLog.builder()
                .startTime(jobTaskContext.getJobExecuteTime())
                .jobClass(this.getClass().getCanonicalName())
                .jobName(environment.resolvePlaceholders(jobDetail.name()))
                .createTime(jobTaskContext.getJobExecuteTime())
                .updateTime(jobTaskContext.getJobExecuteTime())
                .ip(IpUtils.getIp())
                .build();
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            execute(jobTaskContext);
            jobExecutionLog.setStatus(JobTaskConstants.JOB_SUCCESS);
        } catch (Throwable e) {
            log.error("[Job Task] Job {} execute failed,cause:", getJobDetail().name(), e);
            jobExecutionLog.setStatus(JobTaskConstants.JOB_ERROR);
            jobExecutionLog.setErrMsg(StringUtils.substring(e.getMessage(), 0, e.getMessage().length() >= 1000 ? 1000 : e.getMessage().length()));

            //异常邮件通知
            if (jobTaskConfigure.isErrorMessageNotice()) {
                String mailGroup = getJobDetail().mailGroup();
                if (StringUtils.isBlank(mailGroup)) {
                    mailGroup = jobTaskConfigure.getMailGroup();
                }
                if (StringUtils.isNotBlank(mailGroup)) {
                    messageNotice.notice(jobExecutionLog, environment.resolvePlaceholders(mailGroup));
                }
            }
            if (jobDetail.compensate()) {
                jobTaskContext.setCompensate(true);
            }
            //记录补偿信息
            if (jobTaskConfigure.isCompensate() && jobDetail.compensate() && !jobTaskContext.isCompensate()) {
                CompensateJobInfo compensateJobInfo = CompensateJobInfo.builder()
                        .compensateId(UUID.randomUUID().toString().replaceAll("-", ""))
                        .failedTimes(0)
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .errMsg(jobExecutionLog.getErrMsg())
                        .status(0)
                        .jobTask(this.getClass().getCanonicalName())
                        .jobTaskContext(jobTaskContext)
                        .build();
                compensateHandler.saveCompensateJobInfo(compensateJobInfo);
            }
        } finally {
            stopwatch.stop();
            jobExecutionLog.setJobParam(JSON.parseObject(JSON.toJSONString(jobTaskContext)));
            jobExecutionLog.setConsumeTime(stopwatch.elapsed(TimeUnit.MILLISECONDS));
            jobExecutionLog.setEndTime(new Date());
            //记录执行日志
            if (getJobDetail().executionLog() && !jobTaskContext.isCompensate()) {
                jobExecutionHandler.executionLog(jobExecutionLog);
            }
            if (log.isDebugEnabled()) {
                log.debug("[Job Task] Job {} execute finished,cost:{}", environment.resolvePlaceholders(jobDetail.name()), jobExecutionLog.getConsumeTime());
            }
        }

    }

    private boolean useQuartz() {
        return JobType.quartz.equals(jobTaskConfigure.getType());
    }

    private boolean useElasticJob() {
        return JobType.elastic_job.equals(jobTaskConfigure.getType());
    }
}