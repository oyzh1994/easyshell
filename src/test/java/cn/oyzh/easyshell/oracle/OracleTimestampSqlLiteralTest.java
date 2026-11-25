package cn.oyzh.easyshell.oracle;

import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OracleTimestampSqlLiteralTest {

    @Test
    public void wrapsTimestampWithTimeZoneString() {
        OracleColumn column = new OracleColumn();
        column.setType("TIMESTAMP(6) WITH TIME ZONE");

        assertEquals(
                "TO_TIMESTAMP_TZ('2026-11-22 12:34:56.123456+08:00','YYYY-MM-DD HH24:MI:SS.FF6TZH:TZM')",
                ShellOracleDataUtil.parameterizedForSql(column, "2026-11-22T12:34:56.123456+08:00"));
    }

    @Test
    public void wrapsLocalTimestampString() {
        OracleColumn column = new OracleColumn();
        column.setType("TIMESTAMP(6) WITH LOCAL TIME ZONE");

        assertEquals(
                "TO_TIMESTAMP('2026-11-22 12:34:56.123456','YYYY-MM-DD HH24:MI:SS.FF6')",
                ShellOracleDataUtil.parameterizedForSql(column, "2026-11-22T12:34:56.123456"));
    }
}
