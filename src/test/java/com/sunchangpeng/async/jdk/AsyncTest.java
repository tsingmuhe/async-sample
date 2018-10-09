package com.sunchangpeng.async.jdk;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class AsyncTest {

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
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            randomSleep();
            return "sunchp";
        }).thenApplyAsync(i -> {
            assertTrue(Thread.currentThread().isDaemon());
            return i.toUpperCase();
        });

        assertNull(future.getNow(null));
        assertEquals("SUNCHP", future.join());
    }


    @Test
    public void test5() {
        ExecutorService executorService = Executors.newFixedThreadPool(1, r -> new Thread(r, "custom-executor-1"));

        CompletableFuture future = CompletableFuture.supplyAsync(() -> "sunchp").thenApplyAsync(i -> {
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
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            randomSleep();
            return "sunchp";
        }).thenAcceptAsync(i -> {
            assertEquals("sunchp", i);
            result.append(i);
        });

        future.join();
        assertTrue(result.length() > 0);
    }


    @Test
    public void test8() {
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            randomSleep();
            return "sunchp";
        }).thenApplyAsync(String::toUpperCase);

        CompletableFuture handleFuture = future.handle((i, e) -> (e != null) ? "message upon cancel" : "");
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
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            randomSleep();
            return "sunchp";
        }).thenApplyAsync(i -> i.toUpperCase());

        CompletableFuture exceptionFuture = future.exceptionally(i -> "canceled message");

        assertTrue(future.cancel(true));
        assertTrue(future.isCompletedExceptionally());
        assertEquals("canceled message", exceptionFuture.join());
    }

    @Test
    public void test10() {
        CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(200);
                    return "Sunchp";
                })
                .thenApplyAsync(i -> i.toUpperCase())
                .applyToEither(
                        CompletableFuture
                                .supplyAsync(() -> {
                                    sleepEnough(100);
                                    return "Sunchp";
                                })
                                .thenApplyAsync(i -> i.toLowerCase()),
                        i -> i);

        assertEquals("sunchp", future.join());
    }

    @Test
    public void test11() {
        StringBuilder stringBuilder = new StringBuilder();
        CompletableFuture future = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(200);
                    return "Sunchp";
                })
                .thenApply(String::toUpperCase)
                .acceptEither(CompletableFuture.supplyAsync(() -> {
                    sleepEnough(100);
                    return "Sunchp";
                }).thenApply(String::toLowerCase), i -> stringBuilder.append(i));

        future.join();

        assertTrue(stringBuilder.length() > 0);
    }

    @Test
    public void test12() {
        StringBuilder stringBuilder = new StringBuilder();
        CompletableFuture future = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(200);
                    return "Hello";
                }).thenApply(String::toUpperCase)
                .runAfterBoth(CompletableFuture
                        .supplyAsync(() -> {
                            sleepEnough(100);
                            return "World";
                        })
                        .thenApply(String::toLowerCase), () -> stringBuilder.append("Done"));
        future.join();

        assertTrue(stringBuilder.length() > 0);
    }

    @Test
    public void test13() {
        StringBuilder stringBuilder = new StringBuilder();
        CompletableFuture future = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(200);
                    return "Hello";
                }).thenApply(String::toUpperCase)
                .thenAcceptBoth(CompletableFuture
                        .supplyAsync(() -> {
                            sleepEnough(100);
                            return "World";
                        })
                        .thenApply(String::toLowerCase), (i, j) -> stringBuilder.append(i).append(" ").append(j));
        future.join();

        assertEquals("HELLO world", stringBuilder.toString());
    }


    @Test
    public void test14() {
        CompletableFuture future = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(200);
                    return "Hello";
                }).thenApply(String::toUpperCase)
                .thenCombine(CompletableFuture
                        .supplyAsync(() -> {
                            sleepEnough(100);
                            return "World";
                        })
                        .thenApply(String::toLowerCase), (i, j) -> i + " " + j);

        assertEquals("HELLO world", future.join());
    }

    @Test
    public void test15() {
        CompletableFuture future = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(200);
                    return "Hello";
                }).thenApplyAsync(String::toUpperCase)
                .thenCombine(CompletableFuture
                        .supplyAsync(() -> {
                            sleepEnough(100);
                            return "World";
                        })
                        .thenApplyAsync(String::toLowerCase), (i, j) -> i + " " + j);

        assertEquals("HELLO world", future.join());
    }

    private static void sleepEnough(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // ...
        }
    }

    private static void randomSleep() {
        try {
            Thread.sleep(random.nextInt(100));
        } catch (InterruptedException e) {
            // ...
        }
    }

    static Random random = new Random();

    private static void sleepEnough() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // ...
        }
    }
}
