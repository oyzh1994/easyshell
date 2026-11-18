package cn.oyzh.easyshell.oracle.generator.routine;//package cn.oyzh.easyshell.oracle.generator.routine;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.oracle.function.OracleFunction;
//import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
//import cn.oyzh.easyshell.oracle.routine.OracleRoutineParam;
//import cn.oyzh.fx.db.DBDialect;
//import cn.oyzh.fx.db.DBSqlGenerator;
//import cn.oyzh.easyshell.util.oracle.DBUtil;
//
//import java.util.List;
//
///**
// * 国策sql生成器
// *
// * @author oyzh
// * @since 2024/08/09
// */
//public class OracleProcedureSqlGenerator extends DBSqlGenerator {
//
//    private void _generate(OracleProcedure procedure) {
//        this.sqlBuilder.append("CREATE OR REPLACE PROCEDURE ");
//        this.sqlBuilder.append(DBUtil.wrap(procedure.getName(), DBDialect.ORACLE));
//        // 参数
//        this.sqlBuilder.append(" (");
//        List<OracleRoutineParam> params = procedure.getParams();
//        if (CollectionUtil.isNotEmpty(params)) {
//            for (OracleRoutineParam param : params) {
//                this.sqlBuilder.append("\n")
//                        .append(param.getDefinition())
//                        .append(",");
//            }
//        }
//        StringUtil.deleteLast(this.sqlBuilder, ",");
//        this.sqlBuilder.append("\n) ");
//        String characteristic = procedure.getCharacteristic();
//        if (StringUtil.isNotBlank(characteristic)) {
//            if (characteristic.contains("PARALLEL_ENABLE")) {
//                this.sqlBuilder.append(" \nPARALLEL_ENABLE");
//            }
//        }
//        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
//            this.sqlBuilder.append(" \nAUTHID ")
//                    .append(procedure.getSecurityType());
//        }
//        // 存储过程体
//        this.sqlBuilder.append(" \nAS\n")
//                .append(procedure.getDefinition());
//    }
//
//    public String generateSingle(OracleProcedure param) {
//        this._generate(param);
//        return this.buildSqlSingle();
//    }
//
//    public static String generateSqlSingle(OracleProcedure param) {
//        return new OracleProcedureSqlGenerator().generateSingle(param);
//    }
//}
