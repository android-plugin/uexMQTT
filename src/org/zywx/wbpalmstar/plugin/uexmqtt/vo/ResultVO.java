package org.zywx.wbpalmstar.plugin.uexmqtt.vo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ylt on 16/7/15.
 */

public class ResultVO implements Serializable {
    private static final long serialVersionUID = -4645143311596007962L;

    public boolean isSuccess;

    public int errCode;

    public String topic;

    public List<Integer> grantedQoss;

    public String id;




}
