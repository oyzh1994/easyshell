package cn.oyzh.easyshell.postgresql;

import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.query.postgresql.PostgresqlExecuteResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PostgresqlQuerySchemaTest {

    @Test
    public void fillsMissingColumnSchemaFromExecutingSchema() {
        PostgresqlColumn column = new PostgresqlColumn("id");
        PostgresqlColumns columns = new PostgresqlColumns();
        columns.add(column);

        PostgresqlExecuteResult result = new PostgresqlExecuteResult();
        result.setColumns(columns);
        result.fillSchema("demo");

        assertEquals("demo", result.schema());
    }

    @Test
    public void keepsSchemaReturnedByMetadata() {
        PostgresqlColumn column = new PostgresqlColumn("id");
        column.setSchema("source_schema");
        PostgresqlColumns columns = new PostgresqlColumns();
        columns.add(column);

        PostgresqlExecuteResult result = new PostgresqlExecuteResult();
        result.setColumns(columns);
        result.fillSchema("demo");

        assertEquals("source_schema", result.schema());
    }
}
