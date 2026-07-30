package cn.oyzh.easyshell.db;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class DBSqlGenerator {

    protected List<String> sqlList = new ArrayList<>();

    protected StringBuilder sqlBuilder = new StringBuilder();

    protected List<String> buildSql() {
        if (this.sqlList == null) {
            this.sqlList = new ArrayList<>();
        }
        if (this.sqlBuilder != null && !this.sqlBuilder.isEmpty()) {
            this.sqlList.addFirst(this.sqlBuilder.toString().trim());
        }
        return this.sqlList;
    }

    protected String buildSqlSingle() {
        if (this.sqlList == null) {
            this.sqlList = new ArrayList<>();
        }
        StringBuilder builder = new StringBuilder();
        if (this.sqlBuilder != null && !this.sqlBuilder.isEmpty()) {
            builder.append(this.sqlBuilder.toString().trim());
        }
        for (String sql : this.sqlList) {
            builder.append("\n").append(sql);
        }
        return builder.toString().trim();
    }
}

