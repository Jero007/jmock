package com.jero.api.jmock.server;


import com.jero.api.jmock.xml.XmlAdaptor;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

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
public class XmlHandler implements JHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private XmlAdaptor xmlAdaptor;

    @Value("${jmock.packet.mode}")
    private Integer mode;

    @Autowired
    private TcpClient client;

    @Override
    public void handle(Channel channel,Object rcvData) {

        if (rcvData instanceof String) {
            Document rcvXml = xmlAdaptor.parse((String) rcvData);
            if (rcvXml == null) {
                this.error(405, "请求报文解析异常，请检查", (SocketChannel)channel);
                return;
            }
            String path = xmlAdaptor.getRespPath(rcvXml);
            logger.info("=====> 解析后的挡板文件地址：{}",path);
            if (path == null) {
                this.error(403, "返回报文路径解析失败", (SocketChannel) channel);
                return;
            }
            String fileString = this.readFile(path);
            if (null == fileString && this.mode == 0) {
                this.error(404, "返回报文不存在", (SocketChannel) channel);
                return;
            }
            if (null == fileString && this.mode == 1) {
                //往后端系统转发
                logger.info("=====> 无挡板，将转发到后端系统 =====>");
                fileString = client.send((String)rcvData);
            }

            logger.info("ret: "+fileString);
            try {
                ((SocketChannel) channel).write(Charset.forName("UTF-8").encode(fileString));
            } catch (IOException e) {
                logger.error("channel write error", e);
                this.error(500, "通道写入异常", (SocketChannel) channel);
            }

        } else {
            logger.error("require string type");
            this.error(401, "请求报文数据异常", (SocketChannel)channel);
        }

    }

    private void error(int code, String message, SocketChannel channel) {

        Document document = DocumentHelper.createDocument();
        Element serviceEle = document.addElement("service");
        Element codeEle = serviceEle.addElement("code");
        codeEle.setText(code+"");
        Element msgEle = serviceEle.addElement("message");
        msgEle.setText(message);

        try {
            channel.write(Charset.forName("UTF-8").encode(document.asXML()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile(String path) {

        File resp = new File(path);
        if (!resp.exists()) {
            return null;
        }
        logger.info("=====> 有挡板，将读取本地xml =====>");
        StringBuilder sb = new StringBuilder();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(resp);
            FileChannel fileChannel = fileInputStream.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
            int length;
            while ((length = fileChannel.read(byteBuffer)) != -1) {
                sb.append(new String(byteBuffer.array()), 0, length);
                byteBuffer.clear();
            }
            fileChannel.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();

    }
}
