package com.jero.api.jmock.config;

import com.jero.api.jmock.executor.JExecutor;
import com.jero.api.jmock.executor.PooledExecutor;
import com.jero.api.jmock.server.*;
import com.jero.api.jmock.xml.XmlAdaptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
@Configuration
public class JBeanConfig {

    @Bean(initMethod = "start")
    public JExecutor pooledExecutor() {
        JExecutor executor = new PooledExecutor();
        return executor;
    }

    @Bean
    public JHandler xmlHandler() {
        JHandler jHandler = new XmlHandler();
        return jHandler;
    }

    @Bean(initMethod = "start")
    public JMockServer tcpServer() {
        JMockServer jMockServer = new TcpServer();
        return jMockServer;
    }

    @Bean
    public XmlAdaptor xmlAdaptor() {
        return new XmlAdaptor();
    }

    @Bean
    public TcpClient tcpClient() {
        return new TcpClient();
    }

}
