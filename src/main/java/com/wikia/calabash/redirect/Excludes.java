package com.wikia.calabash.redirect;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author wikia
 * @since 2020/1/6 19:53
 */
public class Excludes {
    public static final Set<String> paths = Sets.newHashSet(
            "/error"
    );
}
