package com.flash.framework.job.task.execution.compensate.jdbc;

import com.alibaba.fastjson.JSON;
import com.flash.framework.job.task.execution.compensate.CompensateHandler;
import com.flash.framework.job.task.execution.compensate.CompensateJobInfo;
import com.flash.framework.job.task.task.JobTaskContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhurg
 * @date 2019/8/19 - 下午8:04
 */
@Slf4j
@Data
public class JdbcCompensateHandler implements CompensateHandler {

    private String insertSql = "INSERT INTO job_task_compensate(compensate_id,job_task,job_task_context,failed_times,`status`,err_msg,created_at,updated_at) " +
            "VALUES(?,?,?,?,?,?,?,?)";

    private String count_sql = "SELECT COUNT(id) FROM job_task_compensate WHERE `status` IN (0,2) AND failed_times < ?";

    private String paging_sql = "SELECT * FROM job_task_compensate WHERE `status` IN (0,2) AND failed_times < ? LIMIT ?,?";

    private String update_sql = "UPDATE job_task_compensate SET failed_times = ?,`status` = ?,err_msg = ?,updated_at = ? WHERE id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void saveCompensateJobInfo(CompensateJobInfo compensateJobInfo) {
        int rs = jdbcTemplate.update(insertSql, compensateJobInfo.getCompensateId(), compensateJobInfo.getJobTask(),
                Objects.nonNull(compensateJobInfo.getJobTaskContext()) ? JSON.toJSONString(compensateJobInfo.getJobTaskContext()) : null,
                compensateJobInfo.getFailedTimes(), compensateJobInfo.getStatus(), compensateJobInfo.getErrMsg(), compensateJobInfo.getCreatedAt(), compensateJobInfo.getUpdatedAt());
        if (rs != 1) {
            log.error("[Job Task] save job compensate info {} failed", JSON.toJSONString(compensateJobInfo));
        }
    }

    @Override
    public void updateCompensateJobInfo(CompensateJobInfo compensateJobInfo) {
        int rs = jdbcTemplate.update(update_sql, compensateJobInfo.getFailedTimes(), compensateJobInfo.getStatus(), compensateJobInfo.getErrMsg(),
                compensateJobInfo.getUpdatedAt(), compensateJobInfo.getId());
        if (rs != 1) {
            log.error("[Job Task] update job compensate info {} failed", JSON.toJSONString(compensateJobInfo));
        }
    }

    @Override
    public long countFailedCompensate(Map<String, Object> criteria) {
        return jdbcTemplate.queryForObject(count_sql, new Object[]{criteria.get("retry")}, Long.class);
    }

    @Override
    public List<CompensateJobInfo> pagingFailedCompensate(long offset, long limit, Map<String, Object> criteria) {
        return jdbcTemplate.query(paging_sql, new Object[]{criteria.get("retry"), offset, limit}, (rs, rowNum) -> {
            CompensateJobInfo compensateJobInfo = new CompensateJobInfo();
            compensateJobInfo.setId(rs.getLong("id"));
            compensateJobInfo.setCompensateId(rs.getString("compensate_id"));
            compensateJobInfo.setFailedTimes(rs.getInt("failed_times"));
            compensateJobInfo.setStatus(rs.getInt("status"));
            compensateJobInfo.setJobTask(rs.getString("job_task"));
            String jobTaskContext = rs.getString("job_task_context");
            compensateJobInfo.setJobTaskContext(StringUtils.isNotBlank(jobTaskContext) ? JSON.parseObject(jobTaskContext, JobTaskContext.class) : null);
            return compensateJobInfo;
        });
    }
}
