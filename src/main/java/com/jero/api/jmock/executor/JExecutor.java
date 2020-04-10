package com.jero.api.jmock.executor;

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
public interface JExecutor {

    void execute(Runnable runnable) throws InterruptedException;

    void setName(String name);

    void start();

    void shutdown() throws InterruptedException;

    boolean isAlive();
}
