# StopwatchLog

## 简介
一个可灵活控制日志输出的的计时器工具

## 功能
 * 支持串行任务和并行任务
 * 支持日志打印
 * 支持配置日志，设置打印开关和阈值
 
## 使用方法
 可自行打包，引入java工程中使用
 
 ### 创建StopwatchLog对象
 ``` java
   StopwatchLog stopwatchLog = new StopwatchLog("id", "LogName");
 ```

 ### 计时打点操作
 * 对串行任务，可通过以下流程快速打点记录
   > start -> mark -> stopAndInfo 
                          
   举例：
   ```
   StopwatchLog stopwatchLog = new StopwatchLog("test").start();
   //task1
   stopwatchLog.mark();
   //task2
   stopwatchLog.stopAndInfo();
   ```
                                   
 * 对并行任务，可通过以下流程对任务自由打点 
 
   > start(taskName) -> stop(taskName) 
   
   举例：
   ```
   StopwatchLog stopwatchLog = new StopwatchLog("test").start();
   stopwatchLog.start("task1");
   stopwatchLog.start("task2");
   //task1和task2并发执行
   ...
   //task1结束时
   stopwatchLog.stop("task1");
   ...
   //task2 结束时
   stopwatchLog.stop("task2");   
   ```
   **注**：此方法目前只适合异步阻塞调用时，在主线程中打点计时。  
   当StopwatchLog对象传入多个线程时，请使用concurrentStart创建线程安全的task
   
 ### 日志输出
 日志输出为spring风格：
 
 ```
 [main] INFO TestLog - -	StopwatchLog [testSerial]: running time (ms) = 1501
 -----------------------------------------
 cost(ms)     %     start     task 
 -----------------------------------------
 01001  067%  1588940352359  0
 00500  033%  1588940353360  1    
 ```                      
  
 ### 日志打印配置
 可通过配置文件控制指定id的计时器的日志开关，配置格式
   > {id},{switch},{minMillis}
 * id: Stopwatch.id，如果为空则忽略
 * switch: 日志开关，可通过配置随时改变，值为true/false
 * minMillis: 最小耗时阈值，单位为毫秒。如果配置则打印总耗时大于该值的记录。
 
 
 ### 测试用例
 可参考: test/java/tool/common/stopwatchlog/Test.java
 
## TIPS
* 代码中的日志组件为slf4j + slf4j-simple, 自测使用，实际项目中可自行修改为需要的实现
