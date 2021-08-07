package com.wikia.calabash.ddl;

import com.google.common.base.CaseFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wikia
 * @since 5/18/2021 11:17 AM
 */
public class DDL2Model {

    public static void main(String[] args) throws IOException {
        String readSqlFile = "D:/ddl/a.txt";
        String writeJavaFilePath = "d:/ddl";
        String basePackage = "package com.c2.extractor.entity;\n\n";
        String importPackageName = "import com.baomidou.mybatisplus.annotation.TableField;\n" +
                "import com.baomidou.mybatisplus.annotation.TableName;\n" +
                "import lombok.Getter;\n" +
                "import lombok.Setter;\n\n";

        List<String> lines = Files.lines(Paths.get(readSqlFile))
                .collect(Collectors.toList());

        String classContent = "";
        String tableName = "";
        String className = "";
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("CREATE")) {
                String[] split = line.split(" ");
                tableName = split[2].substring(1, split[2].length() -1);
                // 去掉
                className = tableName.substring(0);
                className = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, className);
                String classLine = String.format("@Setter\n@Getter\n@TableName(\"%s\") \npublic class %s {", tableName, className);
                classContent = basePackage + importPackageName + classLine + "\n";
                continue;
            }

            if (isColumnLine(line)) {
                String[] c = line.split(" ");
                String columnName = c[0].substring(1, c[0].length()-1);
                String fieldName = columnName.substring(0);
                fieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, fieldName);
                String type = typeConvert(c[1]);
                String fieldLine = String.format("    @TableField(\"%s\") \n    private %s %s; \n", columnName, type, fieldName);
                classContent = classContent + fieldLine + "\n";
                continue;
            }

            if (line.startsWith(")")) {
                classContent = classContent + "}";
                System.out.println(classContent);
                try {
                    Files.write(Paths.get(writeJavaFilePath, className + ".java"), classContent.getBytes(), StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }
        };
    }

    private static String typeConvert(String mysqlType) {
        if (mysqlType.contains("bigint")) {
            return "Long";
        } else if (mysqlType.contains("varchar")) {
            return "String";
        } else if (mysqlType.contains("datetime")) {
            return "Date";
        } else if (mysqlType.contains("int")) {
            return "Integer";
        } else if (mysqlType.contains("text")) {
            return "String";
        } else if (mysqlType.contains("timestamp")) {
            return "Long";
        } else if (mysqlType.contains("double")) {
            return "Double";
        }
        return null;
    }

    private static boolean isColumnLine(String line) {
        return !(line.isEmpty() || line.startsWith("PRIMARY") || line.startsWith("KEY") || line.contains("UNIQUE") || line.startsWith(")"));
    }
}
