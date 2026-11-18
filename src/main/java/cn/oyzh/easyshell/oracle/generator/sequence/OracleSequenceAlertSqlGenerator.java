package cn.oyzh.easyshell.oracle.generator.sequence;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.sequence.OracleAlertSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;
import java.util.Objects;

/**
 * Oracle修改序列SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleSequenceAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改序列参数
     */
    private void _generate(OracleAlertSequenceParam param) {
        OracleSequence sequence = param.getSequence();
        OracleSequence originalSequence = param.getOriginalSequence();
        this.sqlBuilder.append("ALTER SEQUENCE ");
        this.sqlBuilder.append(DBUtil.wrap(sequence.getSchema(), sequence.getName(), DBDialect.ORACLE));
        int len = this.sqlBuilder.length();
        // 参数
        if (changed(sequence.getIncrementBy(), originalSequence == null ? null : originalSequence.getIncrementBy())) {
            this.sqlBuilder.append(" INCREMENT BY ").append(sequence.getIncrementBy());
        }
        if (changed(sequence.getMaxVal(), originalSequence == null ? null : originalSequence.getMaxVal())) {
            this.sqlBuilder.append(" MAXVALUE ").append(sequence.getMaxVal());
        }
        if (changed(sequence.getMinVal(), originalSequence == null ? null : originalSequence.getMinVal())) {
            this.sqlBuilder.append(" MINVALUE ").append(sequence.getMinVal());
        }
        if (changed(sequence.getCacheSize(), originalSequence == null ? null : originalSequence.getCacheSize())) {
            this.sqlBuilder.append(" CACHE ").append(sequence.getCacheSize());
        }
        if (originalSequence == null || sequence.isOrder() != originalSequence.isOrder()) {
            if (sequence.isOrder()) {
                this.sqlBuilder.append(" ORDER ");
            } else {
                this.sqlBuilder.append(" NOORDER ");
            }
        }
        if (originalSequence == null || sequence.isCycle() != originalSequence.isCycle()) {
            if (sequence.isCycle()) {
                this.sqlBuilder.append(" CYCLE ");
            } else {
                this.sqlBuilder.append(" NOCYCLE ");
            }
        }
        if (len == this.sqlBuilder.length()) {
            StringUtil.clear(this.sqlBuilder);
        }
    }

    private static boolean changed(Object value, Object originalValue) {
        return value != null && (originalValue == null || !Objects.equals(value, originalValue));
    }

    /**
     * 生成表修改SQL列表
     *
     * @param param 表修改参数
     * @return SQL列表
     */
    public List<String> generate(OracleAlertSequenceParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改序列参数
     * @return SQL语句
     */
    public String generateSingle(OracleAlertSequenceParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成建序列SQL列表
     *
     * @param param 建表参数
     * @return SQL列表
     */
    public static List<String> generateSql(OracleAlertSequenceParam param) {
        return new OracleSequenceAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改序列参数
     * @return SQL语句
     */
    public static String generateSqlSingle(OracleAlertSequenceParam param) {
        return new OracleSequenceAlertSqlGenerator().generateSingle(param);
    }
}
