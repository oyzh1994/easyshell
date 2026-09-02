package cn.oyzh.easyshell.dameng.generator.job;//package cn.oyzh.easydameng.generator.job;
//
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.dameng.event.DamengEvent;
//import cn.oyzh.easydameng.generator.event.EventCreateSqlGenerator;
//
///**
// * @author oyzh
// * @since 2024-09-10
// */
//public class DamengJobCreateSqlGenerator extends EventCreateSqlGenerator {
//
//    @Override
//    public String generate(DamengEvent event) {
//        // 起始
//        String sql = "DECLARE\n";
//        sql += " job_id INT;\n";
//        sql += " BEGIN\n";
//        sql += "  DBMS_JOB.SUBMIT (\n";
//        sql += "    job => job_id,\n";
//        String acion = event.getDefinition();
//        if (StringUtil.isNotBlank(acion)) {
//            acion = acion.replace("'", "''");
//            sql = sql + "    what => '" + acion + ";',\n";
//        }
//        if (event.getExecuteAt() != null) {
//            sql = sql + "    next_date  => " + event.getExecuteAt() + "\n";
//        } else {
//            sql = sql + "    next_date  => SYSDATE\n";
//        }
//        //if (event.getIntervalValue() != null) {
//        //    sql = sql + "    repeat_interval => 'FREQ=" + event.getEndIntervalField() + "; INTERVAL=" + event.getIntervalValue() + "',\n";
//        //}
//        //if (StringUtil.equalsIgnoreCase(event.getStatus(), "ENABLE")) {
//        //    sql = sql + "    enabled => TRUE\n";
//        //} else {
//        //    sql = sql + "    enabled => FALSE\n";
//        //}
//        sql += "  );\n";
//        sql += "COMMIT;\n";
//        sql += "END;\n";
//        return sql;
//    }
//
//    public static String generateSql(DamengEvent event) {
//        return new DamengJobCreateSqlGenerator().generate(event);
//    }
//}
