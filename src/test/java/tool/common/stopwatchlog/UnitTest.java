package tool.common.stopwatchlog;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class UnitTest {
    private Logger logger = LoggerFactory.getLogger("testLog");

    @Test
    public void testSerial() {
        StopwatchLog stopwatchLog = new StopwatchLog("testSerial", "TestLog").start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopwatchLog.mark();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopwatchLog.stopAndInfo();
    }

    @Test
    public void testParallel() {
        //a thread which costs random period of time
        class ComputeTask implements Callable<String> {
            private String taskName;
            private int sleepMillis;

            public String getTaskName() {
                return taskName;
            }

            public void setTaskName(String taskName) {
                this.taskName = taskName;
            }

            public ComputeTask(String taskName) {
                this.taskName = taskName;
                this.sleepMillis = (int)(Math.random() * 1000);
                logger.info("create sub thead task: {}", taskName);
            }

            @Override
            public String call() throws Exception {
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("{} done, costs {}ms", taskName, sleepMillis);
                return taskName;
            }
        }

        StopwatchLog stopwatchLog = new StopwatchLog("testParallel", "TestLog");
        List<FutureTask<String>> futureTasks = new ArrayList<>();
        ExecutorService es = Executors.newFixedThreadPool(5);
        for (int i = 1; i <= 5; i++) {
            String taskName = "task-" + i;
            FutureTask<String> futureTask = new FutureTask<>(new ComputeTask(taskName));
            futureTasks.add(futureTask);
            stopwatchLog.start(taskName);
            es.submit(futureTask);
        }
        for (FutureTask<String> task : futureTasks) {
            try {
                String taskName = task.get();
                stopwatchLog.stop(taskName);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        stopwatchLog.info();
        es.shutdown();
    }

    @org.junit.Test
    public void testConfig() {
        StopwatchLog stopwatchLog = new StopwatchLog("test-conf", "TestLog").start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopwatchLog.stopAndInfo();
    }
}
