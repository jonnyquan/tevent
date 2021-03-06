package com.tongbanjie.tevent.server.processer;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.tongbanjie.tevent.common.message.MQType;
import com.tongbanjie.tevent.common.message.TransactionState;
import com.tongbanjie.tevent.rpc.exception.RpcCommandException;
import com.tongbanjie.tevent.rpc.netty.NettyRequestProcessor;
import com.tongbanjie.tevent.rpc.protocol.RequestCode;
import com.tongbanjie.tevent.rpc.protocol.ResponseCode;
import com.tongbanjie.tevent.rpc.protocol.RpcCommand;
import com.tongbanjie.tevent.rpc.protocol.RpcCommandBuilder;
import com.tongbanjie.tevent.rpc.protocol.header.SendMessageHeader;
import com.tongbanjie.tevent.rpc.protocol.header.TransactionMessageHeader;
import com.tongbanjie.tevent.server.ServerController;
import com.tongbanjie.tevent.server.mq.EventProducer;
import com.tongbanjie.tevent.server.mq.EventProducerFactory;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 〈一句话功能简述〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/9/30
 */
public class SendMessageProcessor implements NettyRequestProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageProcessor.class);

    private final ServerController serverController;

    public SendMessageProcessor(final ServerController serverController) {
        this.serverController = serverController;
    }

    @Override
    public RpcCommand processRequest(ChannelHandlerContext ctx, RpcCommand request) throws Exception {
        switch (request.getCmdCode()) {
            case RequestCode.SEND_MESSAGE:
                return this.sendMessage(ctx, request);
            case RequestCode.TRANSACTION_MESSAGE:
                return this.transactionMessage(ctx, request);
            default:
                LOGGER.warn("Invalid request，requestCode："+request.getCmdCode());
                break;
        }
        return RpcCommandBuilder.buildResponse(ResponseCode.INVALID_REQUEST,
                "Invalid request，requestCode："+request.getCmdCode());
    }

    /**
     * 发送普通消息
     * @param ctx
     * @param request
     * @return
     * @throws MQClientException
     * @throws RpcCommandException
     */
    private RpcCommand sendMessage(ChannelHandlerContext ctx, RpcCommand request) throws MQClientException, RpcCommandException {
        //1、解析并校验 消息头
        SendMessageHeader header = (SendMessageHeader)request.decodeCustomHeader(SendMessageHeader.class);

        //2、获取事件处理者
        EventProducer producer = getProducer(header.getMqType());
        if(producer == null){
            return RpcCommandBuilder.buildResponse(ResponseCode.SYSTEM_ERROR,
                    "System error：can not find a producer to handle the message {}" + header);
        }
        //3、处理事件
        return producer.sendMessage(ctx, request);
    }

    /**
     * 事务消息
     * @param ctx
     * @param request
     * @return
     * @throws RpcCommandException
     */
    private RpcCommand transactionMessage(ChannelHandlerContext ctx, RpcCommand request) throws RpcCommandException {
        //1、解析并校验 消息头
        TransactionMessageHeader header = (TransactionMessageHeader) request.decodeCustomHeader(TransactionMessageHeader.class);
        validateTransactionMessage(header);

        //2、获取事件处理者
        EventProducer producer = getProducer(header.getMqType());
        if(producer == null){
            return RpcCommandBuilder.buildResponse(ResponseCode.SYSTEM_ERROR,
                    "System error：can not find a producer to handle the message {}"+ header);
        }
        //3、处理事件
        switch (header.getTransactionState()){
            case PREPARE:
                return producer.prepareMessage(ctx, request);
            case COMMIT:
                return producer.commitMessage(ctx, request, header.getTransactionId());
            case ROLLBACK:
                return producer.rollbackMessage(ctx, request, header.getTransactionId());
            case UNKNOWN:
                return producer.unknownMessage(ctx, request, header.getTransactionId());
            default:
                break;
        }
        //never goto here
        return RpcCommandBuilder.buildResponse(ResponseCode.INVALID_REQUEST,
                "Param error: transactionState can not be null");
    }

    private void validateTransactionMessage(TransactionMessageHeader header) throws RpcCommandException{
        validateMessage(header);
        if(header.getTransactionState() != null && header.getTransactionState() != TransactionState.PREPARE
                && header.getTransactionId() == null){
            throw new RpcCommandException("Param error: transactionId can not be null when transactionState is " + header.getTransactionState());
        }
    }

    private void validateMessage(SendMessageHeader header) throws RpcCommandException{
        if(header == null){
            throw new RpcCommandException("Param error: messageHeader can not be null");
        }
        if(header.getMqType() == null){
            throw new RpcCommandException("Param error: mqType can not be null");
        }
    }

    private EventProducer getProducer(MQType mqType){
        return EventProducerFactory.getInstance().getAndCreate(mqType, this.serverController);
    }



}
