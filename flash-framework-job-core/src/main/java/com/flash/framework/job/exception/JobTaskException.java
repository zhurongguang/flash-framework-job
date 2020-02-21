package com.flash.framework.job.exception;

/**
 * @author zhurg
 * @date 2019/8/19 - 下午4:04
 */
public class JobTaskException extends RuntimeException {

    private static final long serialVersionUID = -254623733394147413L;

    public JobTaskException() {
    }

    public JobTaskException(String message) {
        super(message);
    }

    public JobTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobTaskException(Throwable cause) {
        super(cause);
    }

    public JobTaskException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}