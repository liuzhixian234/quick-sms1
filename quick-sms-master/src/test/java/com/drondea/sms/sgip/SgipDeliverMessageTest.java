package com.drondea.sms.sgip;

import com.drondea.sms.channel.ChannelSession;
import com.drondea.sms.common.SequenceNumber;
import com.drondea.sms.common.util.SgipSequenceNumber;
import com.drondea.sms.conf.sgip.SgipClientSocketConfig;
import com.drondea.sms.handler.sgip.SgipClientCustomHandler;
import com.drondea.sms.message.sgip12.SgipDeliverRequestMessage;
import com.drondea.sms.session.sgip.SgipClientSessionManager;
import com.drondea.sms.type.GlobalConstants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * @version V3.0.0
 * @description: SGIP上行短信测试类(模拟用户回复短信)
 * @author:
 * @date:
 **/
@Slf4j
public class SgipDeliverMessageTest {
    public static void main(String[] args) throws InterruptedException {
        GlobalConstants.METRICS_CONSOLE_ON = false;
        String host = "127.0.0.1";
        int port = 8802; // 连接到SgipServerTest的端口

        // 配置客户端
        SgipClientSocketConfig socketConfig = new SgipClientSocketConfig("deliver-test",
                10 * 1000, 16, host, port);
        socketConfig.setChannelSize(1);
        socketConfig.setNodeId(1l);
        socketConfig.setUserName("100001");
        socketConfig.setPassword("123123");
        // 设置更长的空闲时间，避免连接过早断开
        socketConfig.setIdleTime(120);
        socketConfig.setLoginType((short) 1);

        // 自定义处理器，在登录成功后发送上行短信
        SgipClientCustomHandler customHandler = new SgipClientCustomHandler() {
            @Override
            public void fireUserLogin(Channel channel, ChannelSession channelSession) {
                super.fireUserLogin(channel, channelSession);
                System.out.println("客户端登录成功，连接状态: " + channel.isActive());

                // 定期检查连接状态
                channel.eventLoop().scheduleAtFixedRate(() -> {
                    System.out.println("连接活跃状态: " + channel.isActive() +
                            ", 可写状态: " + channel.isWritable());
                    sendDeliverMessage(channelSession);
                }, 0, 10, TimeUnit.SECONDS);
            }
        };

        // 创建并启动客户端会话
        SgipClientSessionManager sessionManager = new SgipClientSessionManager(socketConfig, customHandler);
        sessionManager.doOpen();

        System.out.println("客户端已启动，连接到服务器 " + host + ":" + port);

        try {
            String str = new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("客户端已退出!");
    }

    /**
     * 发送上行短信(模拟用户回复)
     */
    private static void sendDeliverMessage(ChannelSession channelSession) {
        try {
            SgipDeliverRequestMessage deliverMessage = new SgipDeliverRequestMessage();
            SequenceNumber sequenceNumber = channelSession.getSequenceNumber();
            SgipSequenceNumber sgipSequenceNumber = new SgipSequenceNumber(3027130067L, sequenceNumber.next());
            deliverMessage.getHeader().setSequenceNumber(sgipSequenceNumber);

            // 设置上行短信内容
            deliverMessage.setSpNumber("100001");        // SP号码
            deliverMessage.setUserNumber("13800138000");  // 用户手机号
            deliverMessage.setMsgContent("用户回复测试短信内容"); // 短信内容

            // 设置编码格式
            deliverMessage.setMessageCoding(com.drondea.sms.type.SgipConstants.DEFAULT_MSG_FMT);

            // 发送消息
            ChannelFuture channelFuture = channelSession.sendMessage(deliverMessage);
            log.info("channelFuture===={}", channelFuture.toString());
            System.out.println("上行短信已发送: " + deliverMessage.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
