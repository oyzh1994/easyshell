package cn.oyzh.easyshell.mysql.generator.procedure;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.procedure.MysqlAlertProcedureParam;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * MySQL修改存储过程SQL生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class MysqlProcedureAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改存储过程参数
     */
    private void _generate(MysqlAlertProcedureParam param) {
        String fullName = DBUtil.wrap(param.getDbName(), param.getProcedureName(), DBDialect.MYSQL);
        MysqlProcedure procedure = param.getProcedure();
        // 删除
        StringBuilder builder = new StringBuilder("DROP PROCEDURE IF EXISTS ");
        builder.append(fullName);
        builder.append(";");
        this.sqlList.add(builder.toString());
        StringUtil.clear(builder);

        builder.append("CREATE ");
        // 定义者
        if (StringUtil.isNotBlank(procedure.getDefiner())) {
            builder.append(" DEFINER = ")
                    .append(procedure.getDefiner());
        }
        builder.append(" PROCEDURE ")
                .append(fullName);
        // 参数
        builder.append(" (");
        List<MysqlRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (MysqlRoutineParam routineParam : params) {
                builder.append("\n")
                        .append(routineParam.getDefinition(true))
                        .append(",");
            }
        }
        StringUtil.deleteLast(builder, ",");
        builder.append("\n) ");
        // 注释
        if (StringUtil.isNotBlank(procedure.getComment())) {
            builder.append(" \nCOMMENT ")
                    .append(DBUtil.wrapData(procedure.getComment(), DBDialect.MYSQL));
        }
        // 安全性
        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
            builder.append(" \nSQL SECURITY ")
                    .append(procedure.getSecurityType());
        }
        // 特征
        if (StringUtil.isNotBlank(procedure.getCharacteristic())) {
            builder.append(" \n")
                    .append(procedure.getCharacteristic());
        }
        // 定义
        builder.append(" \n")
                .append(procedure.getDefinition());
        this.sqlList.add(builder.toString());
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改存储过程参数
     * @return SQL列表
     */
    public List<String> generate(MysqlAlertProcedureParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改存储过程参数
     * @return SQL语句
     */
    public String generateSingle(MysqlAlertProcedureParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改存储过程参数
     * @return SQL列表
     */
    public static List<String> generateSql(MysqlAlertProcedureParam param) {
        return new MysqlProcedureAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改存储过程参数
     * @return SQL语句
     */
    public static String generateSqlSingle(MysqlAlertProcedureParam param) {
        return new MysqlProcedureAlertSqlGenerator().generateSingle(param);
    }
}
