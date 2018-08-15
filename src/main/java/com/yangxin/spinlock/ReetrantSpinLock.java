package com.yangxin.spinlock;

import com.yangxin.MyLock;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author leon on 2018/8/15.
 * @version 1.0
 * @Description:
 */
public class ReetrantSpinLock implements MyLock {
    private AtomicReference<Thread> owner = new AtomicReference<>();
    private volatile int count = 0;

    @Override
    public void lock() {
        Thread current = Thread.currentThread();
        if (current == owner.get()) {
            count++;
            return;
        }
        while (!owner.compareAndSet(null , current)){
        }
    }

    @Override
    public void unlock() {
        Thread current = Thread.currentThread();
        if (current == owner.get()) {
            if (count != 0){
                count --;
            } else {
                owner.compareAndSet(current, null);
            }
        }
    }

    @Override
    public void remove() {

    }
}
