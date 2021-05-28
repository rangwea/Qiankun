/*
package com.wikia.calabash.util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

public class DatabaseAccessTemplate<T> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private String insertSql;
    private String deleteSql;
    private String updateSql;
    private String querySql;

    public DatabaseAccessTemplate() {
        Class<?> modelClz = GenericsUtils.getSuperClassGenericType(this.getClass());
        Field[] declaredFields = modelClz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            String fieldUnderscore = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, declaredField.getName());

        }
    }

    public void insert(String sql, Object bean) {
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(bean);
        try {
            namedParameterJdbcTemplate.update(sql, sqlParameterSource);
        } catch (DuplicateKeyException ee) {
        } catch (Exception e) {
            logger.error("insert error!", e);
        }
    }

    public <T> void batchInsert(String sql, List<T> list) {
        try {
            SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(list.toArray());
            namedParameterJdbcTemplate.batchUpdate(sql, batch);
        } catch (DuplicateKeyException ee) {
        } catch (Exception e) {
            logger.error("batchInsert error!", e);
        }
    }

    public int update(String sql, Object bean) {
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(bean);
        int row;
        try {
            row = namedParameterJdbcTemplate.update(sql, sqlParameterSource);
        } catch (Exception e) {
            logger.error("update error!", e);
            row = -1;
        }
        return row;
    }

    public List<T> queryAll(String sql) {
        return namedParameterJdbcTemplate.query(sql, new BeanPropertyRowMapper<>());
    }


    public int count(String table, Object batchTime) {
        String sql = String.format("select count(1) from %s where batch_time = ? ", table);
        return namedParameterJdbcTemplate.getJdbcTemplate().queryForObject(sql, new Object[]{batchTime}, Integer.class);
    }

    public <T> List<T> queryAll(String opr, Class<T> clazz) {
        ColMapperHolder holder = mapperHolderMap.get(opr);
        return namedParameterJdbcTemplate.query(holder.getQueryStatement().getT0(), new BeanPropertyRowMapper<T>(clazz));
    }

    public <T> List<T> queryAllByTime(String opr, LocalDateTime batchTime) {
        ColMapperHolder holder = mapperHolderMap.get(opr);
        String template = holder.getQueryStatement().getT0();
        String sql = template + " where batch_time = ? ";
        //    logger.info("queryAllByTime sql:{} ", sql);
        return namedParameterJdbcTemplate.getJdbcTemplate().query(sql, new BeanPropertyRowMapper<>(holder.getQueryStatement().getT1()), batchTime);
    }

    */
/**
     * 暂时不考虑线程安全问题
     * @param opr
     * @param batchTime
     * @param <T>
     * @return
     *//*

    public <T> List<T> queryAllByTime2(String opr, LocalDateTime batchTime) {
        ColMapperHolder holder = mapperHolderMap.get(opr);
        String template = holder.getQueryStatement().getT0();
        String sql = template + " where batch_time = ? ";
        JdbcTemplate jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
        jdbcTemplate.setFetchSize(10000);
        List<T> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(holder.getQueryStatement().getT1()), batchTime);
        jdbcTemplate.setFetchSize(-1);
        return list;
    }

    */
/**
     * @param
     * @param batchTime
     * @param relOp
     * @param <T>
     * @return
     *//*

    public <T> List<T> queryAllByTime(String key, String table, LocalDateTime batchTime, RelOp relOp, Class<T> clazz) {
        ColMapperHolder holder = mapperHolderMap.get(key);
        String template = holder.getQueryStatement().getT0();
        template = template.replace(key, table);
        String op;
        switch (relOp) {
            case GT:
                op = ">";
                break;
            case LT:
                op = "<";
                break;
            default:
                op = "=";
        }
        String sql = new StringBuilder(template).append(" where batch_time ").append(op).append(" ? ").toString();
        logger.info("queryAllByTime sql:{} ", sql);
        return namedParameterJdbcTemplate.getJdbcTemplate().query(sql, new BeanPropertyRowMapper<>(clazz), batchTime);
    }
}
*/
