package concurrence;

import java.sql.Statement;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * class to manage all requests from a user
 */
public class StatementStampManager {
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Map<Long, StatementStamp> threadsMap = new HashMap<Long, StatementStamp>();
    private long threadId;

    long put(StatementStamp statementStamp) {
        readWriteLock.writeLock().lock();
        try {
            final long key = threadId++;
            threadsMap.put(key,statementStamp);
            return key;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * @param threadId returned by {@link UserDBTimeManagement#putStatement(Object, Statement)
     * @return if threadsMap is empty
     */
    public boolean remove(long threadId) {
        readWriteLock.writeLock().lock();
        try {
            threadsMap.remove(threadId);
            return threadsMap.isEmpty();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * @return map of threads from the user where the key is the Thread identifier in the {@link StatementStampManager}
     * context and the value is a {@link StatementStamp}
     */
    public Map<Long,StatementStamp> getSnapshot(){
        readWriteLock.readLock().lock();
        try {
            HashMap<Long,StatementStamp> hm = (HashMap<Long, StatementStamp>) threadsMap;
            return (Map<Long, StatementStamp>) hm.clone();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * @param threadId returned by {@link UserDBTimeManagement#putStatement(Object, Statement)}
     */
    public void cancel(long threadId){
        readWriteLock.readLock().lock();
        try {
            StatementStamp statementStamp = threadsMap.get(threadId);
            if(statementStamp!=null){
                statementStamp.cancel();
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

}
