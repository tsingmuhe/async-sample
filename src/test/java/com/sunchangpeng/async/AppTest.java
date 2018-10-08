package com.sunchangpeng.async;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class AppTest {

    @Test
    public void test1() {
        CompletableFuture future = CompletableFuture.completedFuture("sunchp");
        assertTrue(future.isDone());
        assertEquals("sunchp", future.getNow(null));
    }

    @Test
    public void test2() {
        CompletableFuture future = CompletableFuture.runAsync(() -> {
            assertTrue(Thread.currentThread().isDaemon());
            randomSleep();
        });

        assertFalse(future.isDone());
        sleepEnough();
        assertTrue(future.isDone());
    }

    @Test
    public void test3() {
        CompletableFuture future = CompletableFuture.completedFuture("sunchp").thenApply(i -> {
            assertFalse(Thread.currentThread().isDaemon());
            return i.toUpperCase();
        });

        assertEquals("SUNCHP", future.getNow(null));
    }


    @Test
    public void test4() {
        CompletableFuture future = CompletableFuture.completedFuture("sunchp").thenApplyAsync(i -> {
            assertTrue(Thread.currentThread().isDaemon());
            return i.toUpperCase();
        });

        assertNull(future.getNow(null));
        assertEquals("SUNCHP", future.join());
    }


    @Test
    public void test5() {
        ExecutorService executorService = Executors.newFixedThreadPool(1, new ThreadFactory() {
            int count = 1;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "custom-executor-" + count++);
            }
        });

        CompletableFuture future = CompletableFuture.completedFuture("sunchp").thenApplyAsync(i -> {
            assertTrue(Thread.currentThread().getName().startsWith("custom-executor-"));
            assertFalse(Thread.currentThread().isDaemon());
            return i.toUpperCase();
        }, executorService);

        assertNull(future.getNow(null));
        assertEquals("SUNCHP", future.join());
    }

    @Test
    public void test6() {
        StringBuilder result = new StringBuilder();
        CompletableFuture.completedFuture("sunchp").thenAccept(i -> {
            assertEquals("sunchp", i);
            result.append(i);
        });

        assertTrue(result.length() > 0);
    }

    @Test
    public void test7() {
        StringBuilder result = new StringBuilder();
        CompletableFuture future = CompletableFuture.completedFuture("sunchp").thenAcceptAsync(i -> {
            assertEquals("sunchp", i);
            result.append(i);
        });

        future.join();
        assertTrue(result.length() > 0);
    }


    @Test
    public void test8() {
        CompletableFuture future = CompletableFuture.completedFuture("sunchp").thenApplyAsync(i -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return i.toUpperCase();
        });

        CompletableFuture handleFuture = future.handle((i, e) -> {
            return (e != null) ? "message upon cancel" : "";
        });

        future.completeExceptionally(new RuntimeException("completed exceptionally"));
        assertTrue(future.isCompletedExceptionally());

        try {
            future.join();
        } catch (Exception e) {
            //....
        }

        assertEquals("message upon cancel", handleFuture.join());
    }

    @Test
    public void test9() {
        CompletableFuture future = CompletableFuture.completedFuture("sunchp").thenApplyAsync(i -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return i.toUpperCase();
        });

        CompletableFuture exceptionFuture = future.exceptionally(i -> "canceled message");

        assertTrue(future.cancel(true));
        assertTrue(future.isCompletedExceptionally());
        assertEquals("canceled message", exceptionFuture.join());
    }

    private static void randomSleep() {
        try {
            Thread.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
            // ...
        }
    }

    static Random random = new Random();

    private static void sleepEnough() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // ...
        }
    }
}