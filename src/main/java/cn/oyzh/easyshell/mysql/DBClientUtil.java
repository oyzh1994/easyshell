// package cn.oyzh.easyshell.mysql;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.mysql.MysqlClient;
//
// /**
//  * db客户端封装
//  *
//  * @author oyzh
//  * @since 2020/6/8
//  */
// public class DBClientUtil {
//
//     public static MysqlClient newClient(ShellConnect info) {
//         if (DBDialect.valueOf(info.getType()) == DBDialect.MYSQL) {
//             return new MysqlClient(info);
//         }
//         return null;
//     }
//
// }
