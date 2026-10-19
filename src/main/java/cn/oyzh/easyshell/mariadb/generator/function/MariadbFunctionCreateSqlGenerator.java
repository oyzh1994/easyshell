package cn.oyzh.easyshell.mariadb.generator.function;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.function.MariadbCreateFunctionParam;
import cn.oyzh.easyshell.mariadb.function.MariadbFunction;
import cn.oyzh.easyshell.mariadb.routine.MariadbRoutineParam;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * MariaDB创建函数SQL生成器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbFunctionCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建函数参数
     */
    private void _generate(MariadbCreateFunctionParam param) {
        String dbName = param.getDbName();
        MariadbFunction function = param.getFunction();
        this.sqlBuilder.append("CREATE ");
        // 定义者
        if (StringUtil.isNotBlank(function.getDefiner())) {
            this.sqlBuilder.append(" DEFINER = ")
                    .append(function.getDefiner());
        }
        this.sqlBuilder.append(" FUNCTION ")
                .append(DBUtil.wrap(dbName, function.getName(), DBDialect.MARIADB));
        // 参数
        this.sqlBuilder.append(" (");
        List<MariadbRoutineParam> params = function.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (MariadbRoutineParam routineParam : params) {
                this.sqlBuilder.append("\n")
                        .append(routineParam.getDefinition(false))
                        .append(",");
            }
        }
        StringUtil.deleteLast(this.sqlBuilder, ",");
        this.sqlBuilder.append("\n) ");
        // 返回值
        MariadbRoutineParam returnParam = function.getReturnParam();
        if (returnParam != null) {
            this.sqlBuilder.append(" \nRETURNS ")
                    .append(returnParam.getDefinition(false));
        }
        // 注释
        if (StringUtil.isNotBlank(function.getComment())) {
            this.sqlBuilder.append(" \nCOMMENT ")
                    .append(DBUtil.wrapData(function.getComment(), DBDialect.MARIADB));
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

    /**
     * 生成单条SQL
     *
     * @param param 创建函数参数
     * @return SQL语句
     */
    public String generateSingle(MariadbCreateFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建函数参数
     * @return SQL语句
     */
    public static String generateSqlSingle(MariadbCreateFunctionParam param) {
        return new MariadbFunctionCreateSqlGenerator().generateSingle(param);
    }
}
