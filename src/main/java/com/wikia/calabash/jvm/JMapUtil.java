package com.wikia.calabash.jvm;

import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wikia
 * @since 7/30/2020 3:52 PM
 */
public class JMapUtil {
    private static final Pattern ROW_PATTERN = Pattern.compile("^(\\d+):\\s+(\\d+)\\s+(\\d+)\\s+(.*)$");

    public static List<Row> read(String file) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(file));
            return lines.stream()
                    .map(JMapUtil::toRow)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Row toRow(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("num") || line.startsWith("-")) {
            System.err.println("skip line:" + line);
            return null;
        }
        Matcher matcher = ROW_PATTERN.matcher(line);
        if (!matcher.find() || matcher.groupCount() != 4) {
            System.err.println("skip line:" + line);
            return null;
        }
        Row row = new Row();
        row.setNum(Integer.parseInt(matcher.group(1)));
        row.setInstances(Long.parseLong(matcher.group(2)));
        row.setBytes(Long.parseLong(matcher.group(3)));
        row.setClassName(matcher.group(4));
        System.out.println("read line:" + line);
        return row;
    }

    @Data
    public static class Row {
        private int num;
        private long instances;
        private long bytes;
        private String className;
    }

}
