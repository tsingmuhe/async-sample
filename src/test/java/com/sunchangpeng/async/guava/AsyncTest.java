package com.sunchangpeng.async.guava;

import com.google.common.util.concurrent.*;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sunchangpeng.async.Utils.randomSleep;
import static org.junit.Assert.*;

public class AsyncTest {
    @Test
    public void test1() {
        ExecutorService executorService = Executors.newFixedThreadPool(1, r -> new Thread(r, "custom-executor-1"));
        SettableFuture<String> future = SettableFuture.create();
        future.addListener(() -> assertTrue(Thread.currentThread().getName().startsWith("custom-executor-")), executorService);
        assertFalse(future.isDone());
        future.set("sunchp");
        assertTrue(future.isDone());
    }

    @Test
    public void test2() {
        ExecutorService executorService = Executors.newFixedThreadPool(1, r -> new Thread(r, "custom-executor-1"));
        SettableFuture<String> future = SettableFuture.create();

        Futures.addCallback(future, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                assertTrue(Thread.currentThread().getName().startsWith("custom-executor-"));
                assertEquals("sunchp", result);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, executorService);

        assertFalse(future.isDone());
        future.set("sunchp");
        assertTrue(future.isDone());
    }

    @Test
    public void test3() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(1, r -> new Thread(r, "custom-executor-1"));
        ListenableFutureTask<String> futureTask = ListenableFutureTask.create(() -> {
            randomSleep();
            return "SUNCHP";
        });
        futureTask.addListener(() -> assertTrue(Thread.currentThread().getName().startsWith("custom-executor-")), executorService);
        new Thread(futureTask).start();
        assertEquals("SUNCHP", futureTask.get());
    }

    @Test
    public void test4() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(1, r -> new Thread(r, "custom-executor-1"));
        ListenableFutureTask futureTask = ListenableFutureTask.create(() -> {
            randomSleep();
            return "SUNCHP";
        });

        Futures.addCallback(futureTask, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                assertTrue(Thread.currentThread().getName().startsWith("custom-executor-"));
                assertEquals("SUNCHP", result);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, executorService);

        new Thread(futureTask).start();

        futureTask.get();
    }

    @Test
    public void test5() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1, r -> new Thread(r, "custom-executor-1"));

        ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
        ListenableFuture<String> future = listeningExecutor.submit(() -> {
            randomSleep();
            return "sunchp";
        });

        future.addListener(() -> assertTrue(Thread.currentThread().getName().startsWith("custom-executor-")), executorService);

        assertEquals("sunchp", future.get());
    }

    @Test
    public void test6() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1, r -> new Thread(r, "custom-executor-1"));

        ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
        ListenableFuture<String> future = listeningExecutor.submit(() -> {
            randomSleep();
            return "sunchp";
        });

        Futures.addCallback(future, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                assertTrue(Thread.currentThread().getName().startsWith("custom-executor-"));
                assertEquals("sunchp", result);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, executorService);

        future.get();
    }

    @Test
    public void test7() throws ExecutionException, InterruptedException {

        ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1, r -> new Thread(r, "custom-executor-1")));
        ListenableFuture<String> future = listeningExecutor.submit(() -> {
            randomSleep();
            return "sunchp";
        });

        Futures.addCallback(future, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                assertTrue(Thread.currentThread().getName().startsWith("custom-executor-"));
                assertEquals("sunchp", result);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });

        future.get();
    }
}
