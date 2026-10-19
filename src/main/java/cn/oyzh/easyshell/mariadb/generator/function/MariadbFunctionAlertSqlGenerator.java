package cn.oyzh.easyshell.mariadb.generator.function;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.function.MariadbAlertFunctionParam;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.mariadb.routine.MariadbRoutineParam;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * MariaDB修改函数SQL生成器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbFunctionAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改函数参数
     */
    private void _generate(MariadbAlertFunctionParam param) {
        String fullName = DBUtil.wrap(param.getDbName(), param.getFunctionName(), DBDialect.MARIADB);
        MariadbFunction function = param.getFunction();

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
        List<MariadbRoutineParam> params = function.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (MariadbRoutineParam routineParam : params) {
                builder.append("\n")
                        .append(routineParam.getDefinition(false))
                        .append(",");
            }
        }
        StringUtil.deleteLast(builder, ",");
        builder.append("\n) ");
        // 返回值
        MariadbRoutineParam returnParam = function.getReturnParam();
        if (returnParam != null) {
            builder.append(" \nRETURNS ")
                    .append(returnParam.getDefinition(false));
        }
        // 注释
        if (StringUtil.isNotBlank(function.getComment())) {
            builder.append(" \nCOMMENT ")
                    .append(DBUtil.wrapData(function.getComment(), DBDialect.MARIADB));
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

    /**
     * 生成SQL列表
     *
     * @param param 修改函数参数
     * @return SQL列表
     */
    public List<String> generate(MariadbAlertFunctionParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改函数参数
     * @return SQL语句
     */
    public String generateSingle(MariadbAlertFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改函数参数
     * @return SQL列表
     */
    public static List<String> generateSql(MariadbAlertFunctionParam param) {
        return new MariadbFunctionAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改函数参数
     * @return SQL语句
     */
    public static String generateSqlSingle(MariadbAlertFunctionParam param) {
        return new MariadbFunctionAlertSqlGenerator().generateSingle(param);
    }
}
