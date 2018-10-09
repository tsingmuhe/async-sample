package com.sunchangpeng.async;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static boolean isUpperCase(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLowerCase(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void sleepEnough(long time, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(time);
        } catch (InterruptedException e) {
            // ...
        }
    }

    public static void randomSleep() {
        try {
            Thread.sleep(random.nextInt(100));
        } catch (InterruptedException e) {
            // ...
        }
    }

    static Random random = new Random();

    public static void sleepEnough() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // ...
        }
    }
}
