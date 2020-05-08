package tool.common.stopwatchlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author kober
 * @date created on 2020/5/8
 */
public class StopwatchLog {
    private final String id;
    private final Logger logger;
    private String traceId;
    private Map<String, TaskInfo> taskMap = new LinkedHashMap<>();
    private String lastTaskName;
    private long startTimeMillis;
    private long totalTimeMillis;

    public StopwatchLog() {
        this("", null);
    }

    public StopwatchLog(String id) {
        this(id, null);
    }

    /**
     * 构造函数
     * @param id 计时器id。入参为空则默认空字符串
     * @param logName 日志名。入参为空则默认计时器类名：StopwatchLog
     */
    public StopwatchLog(String id, String logName) {
        if(id != null && !id.isEmpty()) {
            this.id = id;
        }else{
            this.id = "";
        }
        if(logName != null && !logName.isEmpty()) {
            this.logger = LoggerFactory.getLogger(logName);
        }else{
            this.logger = LoggerFactory.getLogger(this.getClass());
        }
        if(traceId == null) {
            traceId = "-";
        }
    }

    public String getId() {
        return id;
    }

    private boolean isLogAvailable() {
        StopwatchLogConfig logConfig = StopwatchLogConfigLoader.getConf(this.id);
        if(logConfig != null && !logConfig.logSwitchOn) {
            return false;
        }
        return true;
    }

