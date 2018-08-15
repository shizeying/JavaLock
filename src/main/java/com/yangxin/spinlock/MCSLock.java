package com.yangxin.spinlock;

import com.yangxin.MyLock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author leon on 2018/8/14.
 * @version 1.0
 * @Description: CLH是在前驱节点的属性上自旋，而MCS是在本地属性变量上自旋
 */
public class MCSLock implements MyLock {
    private class MCSNode {
        volatile MCSNode next;
        volatile boolean isLocked = true;
    }

    /***指向最后一个申请锁的MCSNode*/
    volatile MCSNode queue;
    private static final ThreadLocal<MCSNode> NODE = new ThreadLocal<>();
    private static final AtomicReferenceFieldUpdater<MCSLock,MCSNode> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(MCSLock.class, MCSNode.class,
                    "queue");
    @Override
    public void lock() {
        MCSNode currentNode = new MCSNode();
        NODE.set(currentNode);
        //step 1
        MCSNode preNode = UPDATER.getAndSet(this, currentNode);
        if (preNode != null) {
            //step2
            preNode.next = currentNode;
            while (currentNode.isLocked) {
            }
        }
    }

    @Override
    public void unlock() {
        MCSNode currentNode = NODE.get();
        // 检查是否有人排在自己后面
        if (currentNode.next == null) {
            if (UPDATER.compareAndSet(this, currentNode, null)){
                // compareAndSet返回true表示确实没有人排在自己后面
                return;
            }else {
                // 突然有人排在自己后面了，可能还不知道是谁，下面是等待后续者
                // 这里之所以要忙等是因为：step 1执行完后，step 2可能还没执行完
                while (currentNode.next == null){
                }
                // 释放锁
                currentNode.next.isLocked = false;
                currentNode.next = null;
            }
        }else {
            currentNode.next.isLocked = false;
            currentNode.next = null;
        }
    }

    @Override
    public void remove() {
        NODE.remove();
    }
}
