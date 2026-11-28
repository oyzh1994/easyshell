package cn.oyzh.easyshell.h2.generator.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.trigger.H2Trigger;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.sql.DBSqlParser;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * H2创建触发器SQL生成器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2TriggerCreateSqlGenerator extends DBSqlGenerator {

    private static final Pattern CLASS_NAME = Pattern.compile(
            "[A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)+");

    /**
     * 生成SQL片段。
     *
     * @param trigger   触发器对象
     * @param tableName 表名
     */
    private void _generate(H2Trigger trigger, String tableName) {
        String policy = StringUtil.blankToDefault(trigger.getPolicy(), "AFTER INSERT")
                .trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        String body = trigger.triggerBody();
        this.sqlBuilder.append("CREATE TRIGGER ")
                .append(H2GeneratorUtil.quote(trigger.getName()))
                .append(' ').append(policy)
                .append(" ON ").append(H2GeneratorUtil.quote(tableName));
        if (CLASS_NAME.matcher(body).matches()) {
            this.sqlBuilder.append(" CALL ").append(H2GeneratorUtil.quote(body)).append(';');
        } else {
            this.sqlBuilder.append(" AS ")
                    .append(H2GeneratorUtil.literal(sqlTriggerSource(body)))
                    .append(';');
        }
    }

    /**
     * 将 SQL 动作包装为 H2 支持的 Trigger 源码。
     *
     * @param body SQL 动作
     * @return Trigger 源码
     */
    private static String sqlTriggerSource(String body) {
        List<String> statements = DBSqlParser.parseSql(body, DBDialect.H2).stream()
                .map(H2GeneratorUtil::stripSemicolon)
                .filter(StringUtil::isNotBlank)
                .toList();
        if (statements.isEmpty()) {
            throw new IllegalArgumentException("H2 trigger body is empty");
        }
        StringBuilder source = new StringBuilder("""
                import java.sql.*;
                import org.h2.api.Trigger;
                @CODE
                Trigger create() {
                    return new Trigger() {
                        public void init(Connection conn, String schemaName, String triggerName,
                                         String tableName, boolean before, int type) {
                        }
                        public void fire(Connection conn, Object[] oldRow, Object[] newRow)
                                throws SQLException {
                            if (conn.getAutoCommit()) {
                                conn.setAutoCommit(false);
                            }
                            try (Statement statement = conn.createStatement()) {
                """);
        for (String statement : statements) {
            source.append("                statement.execute(")
                    .append(javaString(statement))
                    .append(");\n");
        }
        return source.append("""
                            }
                        }
                        public void close() {
                        }
                        public void remove() {
                        }
                    };
                }
                """).toString();
    }

    private static String javaString(String value) {
        return '"' + value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n") + '"';
    }

    /**
     * 生成单条SQL。
     *
     * @param trigger   触发器对象
     * @param tableName 表名
     * @return SQL语句
     */
    public String generateSingle(H2Trigger trigger, String tableName) {
        this._generate(trigger, tableName);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL。
     *
     * @param trigger   触发器对象
     * @param tableName 表名
     * @return SQL语句
     */
    public static String generateSqlSingle(H2Trigger trigger, String tableName) {
        return new H2TriggerCreateSqlGenerator().generateSingle(trigger, tableName);
    }
}
