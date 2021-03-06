/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.tongbanjie.tevent.server;

import com.tongbanjie.tevent.common.Constants;

public class ServerConfig{

    /**
     * 消息发送线程池大小
     */
    private int sendMessageThreadPoolNums = 16 + Runtime.getRuntime().availableProcessors() * 4;

    /**
     * 客户端管理线程池大小
     */
    private int clientManageThreadPoolNums = 8+ Runtime.getRuntime().availableProcessors();

    /**
     * 消息发送线程池队列容量
     */
    private int sendThreadPoolQueueCapacity = 100000;

    /**
     * serverId，每个server必须唯一
     */
    private int serverId = 1;

    /**
     * 注册中心地址
     */
    private String registryAddress = System.getProperty(Constants.TEVENT_REGISTRY_ADDRESS, "192.168.1.120:2181");

    /**
     * RocketMQ nameserv 地址
     */
    private String rocketMQNamesrv =  System.getProperty(Constants.TEVENT_ROCKETMQ_NAMESRV, "192.168.1.42:9876");


    public int getSendMessageThreadPoolNums() {
        return sendMessageThreadPoolNums;
    }

    public void setSendMessageThreadPoolNums(int sendMessageThreadPoolNums) {
        this.sendMessageThreadPoolNums = sendMessageThreadPoolNums;
    }

    public int getClientManageThreadPoolNums() {
        return clientManageThreadPoolNums;
    }

    public void setClientManageThreadPoolNums(int clientManageThreadPoolNums) {
        this.clientManageThreadPoolNums = clientManageThreadPoolNums;
    }

    public int getSendThreadPoolQueueCapacity() {
        return sendThreadPoolQueueCapacity;
    }

    public void setSendThreadPoolQueueCapacity(int sendThreadPoolQueueCapacity) {
        this.sendThreadPoolQueueCapacity = sendThreadPoolQueueCapacity;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getRocketMQNamesrv() {
        return rocketMQNamesrv;
    }

    public void setRocketMQNamesrv(String rocketMQNamesrv) {
        this.rocketMQNamesrv = rocketMQNamesrv;
    }
}
