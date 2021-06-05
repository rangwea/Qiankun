package com.wikia.calabash.jdbc;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wufj(wikia_wu @ kingdee.com)
 * @since 2/3/2021 4:43 PM
 */
public class JdbcCrudGenerator {
    private static final String TABLE_PREFIX = "t_";
    private static final Joiner.MapJoiner UPDATE_MAP_JOINER = Joiner.on(", ").withKeyValueSeparator("=:");

    public static void main(String[] args) throws Exception {
        try {
            String templatePath = Paths.get(JdbcCrudGenerator.class.getResource("/").toURI()).toString();

            Configuration configuration = new Configuration();
            configuration.setDirectoryForTemplateLoading(new File(templatePath));
            Template template = configuration.getTemplate("jdbc.ftl");

            Map<Class<?>, String> classes = new HashMap<>();
            //classes.put(DataInstance.class, "t_data_instance");

            classes.forEach((clz, table) -> {
                Definition definition = createDefinition(clz, table);
                String javaFile = "./" + definition.getDaoName() + ".java";
                generate(template, javaFile, definition);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generate(Template template, String outFile, Definition definition) {
        Writer out = null;
        try {
            System.out.println("generate start:class=" + definition.getClassName());

            File docFile = new File(outFile);
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile)));
            template.process(definition, out);

            System.out.println("generate end:class=" + definition.getClassName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Definition createDefinition(Class<?> clz, String tableName) {
        Definition definition = new Definition();
        definition.setClassName(clz.getName());
        definition.setClassSimpleName(clz.getSimpleName());
        Map<String, String> fields = getFields(clz);
        definition.setFields(fields);

        definition.setTableName(tableName);
        if (tableName == null || tableName.isEmpty()) {
            definition.setTableName(TABLE_PREFIX + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clz.getSimpleName()));
        }

        definition.setDaoName(clz.getSimpleName() + "Dao");

        definition.setCommaColumns(String.join(", ", fields.keySet()));
        definition.setInsertFields(":" + String.join(", :", fields.values()));
        definition.setUpdateClause(UPDATE_MAP_JOINER.join(fields));
        return definition;
    }

    /**
     * table_column -> model_field
     */
    public static Map<String, String> getFields(Class<?> clz) {
        return Stream.of(clz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toMap(s -> "`" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, s) + "`", s ->  s));
    }

    @Data
    public static class Definition {
        private String className;
        private String classSimpleName;
        private String tableName;
        private String daoName;
        private Map<String, String> fields;
        private String commaColumns;
        private String insertFields;
        private String updateClause;
    }

}
