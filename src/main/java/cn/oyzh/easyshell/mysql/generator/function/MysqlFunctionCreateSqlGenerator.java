package cn.oyzh.easyshell.mysql.generator.function;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.DBDialect;
import cn.oyzh.easyshell.db.DBSqlGenerator;
import cn.oyzh.easyshell.mysql.function.MysqlCreateFunctionParam;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;

import java.util.List;

/**
 * 函数sql生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class MysqlFunctionCreateSqlGenerator extends DBSqlGenerator {

    private void _generate(MysqlCreateFunctionParam param) {
        String dbName = param.getDbName();
        MysqlFunction function = param.getFunction();
        this.sqlBuilder.append("CREATE ");
        // 定义者
        if (StringUtil.isNotBlank(function.getDefiner())) {
            this.sqlBuilder.append(" DEFINER = ")
                    .append(function.getDefiner());
        }
        this.sqlBuilder.append(" FUNCTION ")
                .append(ShellMysqlUtil.wrap(dbName, function.getName(), DBDialect.MYSQL));
        // 参数
        this.sqlBuilder.append(" (");
        List<MysqlRoutineParam> params = function.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (MysqlRoutineParam routineParam : params) {
                this.sqlBuilder.append("\n")
                        .append(routineParam.getDefinition(false))
                        .append(",");
            }
        }
        StringUtil.deleteLast(this.sqlBuilder, ",");
        this.sqlBuilder.append(") ");
        // 返回值
        MysqlRoutineParam returnParam = function.getReturnParam();
        if (returnParam != null) {
            this.sqlBuilder.append(" \nRETURNS ")
                    .append(returnParam.getDefinition(false));
        }
        // 注释
        if (StringUtil.isNotBlank(function.getComment())) {
            this.sqlBuilder.append(" \nCOMMENT ")
                    .append(ShellMysqlUtil.wrapData(function.getComment()));
        }
        // 安全性
        if (StringUtil.isNotBlank(function.getSecurityType())) {
            this.sqlBuilder.append(" \nSQL SECURITY ")
                    .append(function.getSecurityType());
        }
        // 特征
        if (StringUtil.isNotBlank(function.getCharacteristic())) {
            this.sqlBuilder.append(" \n")
                    .append(function.getCharacteristic());
        }
        this.sqlBuilder.append(" \n")
                .append(function.getDefinition());
    }

    public String generateSingle(MysqlCreateFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    public static String generateSqlSingle(MysqlCreateFunctionParam param) {
        return new MysqlFunctionCreateSqlGenerator().generateSingle(param);
    }
}
