package concurrence;

import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * class to manage users requests
 */
public class UserDBTimeManagement {
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Map<Object, StatementStampManager> map;

    private StatementStampManager createMap(Object user) {
        map = new HashMap<Object, StatementStampManager>();
        return createStatementStampManager(user);
    }

    private StatementStampManager createStatementStampManager(Object user) {
        StatementStampManager statementStampManager = new StatementStampManager();
        map.put(user, statementStampManager);
        return statementStampManager;
    }


    /**
     * @param user in the applications context
     * @return a {@link StatementStampManager} instance to monitor him requests/threads
     */
    public StatementStampManager get(Object user) {
        readWriteLock.readLock().lock();
        try {
            return map.get(user);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * @param user in the applications context
     * @param statement to monitor
     * @return the threadId registered in {@link StatementStampManager} instance, that can be obtained in
     */
    public long putStatement(Object user, Statement statement) {
        readWriteLock.writeLock().lock();
        try {
            StatementStampManager statementStampManager;
            if (map == null) {
                statementStampManager = createMap(user);
            }else {
                statementStampManager = map.get(user);
            }

            if (statementStampManager == null) {
                statementStampManager = createStatementStampManager(user);
            }
            StatementStamp statementStamp = new StatementStamp(statement);
            return statementStampManager.put(statementStamp);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }


    /**
     * @param user in the applications context
     * @param threadId returned by {@link #putStatement(Object, Statement)}
     */
    public void removeStatement(Object user, long threadId) {
        if (get(user).remove(threadId)) {
            readWriteLock.writeLock().lock();
            try {
                map.remove(user);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
    }
}
