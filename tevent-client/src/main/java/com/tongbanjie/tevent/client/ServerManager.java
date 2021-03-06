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
package com.tongbanjie.tevent.client;

import com.tongbanjie.tevent.client.sender.MQMessageSender;
import com.tongbanjie.tevent.registry.Address;
import com.tongbanjie.tevent.registry.RecoverableRegistry;
import com.tongbanjie.tevent.cluster.loadbalance.LoadBalance;
import com.tongbanjie.tevent.cluster.loadbalance.RandomLoadBalance;
import com.tongbanjie.tevent.rpc.RpcClient;
import com.tongbanjie.tevent.rpc.exception.*;
import com.tongbanjie.tevent.rpc.protocol.RequestCode;
import com.tongbanjie.tevent.rpc.protocol.RpcCommand;
import com.tongbanjie.tevent.rpc.protocol.RpcCommandBuilder;
import com.tongbanjie.tevent.rpc.protocol.body.HeartbeatData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端 管理者<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/10/15
 */
public class ServerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerManager.class);

    private final RecoverableRegistry clientRegistry;

    private final RpcClient rpcClient;

    private final ConcurrentHashMap<String/* group */, MQMessageSender> messageSenderTable;

    private ThreadLocal<LoadBalance<Address>> loadBalanceThreadLocal = new ThreadLocal<LoadBalance<Address>>(){
        @Override
        protected LoadBalance<Address> initialValue() {
            return new RandomLoadBalance();
        }
    };

    public ServerManager(ClientController clientController) {
        this.clientRegistry = clientController.getClientRegistry();
        this.rpcClient = clientController.getRpcClient();
        this.messageSenderTable = clientController.getMessageSenderTable();
    }

    public void sendHeartbeatToAllServer(){
        List<Address> copy = clientRegistry.getDiscovered();
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Start to send heartbeat to {} servers.",  copy.size());
        }
        for(Address address : copy){
            sendHeartbeatToServer(address);
        }
    }

    private void sendHeartbeatToServer(final Address serverAddr){
        for(String producerGroup : this.messageSenderTable.keySet()){
            HeartbeatData heartbeatData = new HeartbeatData();
            heartbeatData.setClientId("");//TODO clientId
            heartbeatData.setGroup(producerGroup);
            sendHeartbeat(serverAddr, heartbeatData, 3000);
        }
    }

    private void sendHeartbeat(final Address serverAddr,
                              final HeartbeatData heartbeatData,
                              final long timeoutMillis ) {
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Send heartbeat to server {}.", serverAddr);
        }
        RpcCommand request = RpcCommandBuilder.buildRequest(RequestCode.HEART_BEAT, null);
        request.setBody(heartbeatData);

        try {
            //用oneWay方式即可
            this.rpcClient.invokeOneway(serverAddr.getAddress(), request, timeoutMillis);
        } catch (RpcConnectException e) {
            LOGGER.warn("Send heartbeat to server " + serverAddr +
                    " exception, maybe lose connection with the sever.", e);
        } catch (RpcException e) {
            LOGGER.warn("Send heartbeat to server " + serverAddr + " exception.", e);
        } catch (Exception e) {
            LOGGER.warn("Send heartbeat to server " + serverAddr + " exception.", e);
        }

    }

    public Address discover() {
        List<Address> copy = clientRegistry.getDiscovered();

        Address address = loadBalanceThreadLocal.get().select(copy);

        if(address == null){
            LOGGER.warn("Can not find a server.");
            return null;
        }
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Find a server {}", address);
        }
        return address;
    }


}
