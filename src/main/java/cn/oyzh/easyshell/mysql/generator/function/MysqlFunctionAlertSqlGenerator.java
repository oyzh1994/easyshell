package cn.oyzh.easyshell.mysql.generator.function;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.function.MysqlAlertFunctionParam;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * 函数sql生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class MysqlFunctionAlertSqlGenerator extends DBSqlGenerator {

    private void _generate(MysqlAlertFunctionParam param) {
        String fullName = DBUtil.wrap(param.getDbName(), param.getFunctionName(), DBDialect.MYSQL);
        MysqlFunction function = param.getFunction();

        // 删除
        StringBuilder builder = new StringBuilder("DROP FUNCTION IF EXISTS ");
        builder.append(fullName);
        builder.append(";");
        this.sqlList.add(builder.toString());
        StringUtil.clear(builder);

        builder.append("CREATE ");
        // 定义者
        if (StringUtil.isNotBlank(function.getDefiner())) {
            builder.append(" DEFINER = ")
                    .append(function.getDefiner());
        }
        builder.append(" FUNCTION ")
                .append(fullName);
        // 参数
        builder.append(" (");
        List<MysqlRoutineParam> params = function.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (MysqlRoutineParam routineParam : params) {
                builder.append("\n")
                        .append(routineParam.getDefinition(false))
                        .append(",");
            }
        }
        StringUtil.deleteLast(builder, ",");
        builder.append("\n) ");
        // 返回值
        MysqlRoutineParam returnParam = function.getReturnParam();
        if (returnParam != null) {
            builder.append(" \nRETURNS ")
                    .append(returnParam.getDefinition(false));
        }
        // 注释
        if (StringUtil.isNotBlank(function.getComment())) {
            builder.append(" \nCOMMENT ")
                    .append(DBUtil.wrapData(function.getComment(), DBDialect.MYSQL));
        }
        // 安全性
        if (StringUtil.isNotBlank(function.getSecurityType())) {
            builder.append(" \nSQL SECURITY ")
                    .append(function.getSecurityType());
        }
        // 特征
        if (StringUtil.isNotBlank(function.getCharacteristic())) {
            builder.append(" \n")
                    .append(function.getCharacteristic());
        }
        // 定义
        builder.append(" \n")
                .append(function.getDefinition());
        this.sqlList.add(builder.toString());
    }

    public List<String> generate(MysqlAlertFunctionParam param) {
        this._generate(param);
        return this.buildSql();
    }

    public String generateSingle(MysqlAlertFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    public static List<String> generateSql(MysqlAlertFunctionParam param) {
        return new MysqlFunctionAlertSqlGenerator().generate(param);
    }

    public static String generateSqlSingle(MysqlAlertFunctionParam param) {
        return new MysqlFunctionAlertSqlGenerator().generateSingle(param);
    }
}
