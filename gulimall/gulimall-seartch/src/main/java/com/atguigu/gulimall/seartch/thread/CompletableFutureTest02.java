package com.atguigu.gulimall.seartch.thread;

import java.util.concurrent.*;

/**
 *
 * Author: YZG
 * Date: 2023/1/24 21:53
 * Description: 
 */
public class CompletableFutureTest02 {
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
        CompletableFuture<String> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务一开始...");
            System.out.println("任务一结束...");
            return "hello";
        }, poolExecutor);


        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务二开始...");
            System.out.println("任务二结束...");
            return "world";
        }, poolExecutor);

        // 不会获得合并的俩个任务的计算结果，没有返回值
        future01.runAfterBothAsync(future02,()->{
            System.out.println("任务三开始...");
            System.out.println("任务三结束...");
        },poolExecutor);

        // 能获取合并的俩个任务计算结果，没有返回值
        future01.thenAcceptBothAsync(future02,(res1,res2) ->{
            System.out.println("任务四开始...");
            System.out.println("第一个任务的计算结果: " + res1 + " 第二个任务的计算结果: " +res2);
            System.out.println("任务四结束...");
        },poolExecutor);

        // 能获取俩个任务的计算结果，并且有返回值
        CompletableFuture<String> future = future01.thenCombineAsync(future02, (res1, res2) -> {
            System.out.println("任务四开始...");
            System.out.println("第一个任务的计算结果: " + res1 + " 第二个任务的计算结果: " + res2);
            System.out.println("任务四结束...");
            return res1 + res2;
        }, poolExecutor);

        String s = future.get();
        System.out.println(Thread.currentThread().getName() + " end... " + s); // main end... helloworld
    }
}
