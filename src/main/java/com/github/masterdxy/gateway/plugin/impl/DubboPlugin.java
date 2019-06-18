package com.github.masterdxy.gateway.plugin.impl;

import org.apache.dubbo.rpc.service.GenericService;

import com.github.masterdxy.gateway.common.Constant;
import com.github.masterdxy.gateway.config.DubboConfiguration;
import com.github.masterdxy.gateway.plugin.Plugin;
import com.github.masterdxy.gateway.plugin.PluginChain;
import com.github.masterdxy.gateway.protocol.v1.GatewayRequest;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DubboPlugin implements Plugin {

    @Autowired
    private DubboConfiguration dubboConfiguration;
    private static final Logger logger = LoggerFactory.getLogger(DubboPlugin.class);

    @Override
    public int order() {
        return 0;
    }

    @Override
    public boolean match(RoutingContext context) {
        //check context
        return true;
    }

    @Override
    public boolean execute(RoutingContext context, PluginChain chain) {
        //invoke rpc in worker thread pool;
        //add result into context;
        //invoke chain next;
        GatewayRequest request = context.get(Constant.GATEWAY_REQUEST_KEY);
        if (request != null){
            GenericService service = dubboConfiguration.getDubboService(request.getNamespace(),request.getVersion());
            Object object = service.$invoke(request.getData().remove("method"),new String[]{request.getData().remove(
                    "reqClass")},
                    new Object[]{request.getData()});
            logger.info("DubboPlugin execute result : {}",object);
            context.put(Constant.PLUGIN_RESULT_KEY,object);
        }else {
            context.put(Constant.PLUGIN_RESULT_KEY,"GatewayRequest is null");
        }
        return chain.execute();
    }
}
