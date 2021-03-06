package com.rishiqing.dingtalk.web.listener;

import com.rishiqing.common.log.LogFormatter;
import com.rishiqing.dingtalk.isv.api.service.biz.SuiteDbCheckBizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Wallace Mao
 * Date: 2018-10-31 23:54
 */
public class SuiteDbCheckApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger bizLogger = LoggerFactory.getLogger(SuiteDbCheckApplicationListener.class);
    private static final Long EXECUTE_INIT_DELAY_MILLS = 3000L;
    private static final Long EXECUTE_DELAY_MILLS = 10000L;
    private static boolean isStarted = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if(!isStarted){
            System.out.println("====db auth check starting...");
            XmlWebApplicationContext xmlWebApplicationContext = (XmlWebApplicationContext)contextRefreshedEvent.getApplicationContext();
            final SuiteDbCheckBizService suiteDbCheckBizService = (SuiteDbCheckBizService)xmlWebApplicationContext.getBean("suiteDbCheckBizService");

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try{
                        suiteDbCheckBizService.checkDingPushEvent();
                    } catch (Exception e){
                        bizLogger.error(LogFormatter.format(
                                LogFormatter.LogEvent.EXCEPTION,
                                "SuiteDbCheckApplicationListener"
                        ), e);
                    }
                }
            }, EXECUTE_INIT_DELAY_MILLS, EXECUTE_DELAY_MILLS, TimeUnit.MILLISECONDS);

            System.out.println("====db auth check started");
            isStarted = true;
        }
    }
}
