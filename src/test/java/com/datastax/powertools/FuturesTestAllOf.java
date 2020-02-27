package com.datastax.powertools;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FuturesTestAllOf {

    public static Executor test = Executors.newSingleThreadExecutor();

        public static void main(String[] args) {

        CompletableFuture<String> slowOne = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(0);
            return "puppies";
        });

        slowOne.thenApply(x -> {
            CompletableFuture<String> future1
                    = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(1);
                return "Hello";
            });
            CompletableFuture<String> future2
                    = CompletableFuture.supplyAsync(() -> {
                System.out.println(2);
                return "Beautiful";
            });
            CompletableFuture<String> future3
                    = CompletableFuture.supplyAsync(() -> {
                System.out.println(3);
                return "World";
            });

            CompletableFuture<Void> combinedFuture
                    = CompletableFuture.allOf(future1, future2, future3);

            combinedFuture.thenApply((y) -> {
                return y;
            });
            return x;
        });

        while(true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
