package cn.oyzh.easyshell.postgresql.generator.sequence;

import cn.oyzh.easyshell.postgresql.sequence.PostgresqlCreateSequenceParam;
import cn.oyzh.easyshell.postgresql.sequence.PostgresqlSequence;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * PostgreSQL创建序列SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlSequenceCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建序列参数
     */
    private void _generate(PostgresqlCreateSequenceParam param) {
        PostgresqlSequence sequence = param.getSequence();
        this.sqlBuilder.append("CREATE SEQUENCE ");
        this.sqlBuilder.append(DBUtil.wrap(sequence.getSchema(), sequence.getName(), DBDialect.POSTGRESQL));
        // 参数
        if (sequence.getIncrementBy() != null) {
            this.sqlBuilder.append(" INCREMENT BY ").append(sequence.getIncrementBy());
        }
        // PostgreSQL中min/max为0或空时使用默认值
        if (sequence.getMinVal() != null && sequence.getMinVal() != 0) {
            this.sqlBuilder.append(" MINVALUE ").append(sequence.getMinVal());
        } else {
            this.sqlBuilder.append(" NO MINVALUE");
        }
        if (sequence.getMaxVal() != null && sequence.getMaxVal() != 0) {
            this.sqlBuilder.append(" MAXVALUE ").append(sequence.getMaxVal());
        } else {
            this.sqlBuilder.append(" NO MAXVALUE");
        }
        // PostgreSQL使用START WITH指定起始值
        if (sequence.getCurrentVal() != null) {
            this.sqlBuilder.append(" START WITH ").append(sequence.getCurrentVal());
        }
        if (sequence.getCacheSize() != null) {
            this.sqlBuilder.append(" CACHE ").append(sequence.getCacheSize());
        }
        // PostgreSQL无ORDER属性，仅保留CYCLE
        if (sequence.isCycle()) {
            this.sqlBuilder.append(" CYCLE");
        } else {
            this.sqlBuilder.append(" NO CYCLE");
        }
        this.sqlBuilder.append(";");
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建序列参数
     * @return SQL语句
     */
    public String generateSingle(PostgresqlCreateSequenceParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建序列参数
     * @return SQL语句
     */
    public static String generateSqlSingle(PostgresqlCreateSequenceParam param) {
        return new PostgresqlSequenceCreateSqlGenerator().generateSingle(param);
    }
}
