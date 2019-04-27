package org.citydb.writer;

import org.citydb.concurrent.WorkerPool;
import org.citygml4j.util.xml.SAXEventBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SequentialXMLWriter {
    private final ReentrantLock lock = new ReentrantLock();
    private final WorkerPool<SAXEventBuffer> writerPool;

    private Map<Long, CachedObject> cache = new HashMap<>();
    private Map<Long, Condition> locks = new HashMap<>();
    private long currentId = 0;
    private volatile boolean shouldRun = true;

    public SequentialXMLWriter(WorkerPool<SAXEventBuffer> writerPool) {
        this.writerPool = writerPool;
    }

    public void write(SAXEventBuffer buffer, long sequenceId) throws InterruptedException {
        if (sequenceId >= 0) {
            lock.lock();
            try {
                if (sequenceId == currentId) {
                    currentId++;
                    if (buffer != null && !buffer.isEmpty())
                        writerPool.addWork(buffer);

                    CachedObject cachedObject;
                    while ((cachedObject = cache.get(currentId)) != null) {
                        if (cachedObject.buffer != null && !cachedObject.buffer.isEmpty())
                            writerPool.addWork(cachedObject.buffer);

                        cache.remove(currentId);
                        locks.get(cachedObject.threadId).signal();
                        currentId++;
                    }
                } else {
                    long threadId = Thread.currentThread().getId();
                    cache.put(sequenceId, new CachedObject(buffer, threadId));

                    if (shouldRun)
                        locks.computeIfAbsent(threadId, v -> lock.newCondition()).await();
                }
            } finally {
                lock.unlock();
            }
        } else if (buffer != null && !buffer.isEmpty())
            writerPool.addWork(buffer);
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
                        .map(e -> e.getValue().buffer)
                        .filter(b -> b != null && !b.isEmpty())
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
        private final SAXEventBuffer buffer;
        private final long threadId;

        private CachedObject(SAXEventBuffer buffer, long threadId) {
            this.buffer = buffer;
            this.threadId = threadId;
        }
    }
}
