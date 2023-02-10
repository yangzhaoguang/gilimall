package com.atguigu.gulimall.seartch.thread;

import java.util.concurrent.*;

/**
 *
 * Author: YZG
 * Date: 2023/1/24 21:53
 * Description: 
 */
public class CompletableFutureTest01 {
    /*
     * 异步编排 CompletableFuture*
     *   使用 CompletableFuture 建议使用自定义的线程池。、
     *   如果不指定自定义的线程池默认使用 ForkJoinPool，这个线程池中的线程跟守护线程是的
     *   如果主线程结束，ForkJoinPool也会随之关闭。因此如果主线程结束太快，异步任务可能会获取不到结果
     * */

    private static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
            3,
            200,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());

    // 异步线程串行化
    public static void main(String[] args) throws ExecutionException, InterruptedException {
       CompletableFuture.supplyAsync(() -> {
                    System.out.println("任务一开始...");
                    System.out.println("任务一结束...");
                    return "hello";
                }, poolExecutor)
               .thenApplyAsync(res ->{ // 依赖于上一步的计算结果，并且提供一个新的计算结果
                   System.out.println("任务二开始...");
                   System.out.println("任务二结束...");
                   return res + "world";
               },poolExecutor)
               .thenAccept(res ->{ // 依赖于上一步的计算结果，但是没有返回值

                   System.out.println("任务三开始...");
                   System.out.println("任务三结束..."+ res);
               })
                .thenRun(() ->{ // 不依赖上一步的计算结果，并且没有返回值
                    System.out.println("任务四开始...");
                    System.out.println("任务四结束...");
                })
                .whenCompleteAsync((res, e) -> { // 获取上一步的计算结果 null
                    System.out.println("计算结果:" + res + ", 异常: " + e);
                },poolExecutor);

        // String s = future.get();
        System.out.println(Thread.currentThread().getName() + " end... ");
    }
}
