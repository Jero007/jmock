package com.jero.api.jmock.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @Description TODO
 * @Date 2020-04-09
 * @Author jero
 * @Version 1.0
 * @ModifyNote (add note when you modify)
 * |---modifyText:
 * |---modifyDate:
 * |---modifyAuthor:
 */
public class TcpClient {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${jmock.tcpclient.ip}")
    private String ip;
    @Value("${jmock.tcpclient.port}")
    private int port;

    private Selector selector;

    @Value("${jmock.tcpclient.errorString}")
    private String errorString;

    public String send(String data) {

        this.init();

        SocketChannel socketChannel;
        try {
            socketChannel= SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(this.ip, this.port));
            if (null == selector) {
                return this.errorString;
            }
            socketChannel.register(this.selector, SelectionKey.OP_CONNECT);
            StringBuilder sb = new StringBuilder();
            boolean finish = false;
            while (!finish) {
                if (this.selector.select() == 0) {
                    continue;
                }
                Iterator<SelectionKey> iterators = this.selector.selectedKeys().iterator();
                while (iterators.hasNext()) {
                    SelectionKey temp = iterators.next();
                    iterators.remove();

                    if (socketChannel == temp.channel() && temp.isReadable()) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int readBytes = 0;
                        int ret;
                        try {
                            while ((ret = socketChannel.read(byteBuffer)) > 0) {
                                readBytes += ret;
                                byteBuffer.flip();
                                sb.append(Charset.forName("UTF-8").decode(byteBuffer).toString());
                                byteBuffer.clear();
                            }
                            if (readBytes == 0) {
                                socketChannel.close();
                            }

                        } catch (IOException e) {
                            logger.error("",e);
                        }
                        finish = true;
                        break;

                    }
                    if (socketChannel == temp.channel() && temp.isConnectable() && socketChannel.isConnectionPending()) {
                        socketChannel.finishConnect();
                        socketChannel.write(Charset.forName("UTF-8").encode(data));
                        socketChannel.register(temp.selector(), SelectionKey.OP_READ);
                        break;
                    }
                }
            }
            if (socketChannel.isConnected()) {
                socketChannel.close();
            }
            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void init() {
        if (this.selector != null) {
            return;
        }

        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            logger.error("client selector open fail.",e);
        }

    }


}
