package pt.xico.tests;

import concurrence.StatementExecutionWithMonitor;
import concurrence.StatementStamp;
import concurrence.TimeoutStatementExecution;
import concurrence.UserDBTimeManagement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class UsersTest {

    private UserDBTimeManagement userDBTimeManagement;
    private PreparedStatementsMock preparedStatementsMock;

    @BeforeMethod
    private void init() {
        userDBTimeManagement = new UserDBTimeManagement();
        preparedStatementsMock = new PreparedStatementsMock();
    }

    private void createRequest(String user, final int psDuration, final int timeoutDurationPS) {
        new StatementExecutionWithMonitor<Void>(userDBTimeManagement, user) {
            private PreparedStatement ps;

            @Override
            protected Void execute() {
                try {
                    ps.execute();
                    return null;
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            protected Statement getStatement() {
                if (ps == null) {
                    this.timeoutDuration = timeoutDurationPS;
                    this.ps = preparedStatementsMock.getPreparedStatement(psDuration);
                }
                return ps;
            }

            @Override
            protected TimeoutStatementExecution getTimeoutEvent() {
                return new TimeoutStatementExecution() {
                    public void timeout(Object user, long threadId) {
                        System.out.println("Time out user " + user + " threadId:" + threadId);
                    }
                };
            }
        }.start();

    }

    @Test(invocationCount = 10)
    public void simulateRequests() throws InterruptedException {
        ArrayList<Thread> requests = new ArrayList<Thread>();
        createRequests2500Duration(requests, 20, "1", 2000);
        createRequests2500Duration(requests, 30, "2", 2000);
        createRequests2500Duration(requests, 20, "3", 2000);
        createRequests2500DurationBut1(requests, 30, "4", 2000);

        Thread.sleep(1000);

        printUserRequests("1", 20);
        printUserRequests("2", 30);

        userDBTimeManagement.get("1").cancel(1);
        Thread.sleep(1000);
        printUserRequests("1", 19);
        Assert.assertEquals(30,userDBTimeManagement.get("2").getSnapshot().size());
        Assert.assertEquals(20,userDBTimeManagement.get("3").getSnapshot().size());

        printUserRequests("4", 29);

        for (Thread request : requests) {
            try {
                request.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void printUserRequests(String user, int assertQt) {
        System.out.println("---Snapshot from user " + user);
        Set<Map.Entry<Long, StatementStamp>> snapshot = userDBTimeManagement.get(user).getSnapshot().entrySet();
        Assert.assertEquals(assertQt, snapshot.size());
        for (Map.Entry<Long,StatementStamp> line : snapshot) {
            System.out.printf("%2s %s\n", line.getKey(), line.getValue().timeStamp());
        }
        System.out.println();
    }

    private void createRequests2500Duration(ArrayList<Thread> requests, int quantity, final String user, final int timeoutDuration) {
        for (int i = 0; i < quantity; i++) {
            final Thread request = new Thread() {
                @Override
                public void run() {
                    createRequest(user, 2500, timeoutDuration);
                }
            };
            request.start();
            requests.add(request);
        }
    }

    private void createRequests2500DurationBut1(ArrayList<Thread> requests, final int quantity, final String user, final int timeoutDuration) {
        final long randIndex = (long) (Math.random() * quantity);
        for (int i = 0; i < quantity; i++) {
            final int finalI = i;
            final Thread request = new Thread() {
                @Override
                public void run() {
                    if (finalI == randIndex) {
                        createRequest(user, 1000, timeoutDuration);
                    } else {
                        createRequest(user,2500,timeoutDuration);
                    }
                }
            };
            request.start();
            requests.add(request);
        }
    }
}
