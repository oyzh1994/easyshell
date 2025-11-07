package cn.oyzh.easyshell.mysql.generator.routine;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.util.mysql.DBUtil;

import java.util.List;

/**
 * 国策sql生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class MysqlProcedureSqlGenerator {

    public static final MysqlProcedureSqlGenerator INSTANCE = new MysqlProcedureSqlGenerator();

    public String generate(MysqlProcedure procedure) {
        String sql = "CREATE ";
        // 定义者
        if (StringUtil.isNotBlank(procedure.getDefiner())) {
            sql += " DEFINER = " + procedure.getDefiner();
        }
        sql += " PROCEDURE " + DBUtil.wrap(procedure.getName(), DBDialect.MYSQL);
        // 参数
        sql += " (";
        List<MysqlRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (MysqlRoutineParam param : params) {
                sql = sql + "\n" + param.getDefinition() + ",";
            }
        }
        sql = StringUtil.replaceLast(sql, ",", "");
        sql += ") ";
        // 注释
        if (StringUtil.isNotBlank(procedure.getComment())) {
            sql += " \nCOMMENT " + DBUtil.wrapData(procedure.getComment());
        }
        // 安全性
        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
            sql += " \nSQL SECURITY " + procedure.getSecurityType();
        }
        // 特征
        if (StringUtil.isNotBlank(procedure.getCharacteristic())) {
            sql += " \n" + procedure.getCharacteristic();
        }
        sql += " \n" + procedure.getDefinition();
        return sql;
    }
}