    public String getTraceId() {
        return traceId;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    /**
     * 获得总耗时
     */
    public long getTotalTimeMillis() {
        return totalTimeMillis;
    }

    /**
     * 获得任务总数
     */
    public int getTaskCount() {
        return taskMap.size();
    }

    /**
     * 开始一个新的任务计时,taskName默认为taskCount递增
     */
    public StopwatchLog start() {
        return start(null);
    }

    /**
     * 开始一个任务名为taskName的任务计时
     */
    public StopwatchLog start(String taskName) {
        long now = System.currentTimeMillis();
        if(getTaskCount() == 0) {
            startTimeMillis = now;
        }
        if(taskName == null || taskName.length() == 0) {
            taskName = String.valueOf(taskMap.size());
        }
        lastTaskName = taskName;
        TaskInfo taskInfo = taskMap.get(taskName);
        if(taskInfo != null && taskInfo.isRunning() && isLogAvailable()) {
            logger.error("Can't start task {}: it's already running", taskName);
        }else{
            //add task
            taskInfo = new TaskInfo(taskName, now);
            taskMap.put(taskName, taskInfo);
        }
        return this;
    }

    /**
     * 结束上一个任务计时，并立即开始一个新的任务，taskName默认为taskCount递增
     */
    public void mark() {
        mark(null);
    }

    /**
     * 结束上一个任务，并立即开始一个新的任务
     */
    public void mark(String nextTaskName) {
        stop();
        start(nextTaskName);
    }

    /**
     * 结束上一个任务
     */
    public void stop() {
        stop(lastTaskName);
    }

    /**
     * 结束名为taskName的任务
     */
    public void stop(String taskName) {
        TaskInfo taskInfo = taskMap.get(taskName);
        if(taskInfo == null) {
            if(!isLogAvailable()) {
                logger.error("Can't stop task: it's not exist");
            }
            return;
        }
        if (!taskInfo.isRunning()) {
            if(!isLogAvailable()) {
                logger.error("Can't stop task {}: it's not running", taskName);
            }
        } else {
            long now = System.currentTimeMillis();
            taskInfo.costTimeMillis = now - taskInfo.startTimeMillis;
            taskInfo.running = false;

            this.totalTimeMillis = now - this.startTimeMillis;
        }
    }

    /**
     * info打印条件判断
     * 如果日志关闭或总耗时低于阈值，则不打印
     * @return
     */
    private boolean isLogInfoAvailable() {
        StopwatchLogConfig logConfig = StopwatchLogConfigLoader.getConf(this.id);
        if(logConfig == null) {
            return true;
        }
        if(!logConfig.logSwitchOn) {
            return false;
        }
        if(this.totalTimeMillis < logConfig.minMillis) {
            return false;
        }
        return true;
    }

    /**
     * 结束上一个任务，并打印统计信息
     */
    public void stopAndInfo() {
        stop();
        info();
    }

    /**
     * 获得任务名为taskName的任务耗时
     */
    public long getTaskCostTime(String taskName) {
        TaskInfo task = taskMap.get(taskName);
        if(task != null) {
            return task.getCostTimeMillis();
        }
        return 0;
    }

    /**
     * 获得任务详情
     */
    public long getTaskStartTime(String taskName) {
        TaskInfo task = taskMap.get(taskName);
        if(task != null) {
            return task.getStartTimeMillis();
        }
        return 0;
    }

    public TaskInfo getTask(String taskName) {
        return taskMap.get(taskName);
    }

    private String shortSummary() {
        return traceId + "\tStopwatchLog [" + this.id + "]: running time (ms) = " + this.totalTimeMillis;
    }

    /**
     * 打印所有任务的耗时统计
     */
    private String prettyPrint() {
        boolean exception = false;
        StringBuilder sb = new StringBuilder(shortSummary());
        sb.append('\n');
        if (getTaskCount() == 0) {
            sb.append("No task info kept");
        } else {
            sb.append("-----------------------------------------\n");
            sb.append("cost(ms)     %     start     task \n");
            sb.append("-----------------------------------------\n");
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumIntegerDigits(5);
            nf.setGroupingUsed(false);
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(3);
            pf.setGroupingUsed(false);
            for (TaskInfo task : taskMap.values()) {
                if(task.isRunning()) {
                    sb.append("running").append("  ");
                    exception = true;
                }else{
                    sb.append(nf.format(task.costTimeMillis)).append("  ");
                }
                sb.append(pf.format(task.getCostTimeSeconds() / getTotalTimeSeconds())).append("  ");
                sb.append(task.startTimeMillis).append("  ");
                sb.append(task.taskName).append("\n");
            }
        }
        if(exception) {
            sb.append("[Warning] Exception found! Please check your code.").append("\n");
        }
        return sb.toString();
    }

    /**
     * 在logger中打印默认耗时统计, 可通过cms配置设置输出的日志名和阈值
     * 自动上报到trace系统
     */
    public void info() {
        if(isLogInfoAvailable()) {
            info(prettyPrint());
        }
    }

    /**
     * 打印msg到logger
     */
    private void info(String msg) {
        logger.info(msg);
    }

    public double getTotalTimeSeconds() {
        return this.totalTimeMillis / 1000.0;
    }

    public static final class TaskInfo {
        private final String taskName;
        private boolean running;
        private long startTimeMillis;
        private long costTimeMillis;

        public TaskInfo(String taskName) {
            this.taskName = taskName;
        }

        public TaskInfo(String taskName, long startTimeMillis) {
            this.taskName = taskName;
            this.startTimeMillis = startTimeMillis;
            this.running = true;
        }

        public boolean isRunning() {
            return running;
        }

        public long getStartTimeMillis() {
            return startTimeMillis;
        }

        public long getCostTimeMillis() {
            return costTimeMillis;
        }

        public double getCostTimeSeconds() {
            return (this.costTimeMillis / 1000.0);
        }

        @Override
        public String toString() {
            return "TaskInfo{" +
                    "taskName='" + taskName + '\'' +
                    ", running=" + running +
                    ", startTimeMillis=" + startTimeMillis +
                    ", costTimeMillis=" + costTimeMillis +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "StopwatchLog{" +
                "id='" + id + '\'' +
                ", traceId='" + traceId + '\'' +
                ", taskMap=" + taskMap +
                ", startTimeMillis=" + startTimeMillis +
                ", totalTimeMillis=" + totalTimeMillis +
                '}';
    }
}
