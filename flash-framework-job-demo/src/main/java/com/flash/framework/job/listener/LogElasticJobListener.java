package com.flash.framework.job.listener;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;

/**
 * @author zhurg
 * @date 2020/2/21 - 10:15 PM
 */
public class LogElasticJobListener implements ElasticJobListener {
    @Override
    public void beforeJobExecuted(ShardingContexts shardingContexts) {
        System.out.println("beforeJobExecuted : " + JSON.toJSONString(shardingContexts));
    }

    @Override
    public void afterJobExecuted(ShardingContexts shardingContexts) {
        System.out.println("afterJobExecuted : " + JSON.toJSONString(shardingContexts));
    }
}
