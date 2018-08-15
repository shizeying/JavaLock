package com.yangxin.spinlock;

import com.yangxin.MyLock;

import java.util.concurrent.atomic.AtomicReference;


/**
 * @author leon on 2018/8/13.
 * @version 1.0
 * @Description: 非公平的自弦锁
 */
public  class BaseSpinLock implements MyLock {

    private AtomicReference<Thread> sign = new AtomicReference<>();


    @Override
    public void lock() {
        Thread current = Thread.currentThread();
        while (!sign.compareAndSet(null, current)) {
        }
    }

    @Override
    public void unlock() {
        Thread current = Thread.currentThread();
        sign.compareAndSet(current, null);
    }

    @Override
    public void remove() {
    }

}
