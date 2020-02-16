package com.flash.framework.job.task.execution.compensate;

import com.flash.framework.job.task.JobTaskConstants;
import com.flash.framework.job.task.task.JobTask;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 补偿任务处理
 *
 * @author zhurg
 * @date 2019/4/30 - 下午4:45
 */
@Slf4j
public class CompensateService {

    @Value("${tasks.compensateRetry:3}")
    private int failedTimes;

    @Autowired
    private CompensateHandler compensateHandler;

    @Autowired
    private ApplicationContext applicationContext;

    public void handler() {
        long pageNo = 1L;
        long pageSize = 100L;
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("retry", failedTimes);
        long total = compensateHandler.countFailedCompensate(criteria);
        long totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        while (pageNo <= totalPage) {
            List<CompensateJobInfo> compensateJobInfos = compensateHandler.pagingFailedCompensate((pageNo - 1) * pageSize, pageSize, criteria);
            if (CollectionUtils.isEmpty(compensateJobInfos)) {
                break;
            }
            pageNo++;
            compensateJobInfos.forEach(compensateJobInfo -> {
                try {
                    JobTask jobTask = (JobTask) applicationContext.getBean(Class.forName(compensateJobInfo.getJobTask()));
                    jobTask.execute(compensateJobInfo.getJobTaskContext());
                    compensateJobInfo.setStatus(JobTaskConstants.JOB_SUCCESS);
                } catch (Exception e) {
                    log.error("[Job Task] Job {} compensate execute failed ", compensateJobInfo.getJobTask(), e);
                    compensateJobInfo.setFailedTimes(compensateJobInfo.getFailedTimes() + 1);
                    compensateJobInfo.setStatus(JobTaskConstants.JOB_ERROR);
                    compensateJobInfo.setErrMsg(StringUtils.substring(e.getMessage(), 2000));
                } finally {
                    compensateJobInfo.setUpdatedAt(new Date());
                    compensateHandler.updateCompensateJobInfo(compensateJobInfo);
                }
            });
        }
    }
}