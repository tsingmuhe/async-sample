package com.sunchangpeng.async.jetty;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncTest {
    @Test
    public void test() {
        FuturePromise<String> promise = new FuturePromise<>();
        promise.addCallback(new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println(Thread.currentThread().getName() + ": " + result);
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, Executors.newSingleThreadExecutor());


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                promise.succeeded("sunchp");
            }
        }).start();

        try {
            promise.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() {
        FuturePromise<String> promise = new FuturePromise<>();
        promise.addCallback(new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println(Thread.currentThread().getName() + ": " + result);
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, Executors.newSingleThreadExecutor());


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                promise.succeeded("sunchp");
            }
        }).start();

        try {
            promise.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        promise.addCallback(new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println(Thread.currentThread().getName() + ": " + result);
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, Executors.newSingleThreadExecutor());
    }
}
