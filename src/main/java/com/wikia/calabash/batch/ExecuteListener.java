package com.wikia.calabash.batch;

import java.util.List;

public interface ExecuteListener<T> {
    void onSuccess(List<T> list);

    void onFail(Throwable throwable, List<T> list);
}
