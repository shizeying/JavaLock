package com.yangxin.spinlock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author leon on 2018/8/13.
 * @version 1.0
 * @Description:
 */
public class SpinLockTest implements Runnable {

    static int sum;
    private MyLock lock;

    public SpinLockTest(MyLock lock) {
        this.lock = lock;
    }

    public static void main(String[] args)  throws Exception{
        //go("com.yangxin.spinlock.BaseSpinLock");
        //go("com.yangxin.spinlock.BaseTicketLock");
        //go("com.yangxin.spinlock.CLHLock");
        go("com.yangxin.spinlock.MCSLock");
    }

    public static void go(String name) throws Exception {
        sum = 0;
        System.out.println("******Class:"+name);
        ExecutorService cachedPool = Executors.newCachedThreadPool();
        MyLock lock = (MyLock)Class.forName(name).getConstructor().newInstance();
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            SpinLockTest test = new SpinLockTest(lock);
            cachedPool.execute(test);
        }

        cachedPool.shutdown();
        cachedPool.awaitTermination(1, TimeUnit.MINUTES);
        long time = System.nanoTime() - start;
        System.out.println(time/1e6 + "ms");
        System.out.println(sum);
        lock.remove();
    }

    @Override
    public void run() {
        this.lock.lock();
        sum++;
        this.lock.unlock();
    }
}