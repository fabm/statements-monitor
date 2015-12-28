package concurrence;

import java.sql.Statement;

/**
 * Interface that was called in {@link StatementExecutionWithMonitor}
 */
public interface TimeoutStatementExecution {
    /**
     * Method called in {@link StatementExecutionWithMonitor#start()} when timeout is reached
     * @param user in application context
     * @param threadId identifier returned in {@link UserDBTimeManagement#putStatement(Object, Statement)}
     */
    void timeout(Object user, long threadId);
}
