package com.flash.framework.job.factory.quartz;

import com.flash.framework.job.factory.ScheduleContext;
import lombok.Data;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Map;

/**
 * @author zhurg
 * @date 2020/2/20 - 11:10 AM
 */
@Data
public class QuartzScheduleContext extends ScheduleContext<QuartzJobBean> {

    private String jobGroupName;

    private String triggerGroupName;

    private String desc;

    private QuartzScheduleContext(String jobName, QuartzJobBean jobTask, String cron, Map<String, Object> jobParams) {
        super(jobName, jobTask, cron, jobParams);
    }

    public static QuartzScheduleContextBuilder builder(String jobName, QuartzJobBean jobTask, String cron, Map<String, Object> jobParams) {
        return new QuartzScheduleContextBuilder(new QuartzScheduleContext(jobName, jobTask, cron, jobParams));
    }

    public static class QuartzScheduleContextBuilder {
        private final QuartzScheduleContext context;

        public QuartzScheduleContextBuilder(QuartzScheduleContext context) {
            this.context = context;
        }

        public QuartzScheduleContextBuilder jobGroupName(String jobGroupName) {
            this.context.setJobGroupName(jobGroupName);
            return this;
        }

        public QuartzScheduleContextBuilder triggerGroupName(String triggerGroupName) {
            this.context.setTriggerGroupName(triggerGroupName);
            return this;
        }

        public QuartzScheduleContextBuilder desc(String desc) {
            this.context.setDesc(desc);
            return this;
        }

        public QuartzScheduleContext build() {
            return this.context;
        }
    }
}