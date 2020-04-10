package com.jero.api.jmock.server;

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
public interface JMockServer {

    void start();

    void restart();

    void shutdown();

    boolean isAlive();

}
