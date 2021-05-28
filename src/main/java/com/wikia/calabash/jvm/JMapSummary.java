package com.wikia.calabash.jvm;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wikia
 * @since 7/30/2020 4:20 PM
 */
public class JMapSummary {
    public static void main(String[] args) {
        List<String> files = Lists.newArrayList("", "");

        List<Map<String, JMapUtil.Row>> all = new ArrayList<>();
        for (String file : files) {
            List<JMapUtil.Row> jMaps = JMapUtil.read(file);
            // key: className -> value: jMapRow
            assert jMaps != null;
            Map<String, JMapUtil.Row> class2JMap = jMaps.stream()
                    .collect(Collectors.toMap(JMapUtil.Row::getClassName, e -> e));
            all.add(class2JMap);
        }

        Map<String, JMapUtil.Row> rowMap = all.get(0);

    }
}
