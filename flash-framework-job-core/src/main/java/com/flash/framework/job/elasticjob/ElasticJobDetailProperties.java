package com.flash.framework.job.elasticjob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhurg
 * @date 2019/8/19 - 下午4:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElasticJobDetailProperties implements Serializable {

    private static final long serialVersionUID = 3812044550883084623L;

    private int monitorPort = -1;

    boolean monitorExecution;

    private int maxTimeDiffSeconds = -1;

    private String jobShardingStrategyClass = "com.dangdang.ddframe.job.lite.api.strategy.impl.AverageAllocationJobShardingStrategy";

    private int reconcileIntervalMinutes = 10;

    private boolean disabled;

    private boolean overwrite = true;
}