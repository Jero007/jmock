package com.jero.api.jmock;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

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
public class NioServerTests {

    @Test
    public void test() throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(9901));

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        int count = 0;
        while (true) {
            if (selector.select() == 0) {
                continue;
            }

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    System.out.println("key.isAcceptable()");
                    ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = serverSocketChannel1.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(key.selector(), SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    System.out.println("key.isReadable()");
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                    StringBuilder sb = new StringBuilder();

                    int readBytes = 0;
                    int ret = 0;
                    while ((ret = socketChannel.read(byteBuffer)) > 0) {
                        readBytes += ret;
                        byteBuffer.flip();
                        sb.append(Charset.forName("UTF-8").decode(byteBuffer).toString());
                        byteBuffer.clear();
                    }
                    if (readBytes == 0) {
                        System.err.println("handle opposite close Exception");
                        socketChannel.close();
                        continue;
                    }

                    System.out.println("第"+(++count)+"次收到消息...");
                    System.out.println("rcv: "+sb.toString());
                    String response = "Server received message: " + sb.toString();
                    System.out.println("send: "+response);
                    System.out.println("===========================");
                    ByteBuffer byteBuffer2 = ByteBuffer.allocate(4);
                    byteBuffer2.put(response.getBytes());
                    socketChannel.write(byteBuffer2);
                }
                keyIterator.remove();
            }
        }

    }

}
