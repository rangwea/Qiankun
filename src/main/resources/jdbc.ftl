import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import javax.annotation.Resource;
import java.util.List;
import ${className};

@Slf4j
public class ${daoName} {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void insert(${classSimpleName} model) {
        final String sql ="insert into ${tableName}(${commaColumns}) value(${insertFields})";
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(model);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    public void insertBatch(List<${classSimpleName}> models) {
        final String sql ="insert into ${tableName}(${commaColumns}) value(${insertFields})";
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(models);
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }

    public void deleteById(Long id) {
        String sql = "delete from ${tableName} where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void update(${classSimpleName} model) {
        String sql = "update ${tableName} set ${updateClause} where id = :id";
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(model);
        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    public void updateBatch(List<${classSimpleName}> models) {
        String sql = "update ${tableName} set ${updateClause} where id = :id";
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(models);
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }

    public void insertOrUpdateOnDuplicate(List<${classSimpleName}> models) {
        String sql = "insert into ${tableName}(${commaColumns})" +
                " values(${insertFields})" +
                " on duplicate key update ${updateClause}";
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(models.toArray());
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }

    public List<${classSimpleName}> findAll() {
        String sql = "select * from ${tableName}";
        BeanPropertyRowMapper<${classSimpleName}> rowMapper = new BeanPropertyRowMapper<>(${classSimpleName}.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    public ${classSimpleName} findById(Long id) {
        String sql = "select ${commaColumns} from ${tableName} where id = :id";
        try {
            BeanPropertyRowMapper<${classSimpleName}> rowMapper = new BeanPropertyRowMapper<>(${classSimpleName}.class);
            ${classSimpleName} r = jdbcTemplate.queryForObject(sql
                    , new Object[]{id}
                    , rowMapper);
            return r;
        } catch (EmptyResultDataAccessException e) {
            log.warn("empty access:id={}", id);
            return null;
        }
    }
}