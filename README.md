# StopwatchLog

## 简介
一个可定制的计时器小工具

## 功能
 * 支持串行任务和并行任务
 * 支持日志打印
 * 支持配置日志，设置打印开关和阈值
 
## 使用方法
 ### 创建StopwatchLog对象
 ``` java
   StopwatchLog stopwatchLog = new StopwatchLog("id", "LogName");
 ```

 ### 计时打点操作
 * 对串行任务，可通过以下流程快速打点记录
   > start -> mark -> stopAndInfo 
            
 * 对并行任务，可通过以下流程对任务自由打点
   > start(taskName) -> stop(taskName) 
 
 ## 日志打印配置
 * 可通过配置文件控制指定id的计时器的日志开关，配置格式
   > {stopwatch.id},{switch},{minMillis}
 * id:计时器id，如果为空则忽略
 * switch：日志开关，可通过配置随时改变，值为true/false
 * minMillis：最小耗时阈值，单位为毫秒。如果配置则打印总耗时大于该值的记录。
 *
 
 ### 测试用例
 可参考: test/java/tool/common/stopwatchlog/Test.java
 
## TIPS
注：本工具非线程安全，同一个StopwatchLog对象不建议在多个线程中使用