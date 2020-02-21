package com.flash.framework.job.execution.notice;

import com.flash.framework.job.execution.JobExecutionLog;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author zhurg
 * @date 2019/4/30 - 下午2:33
 */
@Slf4j
public class MailMessageNotice implements MessageNotice {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    public void notice(JobExecutionLog jobDump, String mailGroup) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(mailFrom);
        mail.setTo(mailGroup.split(","));
        mail.setSubject("[ " + jobDump.getJobName() + " ]任务异常报警");
        mail.setText(message(jobDump));
        try {
            mailSender.send(mail);
        } catch (Exception ex) {
            log.error("[Job Task] job {} send error mail failed,cause:{}", Throwables.getStackTraceAsString(ex));
        }
    }

    private String message(JobExecutionLog jobDump) {
        StringBuffer message = new StringBuffer();
        message.append("任务名称：\t\t");
        message.append(jobDump.getJobName());
        message.append("\n");
        message.append("任务描述：\t\t");
        message.append(jobDump.getRemark());
        message.append("\n");
        message.append("任务类：\t\t");
        message.append(jobDump.getJobClass());
        message.append("\n");
        message.append("开始时间：\t\t");
        message.append(DateFormatUtils.format(jobDump.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
        message.append("\n");
        message.append("异常信息：\t\t");
        message.append(jobDump.getErrMsg());
        return message.toString();
    }
}