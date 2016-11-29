package org.zywx.wbpalmstar.plugin.uexmqtt.vo;

import java.io.Serializable;

/**
 * Created by ylt on 16/7/15.
 */

public class ConnectVO implements Serializable {

    private static final long serialVersionUID = -4362055369289019373L;

    public String clientId;

    public String server;

    public int port;

    public String username;

    public String password;

    public int keepAliveInterval=-1;

    public LWTVO LWT;

}
