package cn.oyzh.easyshell.mysql.query;

import cn.oyzh.easyshell.mysql.MysqlHelper;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.query.MysqlQueryResult;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * @author oyzh
 * @since 2024/02/19
 */
public class MysqlExecuteResult extends MysqlQueryResult {

    /**
     * 是否全字段
     */
    private boolean fullColumn;

    @Override
    public boolean hasResult() {
        if (this.updateCount > 0) {
            return false;
        }
        return super.hasResult();
    }

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception {
        // 获取列数
        this.records = new ArrayList<>();
        this.columns = MysqlHelper.parseColumns(resultSet);
        while (resultSet.next()) {
            MysqlRecord record = new MysqlRecord(columns, readonly);
            int colIndex = 1;
            for (MysqlColumn dbColumn : this.columns) {
                Object data = resultSet.getObject(colIndex++);
                // 获取几何值
                if (dbColumn.supportGeometry()) {
                    data = MysqlHelper.getGeometryString(connection, data);
                }
                record.putValue(dbColumn, data);
            }
            this.records.add(record);
        }
    }

    public void setFullColumn(boolean fullColumn) {
        this.fullColumn = fullColumn;
    }

    public boolean isFullColumn() {
        return fullColumn;
    }
}
