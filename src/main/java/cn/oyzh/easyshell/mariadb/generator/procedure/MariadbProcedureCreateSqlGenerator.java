package cn.oyzh.easyshell.mariadb.generator.procedure;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.procedure.MariadbCreateProcedureParam;
import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
import cn.oyzh.easyshell.mariadb.routine.MariadbRoutineParam;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * MariaDB创建存储过程SQL生成器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbProcedureCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建存储过程参数
     */
    private void _generate(MariadbCreateProcedureParam param) {
        String dbName = param.getDbName();
        MariadbProcedure procedure = param.getProcedure();
        this.sqlBuilder.append("CREATE ");
        // 定义者
        if (StringUtil.isNotBlank(procedure.getDefiner())) {
            this.sqlBuilder.append(" DEFINER = ")
                    .append(procedure.getDefiner());
        }
        this.sqlBuilder.append(" PROCEDURE ")
                .append(DBUtil.wrap(dbName, procedure.getName(), DBDialect.MARIADB));
        // 参数
        this.sqlBuilder.append(" (");
        List<MariadbRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (MariadbRoutineParam routineParam : params) {
                this.sqlBuilder.append("\n")
                        .append(routineParam.getDefinition(true))
                        .append(",");
            }
        }
        StringUtil.deleteLast(this.sqlBuilder, ",");
        this.sqlBuilder.append("\n) ");
        // 注释
        if (StringUtil.isNotBlank(procedure.getComment())) {
            this.sqlBuilder.append(" \nCOMMENT ")
                    .append(DBUtil.wrapData(procedure.getComment(), DBDialect.MARIADB));
        }
        // 安全性
        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
            this.sqlBuilder.append(" \nSQL SECURITY ")
                    .append(procedure.getSecurityType());
        }
        // 特征
        if (StringUtil.isNotBlank(procedure.getCharacteristic())) {
            this.sqlBuilder.append(" \n")
                    .append(procedure.getCharacteristic());
        }
        this.sqlBuilder.append(" \n")
                .append(procedure.getDefinition());
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建存储过程参数
     * @return SQL语句
     */
    public String generateSingle(MariadbCreateProcedureParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建存储过程参数
     * @return SQL语句
     */
    public static String generateSqlSingle(MariadbCreateProcedureParam param) {
        return new MariadbProcedureCreateSqlGenerator().generateSingle(param);
    }
}
