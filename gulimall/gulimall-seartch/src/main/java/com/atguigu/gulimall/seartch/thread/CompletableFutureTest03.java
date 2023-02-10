package com.atguigu.gulimall.seartch.thread;

import java.util.concurrent.*;

/**
 *
 * Author: YZG
 * Date: 2023/1/24 21:53
 * Description: 
 */
public class CompletableFutureTest03 {
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
            try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
            System.out.println("查询图片信息...");
            return "hello.jpg";
        }, poolExecutor);


        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询属性信息...");
            try {Thread.sleep(300);} catch (InterruptedException e) {e.printStackTrace();}
            return "attrs";
        }, poolExecutor);

        CompletableFuture<String> future03 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品信息...");
            try {Thread.sleep(400);} catch (InterruptedException e) {e.printStackTrace();}
            return "attrs";
        }, poolExecutor);

        // 所有任务都完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(future01, future03, future02);
        // 只要有一个任务完成即可，返回值就是第一个完成任务的返回值
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(future01, future03, future02);
        allOf.get();
        Object o = anyOf.get();
        System.out.println(Thread.currentThread().getName() + " end... " + o); //
    }
}
