## flash-framework-job使用说明
### 一、快速入门
* 引maven依赖

  ```xml
   <dependency>
       <groupId>com.flash.framework</groupId>
       <artifactId>flash-framework-job-core</artifactId>
       <version>1.0.0.RELEASE</version>
   </dependency>
  ```

* 继承JobTask类，实现execute方法

  ```java
  @Job(name = "DemoJobTask", cron = "0 0/1 * * * ?", desc = "Demo任务")
  public class DemoJobTask extends JobTask {
  
      @Override
      public void execute(JobTaskContext jobTaskContext) {
          //do job biz
      }
  }
  ```

* 在任务实现类上使用@Job注解（ps：是com.flash.framework.job.task.Job）,配置任务名称、cron表达式

  ```java
  @Job(name = "DemoJobTask", cron = "0 0/1 * * * ?", desc = "Demo任务")
  ```

* 配置application.yml

  ```yaml
  tasks:
    type: elastic_job
    ## 是否开启补偿机制,开启需要配置数据源
    compensate: false
    ## 任务异常是否发通知
    error-message-notice: false
    ## 异常邮件组
    mail-group: zhurg@163.com
    elastic-job:
      namespace: job-task
      server-lists: 127.0.0.1:2181
  ```

### 二、配置说明

#### 1.@Job注解参数

| 参数                   | 是否必填 | 作用描述                                                     |
| ---------------------- | -------- | ------------------------------------------------------------ |
| name                   | 是       | 任务名称，全局唯一，支持Spel表达式                           |
| desc                   | 否       | 任务描述，支持Spel表达式                                     |
| cron                   | 是       | cron表达式，支持Spel表达式                                   |
| jobParameter           | 否       | 任务参数，可在任务上下文中取出，json格式，支持Spel表达式     |
| compensate             | 否       | 是否开启补偿，默认实现依赖数据源，默认为false                |
| compensateCron         | 否       | 补偿任务cron表达式，补偿任务开启下必填，支持Spel表达式       |
| shardingTotalCount     | 否       | ElasticJob分片数，默认为1，支持Spel表达式                    |
| shardingItemParameters | 否       | ElasticJob分片参数，可在任务上下文中取出，json格式，支持Spel表达式 |
| mailGroup              | 否       | 异常邮件组，多个用逗号分隔，支持Spel表达式                   |
| jobGroupName           | 否       | Quartz任务组名称，默认为全局配置                             |
| triggerGroupName       | 否       | Quartz触发器组名称，默认为全局配置                           |

#### 2.application.yml配置

* 主要配置

| 参数               | 是否必填 | 作用描述                           |
| ------------------ | -------- | ---------------------------------- |
| tasks.type         | 是       | 任务实现类型，quartz、elastic_job  |
| compensate         | 否       | 是否开启补偿机制，默认否，全局配置 |
| errorMessageNotice | 否       | 是否开启异常通知机制，默认否       |
| mailGroup          | 否       | 异常邮件组，全局配置               |
| compensateRetry    | 否       | 补偿任务最大重试次数，默认3次      |

* ElasticJob配置

|                                                | 是否必填 | 作用描述                                           |
| ---------------------------------------------- | -------- | -------------------------------------------------- |
| tasks.elasticJob.serverLists                   | 是       | zookeeper地址                                      |
| tasks.elasticJob.namespace                     | 否       | zookeeper目录，默认flash-framework-job             |
| tasks.elasticJob.baseSleepTimeMilliseconds     | 否       | 等待重试的间隔时间的初始值，单位毫秒，默认1000     |
| tasks.elasticJob.maxSleepTimeMilliseconds      | 否       | 等待重试的间隔时间的最大值，单位毫秒，默认3000     |
| tasks.elasticJob.maxRetries                    | 否       | 最大重试次数，默认3                                |
| tasks.elasticJob.sessionTimeoutMilliseconds    | 否       | 会话超时时间，单位毫秒                             |
| tasks.elasticJob.connectionTimeoutMilliseconds | 否       | 连接超时时间，单位毫秒                             |
| tasks.elasticJob.digest                        | 否       | 连接Zookeeper的权限令牌                            |
| tasks.elasticJob.enableDatasource              | 否       | 是否启用ElasticJob的rdb存储，默认为false，走H2存储 |

