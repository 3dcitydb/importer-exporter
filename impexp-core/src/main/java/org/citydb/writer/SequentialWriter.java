package org.citydb.writer;

import org.citydb.concurrent.WorkerPool;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SequentialWriter<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final WorkerPool<T> writerPool;

    private Map<Long, CachedObject> cache = new HashMap<>();
    private Map<Long, Condition> locks = new HashMap<>();
    private long currentId = 0;
    private volatile boolean shouldRun = true;

    public SequentialWriter(WorkerPool<T> writerPool) {
        this.writerPool = writerPool;
    }

    public long reset() {
        currentId = 0;
        cache.clear();
        locks.clear();

        return currentId;
    }

    public long getCurrentSequenceId() {
        return currentId;
    }

    public void write(T object, long sequenceId) throws InterruptedException {
        if (sequenceId >= 0) {
            lock.lock();
            try {
                if (sequenceId == currentId) {
                    currentId++;
                    if (object != null)
                        writerPool.addWork(object);

                    CachedObject cachedObject;
                    while ((cachedObject = cache.get(currentId)) != null) {
                        if (cachedObject.object != null)
                            writerPool.addWork(cachedObject.object);

                        cache.remove(currentId);
                        locks.get(cachedObject.threadId).signal();
                        currentId++;
                    }
                } else {
                    long threadId = Thread.currentThread().getId();
                    cache.put(sequenceId, new CachedObject(object, threadId));

                    if (shouldRun)
                        locks.computeIfAbsent(threadId, v -> lock.newCondition()).await();
                }
            } finally {
                lock.unlock();
            }
        } else if (object != null)
            writerPool.addWork(object);
    }

    public void updateSequenceId(long sequenceId) throws InterruptedException {
        write(null, sequenceId);
    }

    public void writeCache() {
        lock.lock();
        try {
            if (!cache.isEmpty()) {
                cache.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(e -> e.getValue().object)
                        .filter(Objects::nonNull)
                        .forEach(writerPool::addWork);

                cache.clear();
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isInterrupted() {
        return !shouldRun;
    }

    public void interrupt() {
        shouldRun = false;

        lock.lock();
        try {
            locks.values().forEach(Condition::signal);
        } finally {
            lock.unlock();
        }
    }

    private final class CachedObject {
        private final T object;
        private final long threadId;

        private CachedObject(T object, long threadId) {
            this.object = object;
            this.threadId = threadId;
        }
    }
}
