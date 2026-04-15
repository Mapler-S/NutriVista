package com.nutrivista.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.collection.GetCollectionStatisticsParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MilvusConfig {

    @Value("${milvus.host}")
    private String host;

    @Value("${milvus.port}")
    private int port;

    @Bean(destroyMethod = "close")
    public MilvusServiceClient milvusServiceClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .withConnectTimeout(10_000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .withKeepAliveTime(55_000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .withKeepAliveTimeout(20_000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();

        MilvusServiceClient client = new MilvusServiceClient(connectParam);
        log.info("Milvus client connected -> {}:{}", host, port);
        return client;
    }
}
