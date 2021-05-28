package com.wikia.calabash.random;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.randomizers.misc.ConstantRandomizer;

/**
 * @author wikia
 * @since 11/25/2020 10:51 AM
 */
public class Randoms {
    private static final EasyRandom EASY_RANDOM;

    static {
        ConstantRandomizer<Byte> byteRandomizer = new ConstantRandomizer<>((byte) 1);
        ConstantRandomizer<Integer> intRandomizer = new ConstantRandomizer<>(1024);
        ConstantRandomizer<Long> longRandomizer = new ConstantRandomizer<>(1024L);
        EasyRandomParameters easyRandomParameters = new EasyRandomParameters()
                .randomize(byte.class, byteRandomizer)
                .randomize(Byte.class, byteRandomizer)
                .randomize(int.class, intRandomizer)
                .randomize(Integer.class, intRandomizer)
                .randomize(long.class, longRandomizer)
                .randomize(Long.class, longRandomizer)
                .randomize(String.class, new ConstantRandomizer<>("String"))
                .ignoreRandomizationErrors(true);
        EASY_RANDOM = new EasyRandom(easyRandomParameters);
    }

    public static Object getSimpleObject(Class<?> clz) {
        return EASY_RANDOM.nextObject(clz);
    }
}
