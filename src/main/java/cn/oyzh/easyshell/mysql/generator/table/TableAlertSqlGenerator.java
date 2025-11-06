package cn.oyzh.easyshell.mysql.generator.table;// package cn.oyzh.easymysql.generator.table;
//
// import cn.oyzh.easyshell.mysql.DBDialect;
// import cn.oyzh.easyshell.mysql.table.MysqlTable;
// import cn.oyzh.easyshell.mysql.table.MysqlAlertTableParam;
// import lombok.Getter;
//
// /**
//  *
//  * @author oyzh
//  * @since 2024/01/25
//  */
// public abstract class TableAlertSqlGenerator {
//
//     @Getter
//     private DBDialect dialect;
//
//     public TableAlertSqlGenerator(DBDialect dialect) {
//         this.dialect = dialect;
//     }
//
//     public abstract String generate(MysqlAlertTableParam table);
//
//     public static String generate(DBDialect dialect, MysqlAlertTableParam table) {
//         return switch (dialect) {
//             case MYSQL -> new MysqlTableAlertSqlGenerator().generate(table);
//             default -> null;
//         };
//     }
// }
