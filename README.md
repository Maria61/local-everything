# local-everything
本地文件搜索器
- 开发环境：Windows、IDEA2018；
- 主要技术：Java文件操作，多线程，单例模式，JDBC编程，pinyin4j以及SQLite；
- 项目简介：该项目主要实现了对指定文件快速搜索，监控功能，根据文件变化实时更新数据库；
- 项目要点：本项目通过线程池开启多任务处理，使用atomic并发包线程计数，countdownlatch进行线程等待；通过pinyin4j实现拼音检索文件功能；
## 项目概述图
![flow_diagram](https://github.com/Maria61/local-everything/src/main/resources/photos/flow_diagram.png)
