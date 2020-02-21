package com.flash.framework.job.elasticjob;

import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;

import java.util.List;
import java.util.Map;

/**
 * ElasticJob job配置扩展
 *
 * @author zhurg
 * @date 2019/8/19 - 下午4:21
 */
public interface ElasticJobPropertiesHelper {

    /**
     * 添加配置
     *
     * @param jobDetailProperties 配置
     */
    void addJobProperties(Map<Class<?>, ElasticJobDetailProperties> jobDetailProperties);

    /**
     * 设置全局配置
     */
    ElasticJobDetailProperties setGlobalProperties();

    /**
     * 设置监听器
     *
     * @param jobListeners
     */
    void addJobListeners(Map<Class<?>, List<ElasticJobListener>> jobListeners);
}