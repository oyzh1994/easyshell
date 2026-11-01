package cn.oyzh.easyshell.llm;

import cn.oyzh.easyshell.query.dameng.DamengQueryResult;
import cn.oyzh.easyshell.query.mongo.ShellMongoQueryResult;
import cn.oyzh.easyshell.query.mariadb.ShellMariadbQueryResult;
import cn.oyzh.easyshell.query.mysql.ShellMysqlQueryResult;
import cn.oyzh.fx.db.DBColumn;
import cn.oyzh.fx.db.DBRecord;
import cn.oyzh.fx.db.query.DBQueryResult;
import cn.oyzh.fx.db.query.DBQueryResults;

import java.util.List;

/**
 * Formats database execution results for LLM tools.
 */
public final class LLMDBToolsUtil {

    private static final int MAX_ROWS = 1000;

    private LLMDBToolsUtil() {
    }

    public static String format(DBQueryResults<? extends DBQueryResult> results) {
        if (results == null) {
            return "No execution result.";
        }
        if (!results.isSuccess()) {
            return "Execution failed: " + results.getErrMsg();
        }
        if (results.isEmpty()) {
            return "OK";
        }
        StringBuilder output = new StringBuilder();
        for (DBQueryResult result : results.getResults()) {
            if (!output.isEmpty()) {
                output.append('\n');
            }
            output.append(formatResult(result));
        }
        return output.toString();
    }

    private static String formatResult(DBQueryResult result) {
        if (result == null) {
            return "OK";
        }
        if (!result.isSuccess()) {
            return "ERROR: " + result.getMsg();
        }
        if (result.getUpdateCount() > 0) {
            StringBuilder output = new StringBuilder("OK, ")
                    .append(result.getUpdateCount())
                    .append(" rows affected");
            if (result.getUsedMs() > 0) {
                output.append(" (").append(result.getUsedMs()).append(" ms)");
            }
            return output.toString();
        }
        if (result instanceof ShellMysqlQueryResult mysqlResult
                && mysqlResult.getRecords() != null
                && !mysqlResult.getRecords().isEmpty()) {
            return formatRows(mysqlResult.columnList(), mysqlResult.getRecords(), result);
        }
        if (result instanceof ShellMariadbQueryResult mariadbResult
                && mariadbResult.getRecords() != null
                && !mariadbResult.getRecords().isEmpty()) {
            return formatRows(mariadbResult.columnList(), mariadbResult.getRecords(), result);
        }
        if (result instanceof DamengQueryResult damengResult
                && damengResult.getRecords() != null
                && !damengResult.getRecords().isEmpty()) {
            return formatRows(damengResult.columnList(), damengResult.getRecords(), result);
        }
        if (result instanceof ShellMongoQueryResult mongoResult
                && mongoResult.getRecords() != null
                && !mongoResult.getRecords().isEmpty()) {
            return formatRows(mongoResult.columnList(), mongoResult.getRecords(), result);
        }
        return "OK";
    }

    private static String formatRows(
            List<? extends DBColumn> columns,
            List<? extends DBRecord> records,
            DBQueryResult result) {
        if (columns == null || columns.isEmpty() || records == null || records.isEmpty()) {
            return "OK";
        }
        StringBuilder output = new StringBuilder();
        output.append(columns.stream().map(DBColumn::getName).reduce((left, right) -> left + " | " + right).orElse(""));
        output.append('\n');
        int shownRows = Math.min(records.size(), MAX_ROWS);
        for (int rowIndex = 0; rowIndex < shownRows; rowIndex++) {
            DBRecord record = records.get(rowIndex);
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                if (columnIndex > 0) {
                    output.append(" | ");
                }
                output.append(formatValue(record.getValue(columns.get(columnIndex).getName())));
            }
            output.append('\n');
        }
        if (records.size() > shownRows) {
            output.append("... ").append(records.size() - shownRows).append(" more rows omitted");
        } else {
            output.append(records.size()).append(" row(s)");
        }
        if (result.getUsedMs() > 0) {
            output.append(" (").append(result.getUsedMs()).append(" ms)");
        }
        return output.toString();
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        return value.toString()
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("|", "\\|");
    }
}
