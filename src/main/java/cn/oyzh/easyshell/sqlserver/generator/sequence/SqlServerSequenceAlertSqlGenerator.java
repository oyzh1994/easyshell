package cn.oyzh.easyshell.sqlserver.generator.sequence;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerAlertSequenceParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerSequence;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * SqlServer修改序列SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerSequenceAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改序列参数
     */
    private void _generate(SqlServerAlertSequenceParam param) {
        SqlServerSequence sequence = param.getSequence();
        this.sqlBuilder.append("ALTER SEQUENCE ");
        this.sqlBuilder.append(DBUtil.wrap(sequence.getSchema(), sequence.getName(), DBDialect.SQLSERVER));
        int len = this.sqlBuilder.length();
        // 参数
        if (sequence.getIncrementBy() != null) {
            this.sqlBuilder.append(" INCREMENT BY ").append(sequence.getIncrementBy());
        }
        // SqlServer中min/max为0或空时使用默认值
        if (sequence.getMinVal() != null) {
            if (sequence.getMinVal().signum() != 0) {
                this.sqlBuilder.append(" MINVALUE ").append(sequence.getMinVal());
            } else {
                this.sqlBuilder.append(" NO MINVALUE");
            }
        }
        if (sequence.getMaxVal() != null) {
            if (sequence.getMaxVal().signum() != 0) {
                this.sqlBuilder.append(" MAXVALUE ").append(sequence.getMaxVal());
            } else {
                this.sqlBuilder.append(" NO MAXVALUE");
            }
        }
        // SqlServer没有CURRENT VALUE，使用RESTART WITH修改当前值
        if (sequence.getCurrentVal() != null) {
            this.sqlBuilder.append(" RESTART WITH ").append(sequence.getCurrentVal());
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
        if (len == this.sqlBuilder.length()) {
            StringUtil.clear(this.sqlBuilder);
        }
    }

    /**
     * 生成表修改SQL列表
     *
     * @param param 表修改参数
     * @return SQL列表
     */
    public List<String> generate(SqlServerAlertSequenceParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改序列参数
     * @return SQL语句
     */
    public String generateSingle(SqlServerAlertSequenceParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成建序列SQL列表
     *
     * @param param 建表参数
     * @return SQL列表
     */
    public static List<String> generateSql(SqlServerAlertSequenceParam param) {
        return new SqlServerSequenceAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改序列参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SqlServerAlertSequenceParam param) {
        return new SqlServerSequenceAlertSqlGenerator().generateSingle(param);
    }
}
