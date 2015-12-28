package concurrence;

import java.sql.Statement;

/**
 * Class to wrap statement call to monitor a possible timeout
 *
 * @param <T> the type of executions return
 *
 */
public abstract class StatementExecutionWithMonitor<T> {
    private UserDBTimeManagement userDBTimeManagement;
    private Object user;

    /**
     * Duration of timeout in milliseconds
     */
    protected long timeoutDuration;

    /**
     * @return the value of statement
     */
    protected abstract T execute();

    /**
     * This method is used to obtain the {@link Statement} to save it in {@link UserDBTimeManagement} when {@link #start()} is called
     * @return the statement
     */
    protected abstract Statement getStatement();

    /**
     * @return an implementation of {@link TimeoutStatementExecution} interface to be called the method {@link TimeoutStatementExecution#timeout(Object, long)}
     * when the timeout is reached
     */
    protected abstract TimeoutStatementExecution getTimeoutEvent();

    /**
     * @param userDBTimeManagement instance to control the user statements
     * @param user to bind the statement to a user
     */
    public StatementExecutionWithMonitor(UserDBTimeManagement userDBTimeManagement, Object user) {
        this.userDBTimeManagement = userDBTimeManagement;
        this.user = user;
    }

    /**
     * Starts the monitoring timeout and the statement execution, implemented in {@link #execute()}
     * @return the value returned by the method {@link #execute()}
     */
    public T start() {
        final long threadId = userDBTimeManagement.putStatement(user, getStatement());
        Thread tMonitor = new Thread() {
            synchronized private void waitSync() {
                try {
                    wait(timeoutDuration);
                    try {
                        getTimeoutEvent().timeout(user,threadId);
                    } catch (NullPointerException e) {
                        throw  new IllegalStateException("you must implement getTimeEvent() method");
                    }
                } catch (InterruptedException e) {
                    //this thread only leaves inside the monitor it's not possible to happen this
                }
            }

            public void run() {
                waitSync();
            }
        };
        tMonitor.start();
        T ret = execute();
        tMonitor.interrupt();
        userDBTimeManagement.removeStatement(user, threadId);
        return ret;
    }

}
