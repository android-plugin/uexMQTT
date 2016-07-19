package org.zywx.wbpalmstar.plugin.uexmqtt.vo;

import java.io.Serializable;

/**
 * Created by ylt on 16/7/15.
 */

public class PublishVO implements Serializable {
    private static final long serialVersionUID = 4677139885299907285L;

    public String id;

    public String topic;

    public int qos;

    public String data;

    public boolean retainFlag;


}
