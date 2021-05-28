package com.wikia.calabash.cache.common;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Component
public class FastJsonSerializer implements Serializer {
    @Override
    public String serialize(Object o) {
        try {
            return JSON.toJSONString(o);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("redis jackson serialize fail:%s", o));
        }
    }

    @Override
    public Object deserialize(String s, Type type) {
        try {
            if (Strings.isNullOrEmpty(s)) {
                return null;
            }
            return JSON.parseObject(s, type);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("redis jackson deserialize fail:%s, %s", s, type));
        }
    }

    public static void main(String[] args) {
        List<Integer> a = new ArrayList<>();
        a.add(1);
        FastJsonSerializer fastJsonSerializer = new FastJsonSerializer();

        String serialize = fastJsonSerializer.serialize(a);
        System.out.println(serialize);

        Object deserialize = fastJsonSerializer.deserialize(serialize, List.class);
        System.out.println(deserialize);
    }
}
