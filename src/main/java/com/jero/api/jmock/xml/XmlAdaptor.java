package com.jero.api.jmock.xml;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;

/**
 * @Description 对xml进行解析
 * @Date 2020-04-08
 * @Author jero
 * @Version 1.0
 * @ModifyNote (add note when you modify)
 * |---modifyText:
 * |---modifyDate:
 * |---modifyAuthor:
 */
public class XmlAdaptor implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${jmock.packet.response.name.prefix}")
    private String prefix;
    private PrefixRule[] prefixRule;
    @Value("${jmock.packet.basePath}")
    private String basePath;

    public Document parse( String oridata) {
        try {
            int start = oridata.indexOf("<?xml");
            if (start < 0) {
                logger.error("xml style error");
                return null;
            }
            oridata = oridata.substring(start);
            Document document = DocumentHelper.parseText(oridata);
            return document;
        } catch (DocumentException e) {
            logger.error("parse error.",e);
            return null;
        }
    }

    public String getRespPath( Document document) {

        StringBuilder sb = new StringBuilder(this.basePath);
        try {

            for (int i = 0; i < this.prefixRule.length; i++) {
                PrefixRule tempRule = this.prefixRule[i];
                Element ele = document.getRootElement();
                for (; tempRule != null; ) {
                    if (tempRule.getAttrName() != null) {
                        for (Iterator j = ele.elementIterator(); j.hasNext(); ) {
                            Element tempEle = (Element) j.next();
                            if (tempRule.getElementName().equals(tempEle.getName()) && tempRule.getAttrName().equals(tempEle.attributeValue("name"))) {
                                ele = tempEle;
                                break;
                            }
                        }
                    }else {
                        ele = ele.element(tempRule.getElementName()) == null ? ele : ele.element(tempRule.getElementName());
                    }
                    tempRule = tempRule.getNext();
                }
                sb.append(ele.getText());
                if (i < this.prefixRule.length - 1) {
                    sb.append("&");
                } else {
                    sb.append(".xml");
                }
            }

        } catch (NullPointerException e) {
            logger.error("xml do not have the element in prefix");
            return null;
        }

        return sb.toString();
    }

    @Override
    public void afterPropertiesSet() {

        Stack<PrefixRule> innerStack = new Stack<>();
        String[] temp = this.prefix.split("&");
        PrefixRule[] init = new PrefixRule[temp.length];

        for (int i = 0; i < temp.length; i++) {
            String[] elementNames = temp[i].split("\\.");
            for (String elementName : elementNames) {
                PrefixRule ele = new PrefixRule();
                if (elementName.indexOf("[") >= 0) {
                    String eleName = elementName.substring(0, elementName.indexOf("["));
                    String attrName = elementName.substring(elementName.indexOf("[") + 1, elementName.indexOf("]"));

                    ele.setElementName(eleName);
                    ele.setAttrName(attrName);
                } else {
                    ele.setElementName(elementName);
                }

                if (!innerStack.empty()) {
                    PrefixRule parent = innerStack.peek();
                    parent.setNext(ele);
                }
                innerStack.push(ele);
            }

            PrefixRule root = null;
            while (root == null) {
                PrefixRule tempRule = innerStack.pop();
                if (innerStack.empty()) {
                    root = tempRule;
                }
            }

            init[i] = root;
            logger.debug("=====> PrefixRule[{}] =>{}",i,root);
        }

        this.prefixRule = init;

    }

    private void printElement(Element element) {
        if (element.isTextOnly()) {
            System.out.println(element.getName()+" start...");
            System.out.println("value = "+element.getText());
            System.out.println(element.getName()+" end...");
        } else {
            System.out.println(element.getName()+" start...");
            for (Iterator i = element.elementIterator(); i.hasNext(); ) {
                printElement((Element) i.next());
            }
            System.out.println(element.getName()+" end...");
        }
    }


    public static void main(String[] args) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                + "<service>\r\n"
                + "<head>didi</head>\r\n"
                + "<body>"
                + "<data name=\"money\">"
                + "<field>12.34</field>"
                + "</data>"
                + "<data name=\"rate\">"
                + "<field>0.01</field>"
                + "</data>"
                + "</body>"
                + "</service>\r\n";
        XmlAdaptor xmlAdaptor = new XmlAdaptor();
        Document document = xmlAdaptor.parse(xml);

        Element rootElement = document.getRootElement();
        xmlAdaptor.printElement(rootElement);

        System.out.println("===========");
        String ss = ((Element)rootElement.element("body").elements("data").get(1)).element("field").getText();
        System.out.println(ss);
    }
}
