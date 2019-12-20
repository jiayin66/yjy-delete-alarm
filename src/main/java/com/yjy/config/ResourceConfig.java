package com.yjy.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
@Configuration
//@EnableTransactionManagement
@MapperScan(basePackages = {"com.yjy.mapper.resource"}, sqlSessionFactoryRef = "sqlSessionFactory4")
public class ResourceConfig {
	@Autowired
    @Qualifier("resource")
    private DruidDataSource resourceDataSource;


    @Bean
    public SqlSessionFactory sqlSessionFactory4() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(resourceDataSource);
        return factoryBean.getObject();

    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate4() throws Exception {
        SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory4());
        return template;
    }
 
}
