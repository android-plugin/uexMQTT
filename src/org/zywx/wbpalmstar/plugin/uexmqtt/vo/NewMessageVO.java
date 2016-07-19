package org.zywx.wbpalmstar.plugin.uexmqtt.vo;

import java.io.Serializable;

/**
 * Created by ylt on 16/7/15.
 */

public class NewMessageVO implements Serializable {

    private static final long serialVersionUID = -8684616436918424345L;

    public String data;

    public String topic;

    public int qos;

    public boolean retainFlag;

    public int mid;

}
