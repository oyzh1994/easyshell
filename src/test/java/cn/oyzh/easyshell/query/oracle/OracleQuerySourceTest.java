package cn.oyzh.easyshell.query.oracle;

import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OracleQuerySourceTest {

    @Test
    public void resolvesAndFillsQualifiedQuerySource() {
        OracleExecuteResult result = resultWithColumns();

        OracleExecuteResult.SourceName source = result.resolveSourceName(
                "SELECT * FROM \"DEMO\".\"USERS\"", null);
        result.fillColumnSource(source.schema(), source.name());

        assertEquals("DEMO", result.schema());
        assertEquals("USERS", result.tableName());
    }

    @Test
    public void usesCurrentSchemaForUnqualifiedSource() {
        OracleExecuteResult result = resultWithColumns();

        OracleExecuteResult.SourceName source = result.resolveSourceName(
                "SELECT * FROM USERS", "DEMO");
        result.fillColumnSource(source.schema(), source.name());

        assertEquals("DEMO", result.schema());
        assertEquals("USERS", result.tableName());
    }

    private OracleExecuteResult resultWithColumns() {
        OracleColumns columns = new OracleColumns();
        columns.add(new OracleColumn("ID"));
        columns.add(new OracleColumn("EMAIL"));

        OracleExecuteResult result = new OracleExecuteResult();
        result.setColumns(columns);
        return result;
    }
}
