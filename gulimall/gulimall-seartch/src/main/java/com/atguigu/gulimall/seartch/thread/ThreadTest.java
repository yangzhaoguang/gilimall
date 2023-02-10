package com.atguigu.gulimall.seartch.thread;

import org.springframework.cache.annotation.Cacheable;

import java.util.concurrent.*;

/**
 *
 * Author: YZG
 * Date: 2023/1/24 20:32
 * Description: 
 */
public class ThreadTest {
    /*
    * 创建线程的四种方式
    * */
    public static void main(String[] args) {
        // new Thread01().start();
        // new Thread(new Runnable01()).start();
        FutureTask<Integer> task = new FutureTask<>(new Callable01());
        // Integer integer = task.get(); 获取异步任务的结果，但是会阻塞线程

        // 使用 Executors 调用静态方法，创建不同的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        threadPool.execute(new Runnable01());
        // 使用完线程池一定要记着关闭！！！！
        threadPool.shutdown();
        // 自定义线程池
        // new ThreadPoolExecutor();


        System.out.println("main线程....");
    }
    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " 线程...");
        }
    }

    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " 线程...");
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println(Thread.currentThread().getName() + " 线程...");
            return 1;
        }
    }
}
