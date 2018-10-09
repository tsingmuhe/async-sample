package com.sunchangpeng.async.jetty;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FuturePromise<C> implements Future<C>, Promise<C> {
    private final AtomicBoolean done = new AtomicBoolean(false);
    private final CountDownLatch latch = new CountDownLatch(1);
    private Throwable cause;
    private C result;

    private CallbackExecutorPair callbacks;

    public FuturePromise() {
    }

    @Override
    public void succeeded(C result) {
        if (this.done.compareAndSet(false, true)) {
            this.result = result;
            this.latch.countDown();
            executeCallbackSuccess(this.result);
        }

    }

    @Override
    public void failed(Throwable cause) {
        if (this.done.compareAndSet(false, true)) {
            this.cause = cause;
            this.latch.countDown();
            executeCallbackFailure(this.cause);
        }
    }

    @Override
    public void addCallback(FutureCallback<C> callback, Executor executor) {
        if (isDone()) {
            if (this.result != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(result);
                    }
                });
            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(cause);
                    }
                });
            }
            return;
        }

        synchronized (this) {
            this.callbacks = new CallbackExecutorPair(callback, executor, this.callbacks);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (this.done.compareAndSet(false, true)) {
            this.result = null;
            this.cause = new CancellationException();
            this.latch.countDown();
            executeCallbackFailure(this.cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        if (this.done.get()) {
            try {
                this.latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return this.cause instanceof CancellationException;
        }
        return false;
    }

    @Override
    public boolean isDone() {
        return this.done.get() && this.latch.getCount() == 0;
    }

    @Override
    public C get() throws InterruptedException, ExecutionException {
        this.latch.await();
        if (this.result != null)
            return this.result;
        if (this.cause instanceof CancellationException)
            throw (CancellationException) new CancellationException().initCause(this.cause);
        throw new ExecutionException(this.cause);
    }

    @Override
    public C get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!this.latch.await(timeout, unit))
            throw new TimeoutException();

        if (this.result != null)
            return this.result;
        if (this.cause instanceof TimeoutException)
            throw (TimeoutException) this.cause;
        if (this.cause instanceof CancellationException)
            throw (CancellationException) new CancellationException().initCause(this.cause);
        throw new ExecutionException(this.cause);
    }

    private static final class CallbackExecutorPair {
        final FutureCallback callback;
        final Executor executor;
        CallbackExecutorPair next;

        CallbackExecutorPair(FutureCallback callback, Executor executor, CallbackExecutorPair next) {
            this.callback = callback;
            this.executor = executor;
            this.next = next;
        }
    }

    private void executeCallbackSuccess(C result) {
        CallbackExecutorPair callbacks = this.callbacks;
        this.callbacks = null;
        while (callbacks != null) {
            CallbackExecutorPair tmp = callbacks;
            callbacks = callbacks.next;
            tmp.executor.execute(new Runnable() {
                @Override
                public void run() {
                    tmp.callback.onSuccess(result);
                }
            });
        }
    }

    private void executeCallbackFailure(Throwable t) {
        CallbackExecutorPair callbacks = this.callbacks;
        this.callbacks = null;
        while (callbacks != null) {
            CallbackExecutorPair tmp = callbacks;
            callbacks = callbacks.next;
            tmp.executor.execute(new Runnable() {
                @Override
                public void run() {
                    tmp.callback.onFailure(t);
                }
            });
        }
    }
}
