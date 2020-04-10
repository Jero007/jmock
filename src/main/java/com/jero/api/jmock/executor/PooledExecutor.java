package com.jero.api.jmock.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.*;

/**
 * @Description TODO
 * @Date 2020-04-07
 * @Author jero
 * @Version 1.0
 * @ModifyNote (add note when you modify)
 * |---modifyText:
 * |---modifyDate:
 * |---modifyAuthor:
 */

public class PooledExecutor implements JExecutor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static int DEFAULT_QUEUESIZE = 10;
    private static int DEFAULT_MINSIZE = 1;
    private static int DEFAULT_MAXSIZE = 10;
    private static int DEFAULT_INITSIZE = 5;
    private static int DEFAULT_KRRPALIVETIME = 300;

    @Value("${jmock.executor.name}")
    private String name;
    @Value("${jmock.executor.queue.size}")
    private int queueSize;
    private int count;
    @Value("${jmock.executor.min.size}")
    private int minSize;
    @Value("${jmock.executor.max.size}")
    private int maxSize;
    @Value("${jmock.executor.init.size}")
    private int initSize;
    @Value("${jmock.executor.keep.alive.time}")
    private int keepAliveTime;
    private ThreadPoolExecutor executor;
    private boolean alive = false;

    public PooledExecutor() {
        this.minSize = DEFAULT_MINSIZE;
        this.queueSize = DEFAULT_QUEUESIZE;
        this.maxSize = DEFAULT_MAXSIZE;
        this.initSize = DEFAULT_INITSIZE;
        this.keepAliveTime = DEFAULT_KRRPALIVETIME;

    }
    @Override
    public void execute(Runnable runnable) throws InterruptedException {

        if (this.executor == null) {
            this.start();
        }

        this.executor.execute(runnable);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public synchronized void start() {

        if (!this.isAlive()) {
            this.alive = true;
            BlockingQueue queue = new ArrayBlockingQueue(this.queueSize);
            ThreadFactory threadFactory = run -> new Thread(run, (PooledExecutor.this.name != null ? PooledExecutor.this.name : "threadpool") + "-" + PooledExecutor.this.count++);
            RejectedExecutionHandler rejHandler = new ThreadPoolExecutor.CallerRunsPolicy();
            this.executor = new ThreadPoolExecutor(this.minSize, this.maxSize, (long) this.keepAliveTime, TimeUnit.SECONDS, queue, threadFactory, rejHandler);

            logger.info(this + "init success.");
        }
    }

    @Override
    public synchronized void shutdown() throws InterruptedException {
        if (this.executor != null) {
            this.executor.shutdownNow();
        }

        this.alive = false;
    }

    @Override
    public boolean isAlive() {
        return this.alive;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.name == null ? "threadpool" : this.name);
        stringBuilder.append("[queue=").append(this.queueSize);
        stringBuilder.append(",init=").append(this.initSize);
        stringBuilder.append(",min=").append(this.minSize);
        stringBuilder.append(",max=").append(this.maxSize);
        stringBuilder.append(",keepAlive=").append(this.keepAliveTime).append(" sec]");
        return stringBuilder.toString();
    }
}
