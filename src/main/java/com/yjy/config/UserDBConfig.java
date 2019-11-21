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
@MapperScan(basePackages = {"com.yjy.mapperuser"}, sqlSessionFactoryRef = "sqlSessionFactory3")
public class UserDBConfig {

    @Autowired
    @Qualifier("user")
    private DruidDataSource userDataSource;


    @Bean
    public SqlSessionFactory sqlSessionFactory3() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(userDataSource);
        return factoryBean.getObject();

    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate3() throws Exception {
        SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory3());
        return template;
    }
 
 

}
