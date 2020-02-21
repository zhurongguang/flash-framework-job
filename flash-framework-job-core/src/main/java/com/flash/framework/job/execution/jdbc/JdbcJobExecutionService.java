package com.flash.framework.job.execution.jdbc;

import com.alibaba.fastjson.JSON;
import com.flash.framework.job.execution.JobExecutionLog;
import com.flash.framework.job.execution.JobExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author zhurg
 * @date 2019/8/19 - 下午6:17
 */
@Slf4j
public class JdbcJobExecutionService implements JobExecutionService {

    private static final String SQL = "INSERT INTO job_task_execution_log(job_class,job_name,start_time,end_time,err_msg,`status`," +
            "consume_time,remark,create_time,update_time,job_param,`ip`) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void saveJobExecutionLog(JobExecutionLog jobExecutionLog) {
        int rs = jdbcTemplate.update(SQL, jobExecutionLog.getJobClass(), jobExecutionLog.getJobName(), jobExecutionLog.getStartTime(),
                jobExecutionLog.getEndTime(), jobExecutionLog.getErrMsg(), jobExecutionLog.getStatus(), jobExecutionLog.getConsumeTime(),
                jobExecutionLog.getRemark(), jobExecutionLog.getCreateTime(), jobExecutionLog.getUpdateTime(), MapUtils.isNotEmpty(jobExecutionLog.getJobParam()) ? JSON.toJSONString(jobExecutionLog.getJobParam()) : null,
                jobExecutionLog.getIp());
        if (rs != 1) {
            log.error("[Job Task] save job execution log {} failed", JSON.toJSONString(jobExecutionLog));
        }
    }
}
