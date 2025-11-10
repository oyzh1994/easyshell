package cn.oyzh.easyshell.db.sql;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/2/26
 */
public class DruidSqlParser extends DBSqlParser {

    private final DbType dbType;

    public DruidSqlParser(String sqlContent, DBDialect dialect) {
        super(sqlContent, dialect);
        this.dbType = switch (dialect) {
            case MYSQL:
                yield DbType.mysql;
            default:
                yield null;
        };
    }

    @Override
    public String removeComment() {
        // StringBuilder builder = new StringBuilder();
        // AtomicBoolean commentFlag = new AtomicBoolean(false);
        // this.sqlContent.lines().forEach(line -> {
        //     // 单行注释1
        //     if (line.stripLeading().startsWith("-- ")) {
        //         return;
        //     }
        //     // 单行注释2
        //     if (line.stripLeading().startsWith("#")) {
        //         return;
        //     }
        //     // 多行注释开始
        //     if (line.stripLeading().startsWith("/*")) {
        //         commentFlag.set(true);
        //     }
        //     // 多行注释结束
        //     if (line.stripTrailing().endsWith("*/")) {
        //         commentFlag.set(false);
        //         return;
        //     }
        //     // 正常行
        //     if (!commentFlag.get() && StringUtil.isNotBlank(line)) {
        //         builder.append(line).append("\n");
        //     }
        // });
        // return builder.toString();
        return ShellMysqlUtil.removeComment(this.sqlContent);
    }

    private Boolean single;

    private Boolean select;

    private List<SQLStatement> sqlStatements;

    @Override
    public boolean isSingle() {
        if (this.single != null) {
            return this.single;
        }
        return this.sqlStatements != null && this.sqlStatements.size() == 1;
    }

    @Override
    public boolean isSelect() {
        if (this.select != null) {
            return this.select;
        }
        if (CollectionUtil.isNotEmpty(this.sqlStatements)) {
            SQLStatement statement = this.sqlStatements.getFirst();
            SchemaStatVisitor visitor = new SchemaStatVisitor(this.dbType);
            statement.accept(visitor);
            Map<TableStat.Name, TableStat> tables = visitor.getTables();
            if (CollectionUtil.isNotEmpty(tables)) {
                TableStat stat = CollectionUtil.getFirst(tables.values());
                return stat != null && StringUtil.equalsIgnoreCase("Select", stat.toString());
            }
        }
        return false;
    }

    @Override
    public boolean isFullColumn() {
        if (CollectionUtil.isNotEmpty(this.sqlStatements)) {
            SQLStatement statement = this.sqlStatements.getFirst();
            SchemaStatVisitor visitor = new SchemaStatVisitor(this.dbType);
            statement.accept(visitor);
            Collection<TableStat.Column> columns = visitor.getColumns();
            if (CollectionUtil.isNotEmpty(columns)) {
                for (TableStat.Column column : columns) {
                    if (StringUtil.equals("*", column.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> parseSql() {
        String sqlContent = this.removeComment();
        List<String> sqlList = new ArrayList<>();
        // druid无法解析这些语句，直接返回
        if (StringUtil.startWithAnyIgnoreCase(sqlContent,
                "SHOW VARIABLES LIKE",
                "SHOW CREATE EVENT"
        )) {
            sqlList.add(sqlContent);
            this.single = true;
            this.select = true;
            return sqlList;
        }
        this.sqlStatements = SQLUtils.parseStatements(sqlContent, this.dbType, SQLParserFeature.SkipComments);
        for (SQLStatement sqlStatement : this.sqlStatements) {
            String sql = sqlStatement.toString();
            sql = sql.replace("\n", " ");
            sqlList.add(sql);
        }
        this.single = null;
        this.select = null;
        return sqlList;
    }

    @Override
    public String parseSingleSql() throws Exception {
        String sqlContent = this.removeComment();
        SQLStatement statement = SQLUtils.parseSingleStatement(sqlContent, this.dbType, false);
        this.sqlStatements = new ArrayList<>();
        this.sqlStatements.add(statement);
        String sql = statement.toString();
        sql = sql.replace("\n", " ");
        return sql;
    }

    @Override
    public String prettySql() {
        SQLParserFeature[] features = new SQLParserFeature[]{
                SQLParserFeature.KeepComments,
                SQLParserFeature.KeepSelectListOriginalString
        };
        return SQLUtils.format(this.sqlContent, this.dbType, null, null, features);
    }
}
