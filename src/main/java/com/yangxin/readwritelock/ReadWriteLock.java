package com.yangxin.readwritelock;

import java.util.HashMap;
import java.util.Map;

/**
 * @author leon on 2018/8/15.
 * @version 1.0
 * @Description:
 */
public class ReadWriteLock {

    private Map<Thread, Integer> readingThreads = new HashMap<>();

    private int writerAccesses = 0;
    private int writeRequests = 0;
    private Thread writingThread = null;

    public synchronized void lockRead() throws InterruptedException{
        Thread callingThread = Thread.currentThread();
        while (!canGrantReadAccess(callingThread)){
            wait();
        }

        readingThreads.put(callingThread, (getReadAccessCount(callingThread) + 1));
    }

    private boolean canGrantReadAccess(Thread callingThread) {
        if (isWriter(callingThread)){
            return true;
        }
        if (hasWriter()) {
            return false;
        }
        if (isReader(callingThread)){
            return true;
        }
        if (hasWriteRequest()){
            return false;
        }
        return true;
    }

    private synchronized void unlockRead() {
        Thread callingThread = Thread.currentThread();
        if (!isReader(callingThread)) {
            throw new IllegalMonitorStateException("calling Thread does not hold a readLock");
        }
        int accessCount = getReadAccessCount(callingThread);
        if (accessCount == 1){
            readingThreads.remove(callingThread);
        } else {
            readingThreads.put(callingThread, accessCount - 1);
        }
        notifyAll();
    }

    private int getReadAccessCount(Thread callingThread) {
        Integer accessCount = readingThreads.get(callingThread);
        if (accessCount == null) {
            return 0;
        }
        return accessCount;
    }

    private synchronized void lockWrite() throws InterruptedException{
        writeRequests++;
        Thread callingThread = Thread.currentThread();
        while (!canGrantWriteAccess(callingThread)){
            wait();
        }
        writeRequests--;
        writingThread = callingThread;
        writerAccesses++;
    }

    private synchronized void unlockWrite() {
        if (!isWriter(Thread.currentThread())){
            throw new IllegalMonitorStateException("calling Thread does not hold a writeLock");
        }
        writerAccesses--;
        if (writerAccesses == 0){
            writingThread = null;
        }
        notifyAll();
    }

    private boolean canGrantWriteAccess(Thread callingThread) {
        if (isOnlyReader(callingThread)) {
            return true;
        }
        if (hasReader()) {
            return false;
        }
        if (!hasWriter()){
            return true;
        }
        if (isWriter(callingThread)){
            return true;
        }
        return false;
    }

    private boolean isWriter(Thread callingThread) {
        return callingThread == writingThread;
    }

    private boolean hasWriter() {
        return writingThread != null;
    }

    private boolean hasWriteRequest() {
        return writeRequests > 0;
    }

    private boolean isOnlyReader(Thread callingThread) {
        return readingThreads.size() == 1 && readingThreads.get(callingThread) != null;
    }

    private boolean isReader(Thread callingThread) {
        return readingThreads.get(callingThread) != null;
    }

    private boolean hasReader() {
        return readingThreads.size() > 0;
    }


}
