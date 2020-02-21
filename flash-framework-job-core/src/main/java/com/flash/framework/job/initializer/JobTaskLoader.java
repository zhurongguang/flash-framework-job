package com.flash.framework.job.initializer;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 任务加载器
 *
 * @author zhurg
 * @date 2019/8/19 - 上午11:23
 */
public interface JobTaskLoader {

    /**
     * 加载任务
     *
     * @param applicationContext
     * @param beanFactory
     * @param jobTasks
     */
    void loadJobs(ApplicationContext applicationContext, DefaultListableBeanFactory beanFactory, Map<String, Object> jobTasks);
}