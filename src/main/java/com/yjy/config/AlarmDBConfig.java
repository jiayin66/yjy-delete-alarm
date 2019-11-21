package com.yjy.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.alibaba.druid.pool.DruidDataSource;


@Configuration
//@EnableTransactionManagement
@MapperScan(basePackages = {"com.yjy.mapperalarm"}, sqlSessionFactoryRef = "sqlSessionFactory1")
public class AlarmDBConfig {

    @Autowired
    @Qualifier("alarm")
    private DruidDataSource alarmDataSource;


    @Bean
    public SqlSessionFactory sqlSessionFactory1() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(alarmDataSource);
        return factoryBean.getObject();

    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate1() throws Exception {
        SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory1());
        return template;
    }
 
 

}
