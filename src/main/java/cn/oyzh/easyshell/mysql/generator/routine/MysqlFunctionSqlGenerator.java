package cn.oyzh.easyshell.mysql.generator.routine;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.DBDialect;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.util.mysql.DBUtil;

import java.util.List;

/**
 * 函数sql生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class MysqlFunctionSqlGenerator {

    public static final MysqlFunctionSqlGenerator INSTANCE = new MysqlFunctionSqlGenerator();

    public String generate(MysqlFunction function) {
        String sql = "CREATE ";
        // 定义者
        if (StringUtil.isNotBlank(function.getDefiner())) {
            sql += " DEFINER = " + function.getDefiner();
        }
        sql += " FUNCTION " + DBUtil.wrap(function.getName(), DBDialect.MYSQL);
        // 参数
        sql += " (";
        List<MysqlRoutineParam> params = function.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (MysqlRoutineParam param : params) {
                sql = sql + "\n" + param.getDefinition() + ",";
            }
        }
        sql = StringUtil.replaceLast(sql, ",", "");
        sql += ") ";
        // 返回值
        MysqlRoutineParam returnParam = function.getReturnParam();
        if (returnParam != null) {
            sql += " \nRETURNS " + returnParam.getDefinition();
        }
        // 注释
        if (StringUtil.isNotBlank(function.getComment())) {
            sql += " \nCOMMENT " + DBUtil.wrapData(function.getComment());
        }
        // 安全性
        if (StringUtil.isNotBlank(function.getSecurityType())) {
            sql += " \nSQL SECURITY " + function.getSecurityType();
        }
        // 特征
        if (StringUtil.isNotBlank(function.getCharacteristic())) {
            sql += " \n" + function.getCharacteristic();
        }
        sql += " \n" + function.getDefinition();
        return sql;
    }
}
