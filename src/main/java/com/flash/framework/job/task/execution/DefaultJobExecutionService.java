package com.flash.framework.job.task.execution;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhurg
 * @date 2018/11/5 - 下午3:24
 */
@Slf4j
public class DefaultJobExecutionService implements JobExecutionService {

    @Override
    public void saveJobExecutionLog(JobExecutionLog jobExecutionLog) {
        log.info("[Job Task] execution log : {}", JSON.toJSONString(jobExecutionLog, SerializerFeature.BrowserCompatible));
    }
}
