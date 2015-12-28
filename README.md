# statements-monitor
Api to control java.sql.Statement's when we have an only Statement per request/thread for each user

* One instace of UserDBTimeManagement to manage all users requests
* One instance per request of StatementExecutionWithMonitor's implementation
