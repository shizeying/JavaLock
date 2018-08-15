package com.yangxin.spinlock;

import com.yangxin.MyLock;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author leon on 2018/8/13.
 * @version 1.0
 * @Description: Ticket锁主要解决的是访问顺序的问题
 */
public class BaseTicketLock implements MyLock {

    private AtomicInteger serviceNum = new AtomicInteger();
    private AtomicInteger ticketNum = new AtomicInteger();
    private static final ThreadLocal<Integer> LOCAL = new ThreadLocal<>();

    @Override
    public void lock() {
        int myticket = ticketNum.getAndIncrement();
        LOCAL.set(myticket);
        while (myticket != serviceNum.get()){
        }
    }

    @Override
    public void unlock() {
        int myticket = LOCAL.get();
        serviceNum.compareAndSet(myticket, myticket + 1);
    }

    @Override
    public void remove() {
        LOCAL.remove();
    }


}
