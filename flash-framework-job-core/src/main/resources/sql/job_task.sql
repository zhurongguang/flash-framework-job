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