* Quartz配置，Quartz的配置完全走Spring的配置

| 参数                          | 是否必填 | 作用描述                             |
| ----------------------------- | -------- | ------------------------------------ |
| tasks.quartz.jobGroupName     | 否       | 全局任务组名称，默认FlashFramework   |
| tasks.quartz.triggerGroupName | 否       | 全局触发器组名称，默认FlashFramework |



* Quartz 不使用数据源配置，不开启补偿、启日志记录

```yaml
spring:
  application:
    name: job-task
  quartz:
      job-store-type: memory
      properties:
        org:
          quartz:
            scheduler:
              instanceName: job-task
              instanceId: AUTO
            threadPool:
              class: org.quartz.simpl.SimpleThreadPool
              threadCount: 10
              threadPriority: 5
              threadsInheritContextClassLoaderOfInitializingThread: true
tasks:
  type: quartz
  ## 是否开启补偿机制,开启需要配置数据源
  compensate: false
  ## 任务异常是否发通知
  error-message-notice: false
  ## 异常邮件组
  mail-group: zhurg@163.com
```

* Quartz使用数据源配置，开启补偿或者日志记录

```yaml
spring:
  application:
    name: job-task
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/flash_framework?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull
    username: root
    password: password
    type: com.zaxxer.hikari.HikariDataSource
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
  quartz:
      job-store-type: memory
      properties:
        org:
          quartz:
            scheduler:
              instanceName: job-task
              instanceId: AUTO
            threadPool:
              class: org.quartz.simpl.SimpleThreadPool
              threadCount: 10
              threadPriority: 5
              threadsInheritContextClassLoaderOfInitializingThread: true
tasks:
  type: quartz
  ## 是否开启补偿机制,开启需要配置数据源
  compensate: true
  ## 任务异常是否发通知
  error-message-notice: false
  ## 异常邮件组
  mail-group: zhurg@163.com
```

* ElasticJob 不使用数据源，不开启补偿、日志记录

```yaml
### Quartz 配置
spring:
  application:
    name: job-task

tasks:
  type: elastic_job
  ## 是否开启补偿机制,开启需要配置数据源
  compensate: false
  ## 任务异常是否发通知
  error-message-notice: false
  ## 异常邮件组
  mail-group: zhurg@163.com
  elastic-job:
    namespace: job-task
    server-lists: 127.0.0.1:2181
```

* ElasticJob 使用数据源，开启补偿

```yaml
### Quartz 配置
spring:
  application:
    name: job-task
  datasource:
      driver-class-name: com.mysql.jdbc.Driver
      url: jdbc:mysql://127.0.0.1:3306/flash_framework?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull
      username: root
      password: password
      type: com.zaxxer.hikari.HikariDataSource
      hikari:
        minimum-idle: 5
        maximum-pool-size: 20

tasks:
  type: elastic_job
  ## 是否开启补偿机制,开启需要配置数据源
  compensate: true
  ## 任务异常是否发通知
  error-message-notice: false
  ## 异常邮件组
  mail-group: zhurg@163.com
  elastic-job:
    namespace: job-task
    server-lists: 127.0.0.1:2181
    ## 使用数据库存储elasticJob自身日志
    enable-datasource: true
```

### 三、Maven依赖说明

* 若需要开启默认补偿机制、日志记录，需要引入jdbc包

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
</dependency>
```

* 若使用ElasticJob，需要引入curator相关包（ps：curator最高支持到2.13.0）

```xml
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>2.13.0</version>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>2.13.0</version>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-client</artifactId>
    <version>2.13.0</version>
