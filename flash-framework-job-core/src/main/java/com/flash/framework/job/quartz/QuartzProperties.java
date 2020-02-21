package com.flash.framework.job.quartz;

import lombok.Data;

/**
 * @author zhurg
 * @date 2020/2/20 - 10:20 AM
 */
@Data
public class QuartzProperties {

    /**
     * 全局 jobGroupName
     */
    private String jobGroupName = "FlashFramework";

    /**
     * 全局 triggerGroupName
     */
    private String triggerGroupName = "FlashFramework";
}