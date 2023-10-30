package com.wsz.gateway.dynamic;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * InitializingBean作用是当前类所有的属性加载完成后，执行定义在afterPropertiesSet方法中的逻辑
 */
@Slf4j
@Configuration
public class DynamicRoutesLoader implements InitializingBean {
    @Autowired
    private NacosConfigManager configManager;

    @Autowired
    private NacosConfigProperties configProps;

    @Autowired
    private DynamicRoutesListener dynamicRoutesListener;

    private static final String ROUTES_CONFIG = "routes-config.json";

    /**
     * 执行两项任务
     * 调用Nacos提供的NacosConfigManager类加载指定的路由配置文件routes-config.json
     * 将定义的DynamicRoutesListener注册到routes-config.json文件的监听列表中，每次文件发生变动，监听器就能获取到通知
      * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 首次加载的配置
        String routes = configManager.getConfigService().getConfig(ROUTES_CONFIG, configProps.getGroup(), 10000);
        dynamicRoutesListener.receiveConfigInfo(routes);

        // 注册监听器
        configManager.getConfigService().addListener(ROUTES_CONFIG,
                configProps.getGroup(),
                dynamicRoutesListener);
    }
}
