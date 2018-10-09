package com.sunchangpeng.async.jdk;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sunchangpeng.async.Utils.*;
import static org.junit.Assert.*;

public class AsyncTest {
    //single CompletableFuture
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

    //two CompletableFuture
    @Test
    public void test10() {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            sleepEnough(200,TimeUnit.MILLISECONDS);
            return "Sunchp";
        }).thenApplyAsync(i -> i.toUpperCase());

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            sleepEnough(100,TimeUnit.MILLISECONDS);
            return "Sunchp";
        }).thenApplyAsync(i -> i.toLowerCase());

        CompletableFuture<String> future3 = future1.applyToEither(future2, i -> i);

        assertEquals("sunchp", future3.join());
    }

    @Test
    public void test11() {
        StringBuilder stringBuilder = new StringBuilder();

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            sleepEnough(200,TimeUnit.MILLISECONDS);
            return "Sunchp";
        }).thenApply(String::toUpperCase);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            sleepEnough(100,TimeUnit.MILLISECONDS);
            return "Sunchp";
        }).thenApply(String::toLowerCase);

        CompletableFuture future3 = future1.acceptEither(future2, i -> stringBuilder.append(i));

        assertNull(future3.join());
        assertTrue(stringBuilder.length() > 0);
    }

    @Test
    public void test12() {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            sleepEnough(200,TimeUnit.MILLISECONDS);
            return "Hello";
        }).thenApply(String::toUpperCase);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            sleepEnough(100,TimeUnit.MILLISECONDS);
            return "World";
        }).thenApply(String::toLowerCase);

        StringBuilder stringBuilder = new StringBuilder();
        CompletableFuture future3 = future1.runAfterBoth(future2, () -> stringBuilder.append("Done"));

        assertNull(future3.join());
        assertTrue(stringBuilder.length() > 0);
    }

    @Test
    public void test13() {
        CompletableFuture<String> future1 = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(200,TimeUnit.MILLISECONDS);
                    return "Hello";
                }).thenApply(String::toUpperCase);

        CompletableFuture<String> future2 = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(100,TimeUnit.MILLISECONDS);
                    return "World";
                })
                .thenApply(String::toLowerCase);

        StringBuilder stringBuilder = new StringBuilder();
        CompletableFuture future3 = future1.thenAcceptBoth(future2, (i, j) -> stringBuilder.append(i).append(" ").append(j));

        assertNull(future3.join());
        assertEquals("HELLO world", stringBuilder.toString());
    }


    @Test
    public void test14() {
        CompletableFuture<String> future1 = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(200,TimeUnit.MILLISECONDS);
                    return "Hello";
                }).thenApply(String::toUpperCase);

        CompletableFuture<String> future2 = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(100,TimeUnit.MILLISECONDS);
                    return "World";
                })
                .thenApply(String::toLowerCase);

        CompletableFuture future3 = future1.thenCombine(future2, (i, j) -> i + " " + j);
        assertEquals("HELLO world", future3.join());
    }

    @Test
    public void test15() {
        CompletableFuture<String> future1 = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(200,TimeUnit.MILLISECONDS);
                    return "Hello";
                }).thenApplyAsync(String::toUpperCase);

        CompletableFuture<String> future2 = CompletableFuture
                .supplyAsync(() -> {
                    sleepEnough(100,TimeUnit.MILLISECONDS);
                    return "World";
                })
                .thenApplyAsync(String::toLowerCase);

        CompletableFuture future3 = future1.thenCombine(future2, (i, j) -> i + " " + j);

        assertEquals("HELLO world", future3.join());
    }

    //many CompletableFuture
    @Test
    public void test16() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        List<CompletableFuture<String>> futures = messages.stream()
                .map(msg -> CompletableFuture.supplyAsync(() -> {
                    randomSleep();
                    return msg;
                }).thenApply(String::toUpperCase)).collect(Collectors.toList());

        CompletableFuture future = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[futures.size()])).whenComplete((i, e) -> {
            if (e == null) {
                assertTrue(isUpperCase((String) i));
                result.append(i);
            }
        });

        future.join();
        assertTrue(result.length() > 0);
    }

    @Test
    public void test17() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        List<CompletableFuture<String>> futures = messages.stream()
                .map(msg -> CompletableFuture.supplyAsync(() -> {
                    randomSleep();
                    return msg;
                }).thenApply(String::toUpperCase)).collect(Collectors.toList());

        CompletableFuture future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).whenComplete((i, e) -> {
            futures.forEach(cf -> assertTrue(isUpperCase(cf.getNow(null))));
            result.append("done");
        });

        future.join();
        assertTrue(result.length() > 0);
    }

    @Test
    public void test18() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        List<CompletableFuture<String>> futures = messages.stream()
                .map(msg -> CompletableFuture.supplyAsync(() -> {
                    randomSleep();
                    return msg;
                }).thenApplyAsync(String::toUpperCase)).collect(Collectors.toList());

        CompletableFuture future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).whenComplete((i, e) -> {
            futures.forEach(cf -> assertTrue(isUpperCase(cf.getNow(null))));
            result.append("done");
        });

        future.join();
        assertTrue(result.length() > 0);
    }


}
