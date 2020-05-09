package tool.common.stopwatchlog;

import com.google.common.base.Splitter;
import tool.common.config.ConfigFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * log config, control the log output
 * format: [stopwatch_id],[log_switch],[min_cost]
 * stopwatch_id: StopwatchLog.id
 * log_switch: turn on/off the switch by the value:true/false
 * min_cost: print log if total time is larger than it
 *
 *
 * @author kober
 * @date 2020/5/8
 */
public class StopwatchLogConfigLoader {
    private static final String configName = "stopwatch_log_config.ini";
    private static final ConfigFile configFile;
    private static Map<String, StopwatchLogConfig> confMap;

    static {
        //default path is root
        configFile = new ConfigFile("", configName);

        List<String> lines = configFile.getLines();
        if (lines != null && lines.size() > 0) {
            Map<String, StopwatchLogConfig> map = new HashMap<>(lines.size());
            for (String line : lines) {
                List<String> dataList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(line);
                if(dataList.size() == 3) {
                    StopwatchLogConfig conf = new StopwatchLogConfig();
                    conf.id = dataList.get(0);
                    if(conf.id == null || conf.id.isEmpty()) {
                        continue;
                    }
                    conf.logSwitchOn = Boolean.parseBoolean(dataList.get(1));
                    try {
                        conf.minMillis = Integer.parseInt(dataList.get(2));
                    } catch (NumberFormatException e) {
                        conf.minMillis = -1;
                    }
                    map.put(conf.id, conf);
                }
            }
            confMap = map;
        }

    }

    public static StopwatchLogConfig getConf(String id) {
        return confMap != null ? confMap.get(id) : null;
    }
}
