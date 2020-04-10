package com.jero.api.jmock.server;

import java.nio.channels.Channel;

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
public interface JHandler {
    void handle(Channel channel,Object rcvData);
}
