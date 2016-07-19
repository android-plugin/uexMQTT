package org.zywx.wbpalmstar.plugin.uexmqtt.vo;

import java.io.Serializable;

/**
 * Created by ylt on 16/7/15.
 */

public class SubscribeVO implements Serializable {


    private static final long serialVersionUID = 7210205516239041644L;

    public String topic;

    public int qos;

}
