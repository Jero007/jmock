package com.jero.api.jmock.xml;

/**
 * @Description 对需要获取的xml字段提前准备好
 * @Date 2020-04-09
 * @Author jero
 * @Version 1.0
 * @ModifyNote (add note when you modify)
 * |---modifyText:
 * |---modifyDate:
 * |---modifyAuthor:
 */
public class PrefixRule {

    private String elementName;
    private String attrName;
    private PrefixRule next;

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public PrefixRule getNext() {
        return next;
    }

    public void setNext(PrefixRule next) {
        this.next = next;
    }
}
