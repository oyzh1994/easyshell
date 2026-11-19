package cn.oyzh.easyshell.oracle;

import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.query.oracle.OracleExecuteResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OracleQuerySchemaTest {

    @Test
    public void fillsMissingColumnSchemaFromExecutingSchema() {
        OracleColumn column = new OracleColumn("id");
        OracleColumns columns = new OracleColumns();
        columns.add(column);

        OracleExecuteResult result = new OracleExecuteResult();
        result.setColumns(columns);
        result.fillSchema("DEMO");

        assertEquals("DEMO", result.schema());
    }

    @Test
    public void keepsSchemaReturnedByMetadata() {
        OracleColumn column = new OracleColumn("id");
        column.setSchema("SOURCE_SCHEMA");
        OracleColumns columns = new OracleColumns();
        columns.add(column);

        OracleExecuteResult result = new OracleExecuteResult();
        result.setColumns(columns);
        result.fillSchema("DEMO");

        assertEquals("SOURCE_SCHEMA", result.schema());
    }
}
