package cn.oyzh.easyshell.dameng.generator.event;//package cn.oyzh.easydameng.generator.event;
//
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.dameng.event.DamengEvent;
//
///**
// * @author oyzh
// * @since 2024-09-10
// */
//public class DamengEventCreateSqlGenerator extends EventCreateSqlGenerator {
//
//    @Override
//    public String generate(DamengEvent event) {
//        // 起始
//        String sql = "BEGIN\n";
//        sql += "  DBMS_SCHEDULER.CREATE_JOB (\n";
//        sql += "    job_type => 'PLSQL_BLOCK',\n";
//        sql += "    job_name => '" + event.getName() + "',\n";
//        String acion = event.getDefinition();
//        if (StringUtil.isNotBlank(acion)) {
//            acion = acion.replace("'", "''");
//            sql = sql + "    job_action => 'BEGIN " + acion + "; END;',\n";
//        } else {
//            sql = sql + "    job_action => 'BEGIN  END;',\n";
//        }
//        if (event.getExecuteAt() != null) {
//            sql = sql + "    start_date => " + event.getExecuteAt() + ",\n";
//        } else {
//            sql = sql + "    start_date => SYSDATE,\n";
//        }
//        if (event.getIntervalValue() != null) {
//            sql = sql + "    repeat_interval => 'FREQ=" + event.getIntervalField() + "; INTERVAL=" + event.getIntervalValue() + "',\n";
//        }
//        if (StringUtil.equalsIgnoreCase(event.getStatus(), "ENABLE")) {
//            sql = sql + "    enabled => TRUE\n";
//        } else {
//            sql = sql + "    enabled => FALSE\n";
//        }
//        sql += "  );\n";
//        sql += "END;\n";
//        return sql;
//    }
//}
