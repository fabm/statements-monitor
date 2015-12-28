package concurrence;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatementStamp {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS");
    private long creation;
    private Statement statement;

    public StatementStamp(Statement statement) {
        this.statement = statement;
        creation = System.currentTimeMillis();
    }

    public void cancel(){
        try {
            statement.cancel();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public String timeStamp(){
        return FORMAT.format(new Date(creation));
    }
}
