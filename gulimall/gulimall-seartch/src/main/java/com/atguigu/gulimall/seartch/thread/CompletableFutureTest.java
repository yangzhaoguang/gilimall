package com.atguigu.gulimall.seartch.thread;

import com.zaxxer.hikari.util.UtilityElf;
import org.apache.ibatis.executor.Executor;

import java.util.concurrent.*;

/**
 *
 * Author: YZG
 * Date: 2023/1/24 21:53
 * Description: 
 */
public class CompletableFutureTest {
    // 自定义线程池
    private static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
            3,
            200,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());
    /*
     * 异步编排 CompletableFuture*
     *   使用 CompletableFuture 建议使用自定义的线程池。、
     *   如果不指定自定义的线程池默认使用 ForkJoinPool，这个线程池中的线程跟守护线程是的
     *   如果主线程结束，ForkJoinPool也会随之关闭。因此如果主线程结束太快，异步任务可能会获取不到结果
     * */

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        System.out.println(Thread.currentThread().getName() + " start...");
        // 没有返回值
        /*        CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + " start...");
            try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
            System.out.println(Thread.currentThread().getName() +" end...");
        },poolExecutor);*/

        // 有返回值
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    System.out.println(Thread.currentThread().getName() + " start...");
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // int i = 10 / 0;
                    System.out.println(Thread.currentThread().getName() + " end...");
                    return "hello";
                }, poolExecutor)
                .whenComplete((res, e) -> {
                    System.out.println("计算结果:" + res + ", 异常: " + e);
                }).exceptionally(e -> {
                    // exceptionally 可以自定义返回异常结果
                    return "错误原因: " + e.getMessage();
                });

        String s = future.get();
        System.out.println(Thread.currentThread().getName() + " end... " + s);


        // 线程池一定要关闭
        poolExecutor.shutdown();
    }
}
