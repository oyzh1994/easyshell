package cn.oyzh.easyshell.dameng.generator.sequence;

import cn.oyzh.easyshell.dameng.sequence.DamengCreateSequenceParam;
import cn.oyzh.easyshell.dameng.sequence.DamengSequence;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * 达梦创建序列SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengSequenceCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建序列参数
     */
    private void _generate(DamengCreateSequenceParam param) {
        DamengSequence sequence = param.getSequence();
        this.sqlBuilder.append("CREATE SEQUENCE ");
        this.sqlBuilder.append(DBUtil.wrap(sequence.getSchema(), sequence.getName(), DBDialect.DAMENG));
        // 参数
        if (sequence.getIncrementBy() != null) {
            this.sqlBuilder.append(" INCREMENT BY ").append(sequence.getIncrementBy());
        }
        if (sequence.getMaxVal() != null) {
            this.sqlBuilder.append(" MAXVALUE ").append(sequence.getMaxVal());
        }
        if (sequence.getMaxVal() != null) {
            this.sqlBuilder.append(" MINVALUE ").append(sequence.getMinVal());
        }
        if (sequence.getCurrentVal() != null) {
            this.sqlBuilder.append(" CURRENT VALUE ").append(sequence.getCurrentVal());
        }
        if (sequence.getCacheSize() != null) {
            this.sqlBuilder.append(" CACHE ").append(sequence.getCacheSize());
        }
        if (sequence.isOrder()) {
            this.sqlBuilder.append(" ORDER ");
        } else {
            this.sqlBuilder.append(" NOORDER ");
        }
        if (sequence.isCycle()) {
            this.sqlBuilder.append(" CYCLE ");
        } else {
            this.sqlBuilder.append(" NOCYCLE ");
        }
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建序列参数
     * @return SQL语句
     */
    public String generateSingle(DamengCreateSequenceParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建序列参数
     * @return SQL语句
     */
    public static String generateSqlSingle(DamengCreateSequenceParam param) {
        return new DamengSequenceCreateSqlGenerator().generateSingle(param);
    }
}
