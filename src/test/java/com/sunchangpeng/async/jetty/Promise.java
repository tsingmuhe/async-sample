package com.sunchangpeng.async.jetty;

import java.util.concurrent.Executor;

public interface Promise<C> {
    default void succeeded(C result) {
    }

    default void failed(Throwable x) {
    }

    void addCallback(FutureCallback<C> callback, Executor executor);
}
