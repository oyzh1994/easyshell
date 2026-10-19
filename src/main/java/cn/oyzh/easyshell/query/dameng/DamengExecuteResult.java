package cn.oyzh.easyshell.query.dameng;

import cn.oyzh.easyshell.dameng.ShellDamengHelper;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.record.DamengRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * 达梦执行结果
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class DamengExecuteResult extends DamengQueryResult {

    /**
     * 是否全字段
     */
    private boolean fullColumn;

    @Override
    public void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception {
        // 获取列数
        this.records = new ArrayList<>();
        this.columns = ShellDamengHelper.parseColumns(resultSet);
        while (resultSet.next()) {
            DamengRecord record = new DamengRecord(columns, readonly);
            int colIndex = 1;
            for (DamengColumn dbColumn : this.columns) {
                Object data = resultSet.getObject(colIndex++);
//                // 获取几何值
//                if (dbColumn.supportGeometry()) {
//                    data = DamengHelper.getGeometryString(connection, data);
//                }
                record.putValue(dbColumn, data);
            }
            this.records.add(record);
        }
    }

    /**
     * 设置是否全字段
     *
     * @param fullColumn 是否全字段
     */
    public void setFullColumn(boolean fullColumn) {
        this.fullColumn = fullColumn;
    }

    /**
     * 是否全字段
     *
     * @return 结果
     */
    public boolean isFullColumn() {
        return fullColumn;
    }
}
