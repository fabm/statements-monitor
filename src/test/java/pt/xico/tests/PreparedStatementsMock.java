package pt.xico.tests;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class PreparedStatementsMock {

    public PreparedStatement getPreparedStatement(final long time) {
        try {
            final Thread tStatement = new Thread(new Runnable() {
                public void run() {
                    try {
                        System.out.println("starting statement execution");
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        System.out.println("statement canceled");
                    }
                }
            });
            PreparedStatement statement = mock(PreparedStatement.class);

            doAnswer(new Answer<Boolean>() {
                public Boolean answer(InvocationOnMock invocation) {
                    tStatement.start();
                    try {
                        tStatement.join();
                        System.out.println("statement done");
                    } catch (InterruptedException e) {
                        throw new IllegalStateException("this is not supposed to happen");
                    }
                    return true;
                }
            }).when(statement).execute();
            doAnswer(new Answer() {
                public Object answer(InvocationOnMock invocation) {
                    System.out.println("trying interrupt execution");
                    tStatement.interrupt();
                    return null;
                }
            }).when(statement).cancel();
            return statement;
        } catch (SQLException e) {
            throw new IllegalStateException("This is not supposed to happen");
        }
    }
}
