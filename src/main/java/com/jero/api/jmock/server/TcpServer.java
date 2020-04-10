package com.jero.api.jmock.server;

import com.jero.api.jmock.executor.JExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @Description TCP服务入口
 * @Date 2020-04-07
 * @Author jero
 * @Version 1.0
 * @ModifyNote (add note when you modify)
 * |---modifyText:
 * |---modifyDate:
 * |---modifyAuthor:
 */
public class TcpServer implements JMockServer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean alive = false;
    private Selector selector;

    @Value("${jmock.tcpserver.name}")
    private String name ;

    @Value("${jmock.tcpserver.port}")
    private int port;

    @Autowired
    private JExecutor executor;

    @Autowired
    private JHandler handler;

    @Override
    public synchronized void start() {

        if (!this.alive) {
            this.alive = true;
            if (this.selector == null && !this.create()) {
                this.alive = false;
                return;
            }
            logger.info("starting..."+this);
            if (this.executor != null && !this.executor.isAlive()) {
                executor.start();
            }
            Runnable accepting = () -> {
                while (TcpServer.this.isAlive()) {
                    Selector selector = TcpServer.this.selector;
                    try {

                        if (selector.select() == 0) {
                            continue;
                        }
                        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();
                            keyIterator.remove();
                            if (key.isAcceptable()) {
                                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                                SocketChannel socketChannel = serverSocketChannel.accept();
                                socketChannel.configureBlocking(false);
                                socketChannel.register(key.selector(), SelectionKey.OP_READ);
                            } else if (key.isReadable()) {
                                TcpServer.this.handle(TcpServer.this.handler,key.channel());
                            }

                        }

                    } catch (IOException e) {
                        TcpServer.this.logger.error(TcpServer.this + " closed.");
                    }
                }
            };
            Thread acceptingThread = new Thread(accepting, this.toString() + "-main");
            acceptingThread.start();
            logger.info("started..."+this);
        }
    }

    private void handle(final JHandler handler, final SelectableChannel channel) {
        try {

            SocketChannel socketChannel = (SocketChannel) channel;
            if (!socketChannel.isConnected()) {
                return;
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            StringBuilder sb = new StringBuilder();

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
                    return;
                }

                logger.info("rcv: "+sb.toString());

            } catch (IOException e) {
                logger.error("",e);
            }
            this.executor.execute(() -> handler.handle(channel,sb.toString()));
        } catch (InterruptedException e) {
            logger.error("handle error.",e);
        }

    }

    private boolean create() {

        ServerSocketChannel serverSocketChannel;
        Selector selector;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(this.port));


            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            this.logger.error("create server socket error.",e);
            return false;
        }

        this.selector = selector;

        return true;
    }

    @Override
    public void restart() {
        this.shutdown();
        this.start();
    }

    @Override
    public void shutdown() {

        if (this.isAlive()) {
            try {
                this.alive = false;
                this.selector.close();
            } catch (IOException e) {
                this.logger.error("close server socket error.",e);
            }

            if (this.executor != null && this.executor.isAlive()) {
                try {
                    this.executor.shutdown();
                } catch (InterruptedException e) {
                    this.logger.error("close executor error.",e);
                }
            }
        }
    }

    @Override
    public boolean isAlive() {
        return this.alive;
    }

    @Override
    public String toString() {
        return (this.name != null ? this.name : "TcpServer") + super.toString();
    }


}
