package com.yjy.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;



@Configuration
public class DataSourceConfig {

    @Bean(name = "alarm")
    @Primary
    @ConfigurationProperties(prefix = "spring.alarm") // bootstrap.yml中对应属性的前缀
    public DruidDataSource dataSource1() {
        return DruidDataSourceBuilder.create().build();
    }


    @Bean(name = "ins")
    @ConfigurationProperties(prefix = "spring.ins") // bootstrap.yml中对应属性的前缀
    public DruidDataSource dataSource2() {
        return DruidDataSourceBuilder.create().build();
    }
    
    @Bean(name = "user")
    @ConfigurationProperties(prefix = "spring.user") // bootstrap.yml中对应属性的前缀
    public DruidDataSource dataSource3() {
    	return DruidDataSourceBuilder.create().build();
    }

}