</dependency>
```

### 四、扩展点

#### 1.ElasticJob 自定义配置

* ElasticJobPropertiesHelper，实现该接口可以针对某些job设置自己的配置和ElasticJobListener

```java
/**
     * ElasticJob 配置辅助工具，可以扩展配置每个job
     *
     * @return
     */
@Bean
public ElasticJobPropertiesHelper elasticJobPropertiesHelper() {
    return new ElasticJobPropertiesHelper() {
        /**
             * 单个任务配置
             * @param jobDetailProperties 配置
             */
        @Override
        public void addJobProperties(Map<Class<?>, ElasticJobDetailProperties> jobDetailProperties) {
            jobDetailProperties.put(ErrorJobTaskImpl.class, new ElasticJobDetailProperties());
        }

        /**
             * 全局配置
             * @return
             */
        @Override
        public ElasticJobDetailProperties setGlobalProperties() {
            return new ElasticJobDetailProperties();
        }

        /**
             * 单个任务的Listener配置
             * @param jobListeners
             */
        @Override
        public void addJobListeners(Map<Class<?>, List<ElasticJobListener>> jobListeners) {
            jobListeners.put(NormalJobTaskImpl.class, Lists.newArrayList(new LogElasticJobListener()));
        }
    };
}
```

#### 2.任务执行日志记录

* jobExecutionService，默认实现为日志文件记录，提供jdbc记录实现方式，对于使用了Spring @Scheduled注解的任务，仍然可以记录执行日志

```java
/**
     * 记录JobTask的执行日志，写入到数据库
     *
     * @return
     */
@Bean
@Primary
public JobExecutionService jobExecutionService() {
    return new JdbcJobExecutionService();
}
```

#### 3.补偿任务记录扩展

* CompensateHandler，默认实现为JdbcCompensateHandler，用户可根据需求自行扩展存储方式。

#### 4.异常通知扩展

* MessageNotice，默认实现为MailMessageNotice，用户可根据需求自行扩展存储方式。

#### 5.手动增删改任务

* JobScheduleFactory

```java
public interface JobScheduleFactory<S, C> {

    /**
     * 创建任务
     *
     * @param context
     * @return
     */
    S createScheduler(C context) throws Exception;

    /**
     * 创建并启动任务
     *
     * @param context
     * @p
     */
    void addScheduler(C context) throws Exception;

    /**
     * 修改任务
     *
     * @param context
     */
    void modifyScheduler(C context) throws Exception;

    /**
     * 移除任务
     *
     * @param context
     */
    void removeScheduler(C context) throws Exception;
}
```

Quartz和ElasticJob的实现分别为：QuartzJobScheduleFactory、ElasticJobScheduleFactory

### 五、数据库

* job_task_execution_log，记录任务执行日志

```sql
CREATE TABLE `job_task_execution_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `job_class` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '任务类',
  `job_name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '任务名称',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `err_msg` text COLLATE utf8mb4_bin COMMENT '异常信息',
  `status` tinyint(2) NOT NULL COMMENT '状态 任务状态 1:执行成功 2:执行失败',
  `consume_time` bigint(20) DEFAULT NULL COMMENT '任务耗时',
  `remark` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `job_param` text COLLATE utf8mb4_bin COMMENT '任务参数',
  `ip` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行实例ip',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

* job_task_compensate，记录任务补偿日志

```sql
CREATE TABLE `job_task_compensate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `job_task` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '任务名称',
  `job_bean_name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '任务bean名称',
  `job_task_context` text COLLATE utf8mb4_bin COMMENT '任务执行上下文',
  `failed_times` int(11) NOT NULL DEFAULT '0' COMMENT '失败次数',
  `status` tinyint(2) NOT NULL COMMENT '任务状态 1:执行成功 2:执行失败',
  `err_msg` text COLLATE utf8mb4_bin COMMENT '异常信息',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_` (`job_task`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin
```

