package com.drondea.sms.sgip;

import com.drondea.sms.conf.sgip.SgipServerSocketConfig;
import com.drondea.sms.handler.sgip.SgipServerCustomHandler;
import com.drondea.sms.message.sgip12.SgipDeliverRequestMessage;
import com.drondea.sms.message.sgip12.SgipReportRequestMessage;
import com.drondea.sms.session.sgip.SgipServerSessionManager;
import com.drondea.sms.type.GlobalConstants;
import com.drondea.sms.type.UserChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @version V3.0.0
 * @description: SGIP客户端回执和上行短信监听测试
 * @author: liyuehai
 * @date: 2020年06月10日10:37
 **/
public class SgipClientReportAndDeliverTest {
    public static void main(String[] args) throws InterruptedException {
        GlobalConstants.METRICS_CONSOLE_ON = false;

        // 配置监听端口（用于接收回执和上行短信）
        int listenPort = 8803;  // 监听端口

        // 启动监听服务
        startListener(listenPort);

        System.out.println("SGIP回执和上行短信监听服务已启动，端口: " + listenPort);
        System.out.println("等待网关连接并推送回执和上行短信...");

        try {
            String str = new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("已退出!");
    }

    /**
     * 启动监听服务，用于接收回执和上行短信
     */
    private static void startListener(int port) {
        SgipServerSocketConfig socketConfig = new SgipServerSocketConfig("sgip-report-deliver-server", port);
        // 设置空闲时间
        socketConfig.setIdleTime(60);

        SgipServerCustomHandler customHandler = new SgipServerCustomHandler() {
            @Override
            public void configPipelineAfterLogin(ChannelPipeline pipeline) {
                // 调用父类方法添加原有处理器
                super.configPipelineAfterLogin(pipeline);

                // 添加自定义处理器来处理回执和上行短信
                pipeline.addBefore("NettyTailHandler", "CustomReportDeliverHandler", new SimpleChannelInboundHandler<Object>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg instanceof SgipReportRequestMessage) {
                            handleReportMessage(ctx, (SgipReportRequestMessage) msg);
                        } else if (msg instanceof SgipDeliverRequestMessage) {
                            handleDeliverMessage(ctx, (SgipDeliverRequestMessage) msg);
                        } else {
                            // 传递给下一个处理器
                            ctx.fireChannelRead(msg);
                        }
                    }
                });
            }
        };


        SgipServerSessionManager sessionManager = new SgipServerSessionManager(name -> {
            if (name.startsWith("100001")) {
                UserChannelConfig userChannelConfig = new UserChannelConfig();
                userChannelConfig.setUserName(name);
                userChannelConfig.setPassword("123123");
                userChannelConfig.setChannelLimit(3);
                return userChannelConfig;
            }
            return null;
        }, socketConfig, customHandler);

        sessionManager.doOpen();
    }

    /**
     * 处理回执消息的方法
     *
     * @param ctx           ChannelHandlerContext
     * @param reportMessage 回执消息
     */
    private static void handleReportMessage(ChannelHandlerContext ctx, SgipReportRequestMessage reportMessage) {
        System.out.println("=== 接收到短信回执消息 ===");
        System.out.println("序列号: " + reportMessage.getSubmitSequenceNumber());
        System.out.println("提交序列号: " + reportMessage.getSubmitSequenceNumber());
        System.out.println("报告类型: " + reportMessage.getReportType());
        System.out.println("用户号码: " + reportMessage.getUserNumber());
        System.out.println("状态: " + reportMessage.getState());
        System.out.println("错误码: " + reportMessage.getErrorCode());
        System.out.println("================================");

        // TODO: 在这里添加回执消息的具体业务处理逻辑
        // 比如更新数据库中的短信状态等
    }

    /**
     * 处理上行短信消息的方法
     *
     * @param ctx            ChannelHandlerContext
     * @param deliverMessage 上行短信消息
     */
    private static void handleDeliverMessage(ChannelHandlerContext ctx, SgipDeliverRequestMessage deliverMessage) {
        System.out.println("=== 接收到上行短信消息 ===");
        System.out.println("序列号: " + deliverMessage.getSequenceNum());
        System.out.println("SP编号: " + deliverMessage.getSpNumber());
        System.out.println("用户号码: " + deliverMessage.getUserNumber());
        System.out.println("短信内容: " + deliverMessage.getMsgContent());
        System.out.println("================================");

        // TODO: 在这里添加上行短信的具体业务处理逻辑
        // 比如保存到数据库、转发给业务系统等
    }
}
