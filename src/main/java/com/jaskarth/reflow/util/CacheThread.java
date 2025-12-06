package com.jaskarth.reflow.util;

import java.util.concurrent.atomic.AtomicInteger;

public class CacheThread {
    public static void start(String name, AtomicInteger hits, AtomicInteger misses) {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(name + " Hits: " + hits.get() + " Misses: " + misses.get() + " (" + ((double) hits.get() / (double) misses.get()) * 100 + ")");
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
