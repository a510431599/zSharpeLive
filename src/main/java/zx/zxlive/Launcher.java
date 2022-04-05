package zx.zxlive;/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2016 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

import cn.hutool.core.thread.ThreadUtil;
import org.springframework.context.ConfigurableApplicationContext;
import zx.zxlive.core.logging.Red5LoggerFactory;
import zx.zxlive.core.server.api.Red5;
import org.slf4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import zx.zxlive.core.server.net.rtmp.RTMPConnManager;

import java.util.concurrent.TimeUnit;

/**
 * Launches Red5.
 *
 * @author The zSharpe Project
 * @author Vic Wang (xiaoyu860912@163.com)
 */
@EnableMBeanExport
@SpringBootApplication
public class Launcher {

    /**
     * Launch Red5 under it's own classloader
     *
     * @throws Exception on error
     */
    public void launch() throws Exception {
        System.out.printf("Root: %s%nDeploy type: %s%n", System.getProperty("red5.root"), System.getProperty("red5.deployment.type"));
        // check for the logback disable flag
        boolean useLogback = Boolean.valueOf(System.getProperty("useLogback", "true"));
        if (useLogback) {
            // check for context selector in system properties
            if (System.getProperty("logback.ContextSelector") == null) {
                // set our selector
                System.setProperty("logback.ContextSelector", "org.red5.logging.LoggingContextSelector");
            }
        }
        Red5LoggerFactory.setUseLogback(useLogback);
        // install the slf4j bridge (mostly for JUL logging)
        SLF4JBridgeHandler.install();
        // log stdout and stderr to slf4j
        //SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
        // get the first logger
        Logger log = Red5LoggerFactory.getLogger(Launcher.class);
        // version info banner
        log.info("{} (https://github.com/Red5)", Red5.getVersion());
        if (log.isDebugEnabled()) {
            log.debug("fmsVer: {}", Red5.getFMSVersion());
        }
        // create red5 app context
        @SuppressWarnings("resource")
        FileSystemXmlApplicationContext root = new FileSystemXmlApplicationContext(new String[]{"classpath:/red5.xml"}, false);
        // set the current threads classloader as the loader for the factory/appctx
        root.setClassLoader(Thread.currentThread().getContextClassLoader());
        root.setId("red5.root");
        root.setBeanName("red5.root");
        // refresh must be called before accessing the bean factory
        log.trace("Refreshing root server context");
        root.refresh();
        log.trace("Root server context refreshed");
        log.debug("zx.zxlive.core.server.Launcher exit");
    }

    public static void main(String[] args) {
        SpringApplication.run(Launcher.class, args);
//        System.out.println(applicationContext.getBean(RTMPConnManager.class));
//        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
//        int noThreads = threadGroup.activeCount();
//        Thread[] lstThreads = new Thread[noThreads];
//        threadGroup.enumerate(lstThreads);
//        for (int i = 0; i < noThreads; i++)
//            System.out.println("线程号：" + i + " = " + lstThreads[i].getName());
//        ThreadUtil.createScheduledExecutor(1).schedule(() -> {
//            System.exit(0);
//        }, 100, TimeUnit.SECONDS);

    }

}
