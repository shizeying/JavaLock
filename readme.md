## 1. 自旋锁

---
自旋锁是采用让当前线程不停地的在循环体内执行实现的，当循环的条件被其他线程改变时 才能进入临界区。如下

```
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
```
使用了CAS原子操作，lock函数将owner设置为当前线程，并且预测原来的值为空。unlock函数将owner设置为null，并且预测值为当前线程。

当有第二个线程调用lock操作时由于owner值不为空，导致循环一直被执行，直至第一个线程调用unlock函数将owner设置为null，第二个线程才能进入临界区。

由于自旋锁只是将当前线程不停地执行循环体，不进行线程状态的改变，所以响应速度更快。但当线程数不停增加时，性能下降明显，因为每个线程都需要执行，占用CPU时间。如果线程竞争不激烈，并且保持锁的时间段。适合使用自旋锁。

注：该例子为非公平锁，获得锁的先后顺序，不会按照进入lock的先后顺序进行。

### 1.1. TicketLock

---
Ticket锁主要解决的是访问顺序的问题，主要的问题是在多核cpu上,每次都要查询一个serviceNum 服务号，影响性能（必须要到主内存读取，并阻止其他cpu修改）。
### 1.2. CLHLock、MCSLock

---
CLHLock 和MCSLock 则是两种类型相似的公平锁，采用链表的形式进行排序

CLH锁是一种基于链表的可扩展、高性能、公平的自旋锁，申请线程只在本地变量上自旋，它不断轮询前驱的状态，如果发现前驱释放了锁就结束自旋。
          
MCS Spinlock 是一种基于链表的可扩展、高性能、公平的自旋锁，申请线程只在本地变量上自旋，直接前驱负责通知其结束自旋，从而极大地减少了不必要的处理器缓存同步的次数，降低了总线和内存的开销。

- 从代码实现来看，CLH比MCS要简单得多。
- 从自旋的条件来看，CLH是在前驱节点的属性上自旋，而MCS是在本地属性变量上自旋。
- 从链表队列来看，CLH的队列是隐式的，CLHNode并不实际持有下一个节点；MCS的队列是物理存在的。
- CLH锁释放时只需要改变自己的属性，MCS锁释放则需要改变后继节点的属性。
![image](https://github.com/xuanyu66/JavaLock/blob/master/CLH_MCS.png)

## 2. 阻塞锁

---
阻塞锁，与自旋锁不同，改变了线程的运行状态。
在JAVA环境中，线程Thread有如下几个状态：

1. 新建状态

2. 就绪状态

3. 运行状态

4. 阻塞状态

5. 死亡状态

阻塞锁，可以说是让线程进入阻塞状态进行等待，当获得相应的信号（唤醒，时间） 时，才可以进入线程的准备就绪状态，准备就绪状态的所有线程，通过竞争，进入运行状态。
JAVA中，能够进入\退出、阻塞状态或包含阻塞锁的方法有 ，synchronized 关键字（其中的重量锁），ReentrantLock，Object.wait()\notify(),LockSupport.park()/unpart()

阻塞锁的优势在于，阻塞的线程不会占用cpu时间， 不会导致 CPu占用率过高，但进入时间以及恢复时间都要比自旋锁略慢。

在竞争激烈的情况下 阻塞锁的性能要明显高于 自旋锁。

理想的情况则是; 在线程竞争不激烈的情况下，使用自旋锁，竞争激烈的情况下使用，阻塞锁。
