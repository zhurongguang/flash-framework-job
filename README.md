<p align="center" >
    <img src="https://raw.githubusercontent.com/zhurongguang/flash-framework/master/doc/logo.jpg" width="150">
    <h3 align="center">Flash-Framework</h3>
    <p align="center">
        Flash-Framework,A fast develop framework for SpringBoot,included distribution framework、common business framework、data transfer framework...
    </p>
</p>



## Introduction
Flash-Framework，是一个基于SpringBoot的快速开发框架，包含分布式开发框架、基础业务框架、数据转换框架等。

flash-framework-job，为常用定时任务的封装，同时支持Quartz和[ElasticJob](<http://elasticjob.io/index_zh.html>)，分别解决对单机定时任务和分布式定时任务的需求（ps：Quartz当然也可以搞集群，但是不如ElasticJob功能强大。[XXL-Job](<https://www.xuxueli.com/xxl-job/>)也蛮不错的，但是个人不喜欢单独部署一个调度中心，再加上公司一直在用ElasticJob，很多大工程都在用，所以分布式任务依然选择了ElasticJob，缺点是好久不维护了）。另外提供了补偿机制，某次任务执行失败，会自动重试，但不会影响下次任务时钟的执行。


## Documentation
- [文档]()


## Donate
无论捐赠金额多少都足够表达您这份心意，非常感谢 ：