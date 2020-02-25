package com.datastax.powertools;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FuturesTest {
    public static Executor test = Executors.newSingleThreadExecutor();

    public static void main(String[] args) {
        ArrayList<CompletableFuture<Integer>> next = runTasks();

        while(next.size() < 4){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        AtomicInteger doneCount = new AtomicInteger();
        CompletableFuture<Integer> halp;
        for (CompletableFuture<Integer> integerCompletableFuture : next) {
            halp = integerCompletableFuture.thenApplyAsync((x) -> {
                System.out.println("output: " + x);
                //next.remove(next.indexOf(integerCompletableFuture));
                doneCount.getAndIncrement();
                return 0;
            });
        }

        while(doneCount.get() < 4){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static ArrayList<CompletableFuture<Integer>> runTasks() {
        ArrayList<CompletableFuture<Integer>> outputStreamFutures = new ArrayList();

        CompletableFuture.supplyAsync(() -> {
            CompletableFuture<Integer> one = CompletableFuture
                    .supplyAsync(() -> {
                        System.out.println(1);
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return 1;
                    });

            CompletableFuture<Integer> two = one.thenApplyAsync((x) -> {
                System.out.println(2);
                return 2;
            }, test);


            CompletableFuture<Integer> three = CompletableFuture
                    .supplyAsync(() -> {
                        System.out.println(3);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return 3;
                    });

            CompletableFuture<Integer> four = three.thenApplyAsync((x) -> {
                System.out.println(4);
                return 4;
            }, test);

            outputStreamFutures.add(one);
            outputStreamFutures.add(two);
            outputStreamFutures.add(three);
            outputStreamFutures.add(four);
            return 0;
        });

        return outputStreamFutures;
    }

}
