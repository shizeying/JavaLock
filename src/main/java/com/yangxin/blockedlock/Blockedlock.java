package com.yangxin.blockedlock;

import com.yangxin.MyLock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;

/**
 * @author leon on 2018/8/14.
 * @version 1.0
 * @Description: 修改CLHLock为阻塞锁
 */
public class Blockedlock implements MyLock {
    private class BlockedNode {
        private volatile Thread isLocked;
    }

    private volatile BlockedNode tail;
    private static final ThreadLocal<BlockedNode> LOCAL = new ThreadLocal<>();
    public static final AtomicReferenceFieldUpdater<Blockedlock,BlockedNode> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(Blockedlock.class,
                    BlockedNode.class, "tail");

    @Override
    public void lock() {
        BlockedNode node = new BlockedNode();
        System.out.println(Thread.currentThread());
        LOCAL.set(node);
        BlockedNode preNode = UPDATER.getAndSet(this, node);
        if (preNode != null) {
            preNode.isLocked = Thread.currentThread();
            LockSupport.park(this);
        }
    }

    @Override
    public void unlock() {
        BlockedNode node = LOCAL.get();
        if (!UPDATER.compareAndSet(this, node, null)){
            LockSupport.unpark(node.isLocked);
        }
    }

    @Override
    public void remove() {
        LOCAL.remove();
    }
}
