package com.flash.framework.job.execution.compensate;

import com.flash.framework.job.execution.compensate.job.CompensateJobTask;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author zhurg
 * @date 2020/2/20 - 6:50 PM
 */
@Component
public class CompensateJobHelper implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 创建补偿任务
     *
     * @param jobName
     * @param beanFactory
     * @return
     */
    public CompensateJobTask createCompensateJob(String jobName, DefaultListableBeanFactory beanFactory) {
        String beanName = buildCompensateJobName(jobName);
        BeanDefinitionBuilder compensateBuilder = BeanDefinitionBuilder.rootBeanDefinition(CompensateJobTask.class);
        beanFactory.registerBeanDefinition(beanName, compensateBuilder.getBeanDefinition());
        CompensateJobTask compensateJobTask = applicationContext.getBean(beanName, CompensateJobTask.class);
        compensateBuilder.addConstructorArgValue(applicationContext);
        compensateJobTask.setApplicationContext(applicationContext);
        compensateJobTask.setCompensateHandler(applicationContext.getBean(CompensateHandler.class));
        return compensateJobTask;
    }

    /**
     * 构建补偿任务名称
     *
     * @param jobName
     * @return
     */
    public String buildCompensateJobName(String jobName) {
        return String.format("%s%s", jobName, CompensateJobTask.COMPENSATE_JOB);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}