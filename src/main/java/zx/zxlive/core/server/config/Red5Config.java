package zx.zxlive.core.server.config;


import org.apache.mina.integration.beans.InetSocketAddressEditor;
import zx.zxlive.common.cache.impl.NoCacheImpl;
import zx.zxlive.core.server.Context;
import zx.zxlive.core.server.api.IContext;
import zx.zxlive.core.server.scope.GlobalScope;
import zx.zxlive.core.server.scope.WebScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import zx.zxlive.common.io.flv.impl.FLV;

import java.beans.PropertyEditor;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
public class Red5Config {

    @Value("${so.scheduler.pool_size}")
    private int schedulerPoolSize;
    @Value("${rtmp.scheduler.pool_size}")
    private int rtmpSchedulerPoolSize;
    @Value("${rtmp.max_packet_size}")
    private int rtmpMaxPacketSize;
//    @Bean
//    public MBeanServerFactoryBean mbeanServer() {
//        return new MBeanServerFactoryBean();
//    }

//    @Bean
//    public StreamableFileFactory streamableFileFactory() {
//        FLVService flvService = new FLVService();
//        flvService.setGenerateMetadata(true);
//        M4AService m4AService = new M4AService();
//        MP3Service mp3Service = new MP3Service();
//        MP4Service mp4Service = new MP4Service();
//        StreamableFileFactory factory = new StreamableFileFactory();
//        return factory.addService(flvService)
//                .addService(mp3Service)
//                .addService(mp4Service)
//                .addService(m4AService);
//    }

    @Bean("scheduler")
    public ThreadPoolTaskScheduler scheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        scheduler.setDaemon(true);
        scheduler.setThreadNamePrefix("SharedObjectScheduler-");
        return scheduler;
    }

    @Bean("rtmpScheduler")
    public ThreadPoolTaskScheduler rtmpScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(8);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setDaemon(true);
        scheduler.setThreadNamePrefix("RTMPConnectionScheduler-");
        return scheduler;
    }

    @Bean
    public MethodInvokingFactoryBean methodInvokingFactoryBean1() {
        MethodInvokingFactoryBean bean = new MethodInvokingFactoryBean();
        bean.setStaticMethod("zx.zxlive.common.io.flv.impl.FLVReader.setBufferType");
        bean.setArguments("auto");
        return bean;
    }

    @Bean
    public MethodInvokingFactoryBean methodInvokingFactoryBean2() {
        MethodInvokingFactoryBean bean = new MethodInvokingFactoryBean();
        bean.setStaticMethod("zx.zxlive.common.io.flv.impl.FLVReader.setBufferSize");
        bean.setArguments(4096);
        return bean;
    }

    @Bean
    public MethodInvokingFactoryBean methodInvokingFactoryBean3() {
        MethodInvokingFactoryBean bean = new MethodInvokingFactoryBean();
        bean.setStaticMethod("zx.zxlive.core.server.net.rtmp.codec.RTMPProtocolDecoder.setMaxPacketSize");
        bean.setArguments(3145728);
        return bean;
    }

    @Bean
    public MethodInvokingFactoryBean methodInvokingFactoryBean4(FLV flv) {
        MethodInvokingFactoryBean bean = new MethodInvokingFactoryBean();
        bean.setStaticMethod("zx.zxlive.common.io.flv.impl.FLVWriter.setFLV");
        bean.setArguments(flv);
        return bean;
    }

    @Bean
    public NoCacheImpl noCache(){
        return NoCacheImpl.getInstance();
    }

    @Bean
    public FLV flv(NoCacheImpl noCache){
        FLV flv = new FLV();
        flv.setCache(noCache);
        return flv;
    }
    @Bean
    public static CustomEditorConfigurer customEditorConfigurer() {
        CustomEditorConfigurer customEditorConfigurer = new CustomEditorConfigurer();
        Map<Class<?>, Class<? extends PropertyEditor>> customEditors = new HashMap<>();
        customEditors.put(SocketAddress.class, InetSocketAddressEditor.class);
        customEditorConfigurer.setCustomEditors(customEditors);
        return customEditorConfigurer;
    }
    @Bean
    public ScheduledThreadPoolExecutor streamExecutor(){
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(16);
        scheduledThreadPoolExecutor.setMaximumPoolSize(64);
        return scheduledThreadPoolExecutor;
    }

    @Bean
    public Context webContext(){
        return new Context();
    }

    @Bean
    public Context globalContext(){
        return new Context();
    }

    @Bean
//    @DependsOn("globalContext")
    public GlobalScope globalScope(IContext globalContext){
        GlobalScope globalScope = new GlobalScope();
        globalScope.setContext(globalContext);
        return globalScope;
    }
    @Bean
//    @DependsOn("webContext")
    public WebScope webScope(IContext webContext,GlobalScope globalScope){
        WebScope webScope = new WebScope();
        webScope.setContext(webContext);
        webScope.setContextPath("/");
        webScope.setParent(globalScope);
        webScope.setVirtualHosts("localhost");
        return webScope;
    }
}
