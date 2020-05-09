package tool.common.stopwatchlog;

/**
 * @author kober
 * @date 2020/5/8
 */
public class StopwatchLogConfig {
    /**
     * StopwatchLog类的id
     */
    String id;
    /**
     * 打印开关
     */
    boolean logSwitchOn;
    /**
     * 打印阈值，单位毫秒，如果总耗时小于这个值则不打印
     */
    int minMillis;
}
