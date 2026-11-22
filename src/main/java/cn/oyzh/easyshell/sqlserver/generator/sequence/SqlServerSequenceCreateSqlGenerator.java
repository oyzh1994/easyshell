package cn.oyzh.easyshell.sqlserver.generator.sequence;

import cn.oyzh.easyshell.sqlserver.sequence.SqlServerCreateSequenceParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerSequence;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * SqlServer创建序列SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerSequenceCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建序列参数
     */
    private void _generate(SqlServerCreateSequenceParam param) {
        SqlServerSequence sequence = param.getSequence();
        this.sqlBuilder.append("CREATE SEQUENCE ");
        this.sqlBuilder.append(DBUtil.wrap(sequence.getSchema(), sequence.getName(), DBDialect.SQLSERVER));
        // 参数
        if (sequence.getIncrementBy() != null) {
            this.sqlBuilder.append(" INCREMENT BY ").append(sequence.getIncrementBy());
        }
        // SqlServer中min/max为0或空时使用默认值
        if (sequence.getMinVal() != null && sequence.getMinVal().signum() != 0) {
            this.sqlBuilder.append(" MINVALUE ").append(sequence.getMinVal());
        } else {
            this.sqlBuilder.append(" NO MINVALUE");
        }
        if (sequence.getMaxVal() != null && sequence.getMaxVal().signum() != 0) {
            this.sqlBuilder.append(" MAXVALUE ").append(sequence.getMaxVal());
        } else {
            this.sqlBuilder.append(" NO MAXVALUE");
        }
        // SqlServer使用START WITH指定起始值
        if (sequence.getCurrentVal() != null) {
            this.sqlBuilder.append(" START WITH ").append(sequence.getCurrentVal());
        }
        if (sequence.getCacheSize() != null) {
            this.sqlBuilder.append(" CACHE ").append(sequence.getCacheSize());
        }
        // SqlServer无ORDER属性，仅保留CYCLE
        if (sequence.isCycle()) {
            this.sqlBuilder.append(" CYCLE");
        } else {
            this.sqlBuilder.append(" NO CYCLE");
        }
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建序列参数
     * @return SQL语句
     */
    public String generateSingle(SqlServerCreateSequenceParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建序列参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SqlServerCreateSequenceParam param) {
        return new SqlServerSequenceCreateSqlGenerator().generateSingle(param);
    }
}
