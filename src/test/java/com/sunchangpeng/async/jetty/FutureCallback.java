package com.sunchangpeng.async.jetty;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface FutureCallback<V> {
    /**
     * Invoked with the result of the {@code Future} computation when it is
     * successful.
     */
    void onSuccess(V result);

    /**
     * Invoked when a {@code Future} computation fails or is canceled.
     *
     * <p>If the future's {@link Future#get() get} method throws an {@link
     * ExecutionException}, then the cause is passed to this method. Any other
     * thrown object is passed unaltered.
     */
    void onFailure(Throwable t);
}
