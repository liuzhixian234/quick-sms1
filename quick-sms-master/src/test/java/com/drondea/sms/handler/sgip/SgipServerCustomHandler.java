package com.drondea.sms.handler.sgip;

import com.drondea.sms.channel.ChannelSession;
import com.drondea.sms.common.SequenceNumber;
import com.drondea.sms.common.util.SgipSequenceNumber;
import com.drondea.sms.handler.TailBizHandler;
import com.drondea.sms.message.IMessage;
import com.drondea.sms.message.sgip12.SgipDeliverRequestMessage;
import com.drondea.sms.type.ICustomHandler;
import com.drondea.sms.type.UserChannelConfig;
import com.drondea.sms.windowing.DuplicateKeyException;
import com.drondea.sms.windowing.OfferTimeoutException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @version V3.0.0
 * @description: sgip的定制处理器
 * @author: liyuehai
 * @date: 2020年06月23日17:35
 **/
public class SgipServerCustomHandler extends ICustomHandler {
    private static final Logger logger = LoggerFactory.getLogger(SgipServerCustomHandler.class);

    @Override
    public void fireUserLogin(Channel channel, ChannelSession channelSession) {
        logger.debug("客户端登录了");
    }



    @Override
    public void channelClosed(ChannelSession channelSession) {
    }

    @Override
    public void configPipelineAfterLogin(ChannelPipeline pipeline) {
        /**
         * ServerSgipSubmitRequestHandler
         * 处理客户端发送的短信提交请求（SGIP_SUBMIT消息）
         * 接收并解析客户端发来的短信内容
         * 进行基本的短信验证和处理
         */
        pipeline.addLast("ServerMessageRecieverHandler", new ServerSgipSubmitRequestHandler());
        /**
         * ServerSgipDeliverRequestHandler
         * 处理上行短信请求（SGIP_DELIVER消息）
         * 接收用户回复给SP的短信内容
         * 处理用户主动发送给服务提供商的短信
         */
        pipeline.addLast("ServerSgipDeliverRequestHandler", new ServerSgipDeliverRequestHandler());
        /**
         * ServerSgipReportRequestHandler
         * 处理短信状态报告请求（SGIP_REPORT消息）
         * 接收短信发送状态的反馈信息
         * 包括短信是否成功送达等状态信息
         */
        pipeline.addLast("ServerSgipReportRequestHandler", new ServerSgipReportRequestHandler());
        pipeline.addLast("NettyTailHandler", new TailBizHandler());
    }

    @Override
    public void responseMessageExpired(Integer sequenceId, IMessage request) {
        System.out.println("短信超时处理" + sequenceId);
    }

    @Override
    public void slidingWindowException(ChannelSession session, ChannelHandlerContext ctx, IMessage message, ChannelPromise promise, Exception exception) {
        logger.error("slidingWindowException", exception);
        int retryCount = message.addRetryCount();
        //失败越多延时越长，防止线程堆积
        int delay = 10;
        if (retryCount >= 30) {
            delay = 500 * retryCount / 10;
        }
        if (retryCount > 20) {
            System.out.println("重试" + retryCount);
        }
        //重写
        ctx.executor().schedule(() -> {
            session.sendWindowMessage(ctx, message, promise);
        }, delay, TimeUnit.MILLISECONDS);
        //滑动窗口key冲突
        if (exception instanceof DuplicateKeyException) {
            return;
        }
        //滑动窗口获取slot超时
        if (exception instanceof OfferTimeoutException) {
            return;
        }
        if (exception instanceof InterruptedException) {
            return;
        }
    }

    @Override
    public boolean customLoginValidate(IMessage message, UserChannelConfig channelConfig, Channel channel) {
        return true;
    }

    @Override
    public void failedLogin(ChannelSession channelSession, IMessage msg, long status) {
    }



}
