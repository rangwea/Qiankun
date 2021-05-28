package com.wikia.calabash.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {"xxx"}
        , sqlSessionFactoryRef = "xxxSqlSessionFactory"
        , sqlSessionTemplateRef = "xxxSqlSessionTemplate")
public class DataSourceXXXConfig {

    @Bean(name = "xxxDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.xxx")
    public DataSource xxxDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("xxxSqlSessionFactory")
    public SqlSessionFactory xxxSqlSessionFactory(@Qualifier("xxxDataSource") DataSource xxxDataSource)
            throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(xxxDataSource);

        //手动设置mybatis的配置
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session
                .Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        sessionFactoryBean.setConfiguration(configuration);
        return sessionFactoryBean.getObject();
    }

    @Bean("xxxTransactionManager")
    public DataSourceTransactionManager xxxTransactionManager(@Qualifier("xxxDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("xxxSqlSessionTemplate")
    public SqlSessionTemplate xxxSqlSessionTemplate(
            @Qualifier("xxxSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "xxxJdbcTemplate")
    public JdbcTemplate xxxJdbcTemplate(
            @Qualifier("xxxDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
