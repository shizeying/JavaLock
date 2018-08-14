package com.yangxin.spinlock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author leon on 2018/8/14.
 * @version 1.0
 * @Description:
 */
public class CLHLock implements MyLock {

    class CLHNode {
        private volatile boolean isLocked = true;
    }

    private volatile CLHNode tail;
    private static final ThreadLocal<CLHNode> Local = new ThreadLocal<>();
    private static final AtomicReferenceFieldUpdater<CLHLock, CLHNode> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(CLHLock.class, CLHNode.class, "tail");

    @Override
    public void lock() {
        CLHNode node = new CLHNode();
        Local.set(node);
        CLHNode preNode = UPDATER.getAndSet(this, node);
        if (preNode != null) {
            //已有线程占用了锁，进入自旋
            while (preNode.isLocked){
            }
        }
    }

    @Override
    public void unlock() {
        CLHNode node = Local.get();
        //如果队列里只有当前线程，则释放对当前线程的引用（for GC）。
        if (!UPDATER.compareAndSet(this, node, null)) {
            // 还有后续线程,改变状态，让后续线程结束自旋
            node.isLocked = false;
        }
    }

    @Override
    public void remove() {
        Local.remove();
    }
}
