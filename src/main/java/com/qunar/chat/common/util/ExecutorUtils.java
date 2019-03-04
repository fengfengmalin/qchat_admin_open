package com.qunar.chat.common.util;

import java.util.concurrent.*;

/**
 * Author : open
 * Date : 16-4-6
 */
public class ExecutorUtils {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorUtils.class);

    public static ExecutorService newLimitedCachedThreadPool(int activeNum, int queueNum) {
        return new ThreadPoolExecutor(0, activeNum, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueNum));
    }

    public static ExecutorService newLimitedCachedThreadPool() {
        return newLimitedCachedThreadPool(100, 100);
    }

    public static ExecutorService newLinkedThreadPoll(int activeNum, int queueNum) {
        return new ThreadPoolExecutor(1, activeNum, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(queueNum));
    }
}
