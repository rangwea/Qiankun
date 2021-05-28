package com.wikia.calabash.auth;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wikia
 * @since 4/20/2021 11:11 AM
 */
@Repository
public class AuthClientSinkDao {
    @Resource
    private JdbcTemplate configJdbcTemplate;

    public List<AuthClientSink> getAll() {
        try {
            String sql = "select * from t_auth_client_sink";
            BeanPropertyRowMapper<AuthClientSink> rowMapper = new BeanPropertyRowMapper<>(AuthClientSink.class);
            return configJdbcTemplate.query(sql, rowMapper);
        } catch (EmptyResultDataAccessException empty) {
            return null;
        }
    }
}
