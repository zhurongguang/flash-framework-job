package com.flash.framework.job.execution;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认任务执行处理，仅打印日志到日志文件
 *
 * @author zhurg
 * @date 2018/11/5 - 下午3:24
 */
@Slf4j
public class DefaultJobExecutionService implements JobExecutionService {

    @Override
    public void saveJobExecutionLog(JobExecutionLog jobExecutionLog) {
        log.info("[Job Task] execution log : {}", JSON.toJSONString(jobExecutionLog));
    }
}
