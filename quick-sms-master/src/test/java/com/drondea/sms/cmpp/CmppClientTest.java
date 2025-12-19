package com.drondea.sms.cmpp;
import com.drondea.sms.handler.cmpp.CmppClientCustomHandler;
import com.drondea.sms.session.cmpp.CmppClientSessionManager;
import com.drondea.sms.conf.cmpp.CmppClientSocketConfig;
import com.drondea.sms.type.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 * @version V3.0.0
 * @description: 客户端测试
 * @author: 刘彦宁
 * @date: 2020年06月10日10:37
 **/
public class CmppClientTest {
    public static void main(String[] args) throws InterruptedException {
//        GlobalConstants.METRICS_CONSOLE_ON = true;
        String host = "127.0.0.1";
        //滑动窗口建议值为16，窗口大小即为一次向server端提交的数量（可以收不到Response）
        CmppClientSocketConfig socketConfig = new CmppClientSocketConfig("test",
                10 * 1000, 16, host, 7892);
        socketConfig.setChannelSize(1);
        socketConfig.setUserName("100001");
//        socketConfig.setUserName("100001");
        socketConfig.setPassword("123123");
//        socketConfig.setPassword("123123");
        socketConfig.setVersion(CmppConstants.VERSION_20);
//        socketConfig.setIdleTime(120);
        //限速 条/s
//        socketConfig.setQpsLimit(10);
        //移除标签
//        socketConfig.setSignatureDirection(SignatureDirection.CHANNEL_FIXED);
//        socketConfig.setSignaturePosition(SignaturePosition.PREFIX);
//        socketConfig.setSmsSignature("【二进制科技】");
        //开启超时监控,设置监控间隔时间，这个值最好是RequestExpiryTimeout的1/2
        socketConfig.setWindowMonitorInterval(10 * 1000);
        //设置响应超时时间
        socketConfig.setRequestExpiryTimeout(20 * 1000);
        CmppClientCustomHandler cmppCustomHandler = new CmppClientCustomHandler();
        CmppClientSessionManager sessionManager = new CmppClientSessionManager(socketConfig, cmppCustomHandler);
        //创建链接
        sessionManager.doOpen();
        sessionManager.doCheckSessions();
        try {
            String str = new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("已退出!");
    }
}
