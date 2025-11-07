// package cn.oyzh.easyshell.util.mysql;
//
// import cn.oyzh.common.thread.ThreadUtil;
// import cn.oyzh.easyshell.mysql.DBClientUtil;
// import cn.oyzh.fx.plus.information.MessageBox;
// import cn.oyzh.fx.plus.window.StageAdapter;
//
// /**
//  * db连接工具类
//  *
//  * @author oyzh
//  * @since 2023/07/01
//  */
// public class DBConnectUtil {
//
//     /**
//      * 测试连接
//      *
//      * @param view   页面
//      * @param dbInfo db信息
//      */
//     public static void testConnect(StageAdapter view, MysqlConnect dbInfo) {
//         ThreadUtil.startVirtual(() -> {
//             try {
//                 view.disable();
//                 view.waitCursor();
//                 view.appendTitle("==连接测试中...");
//                 ShellMysqlClient client = DBClientUtil.newClient(dbInfo);
//                 if (client != null) {
//                     client.start();
//                     if (client.isConnected()) {
//                         client.close();
//                         MessageBox.okToast("连接成功！");
//                     } else {
//                         MessageBox.warn("连接失败，请检查地址是否有效！");
//                     }
//                 }
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//                 MessageBox.exception(ex);
//             } finally {
//                 view.enable();
//                 view.defaultCursor();
//                 view.restoreTitle();
//             }
//         });
//     }
//
//     /**
//      * 关闭连接
//      *
//      * @param client db客户端
//      * @param async  是否异步
//      */
//     public static void close(ShellMysqlClient client, boolean async) {
//         try {
//             if (client != null && client.isConnected()) {
//                 Runnable func = client::close;
//                 if (async) {
//                     ThreadUtil.startVirtual(func);
//                 } else {
//                     func.run();
//                 }
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//     }
//
//     /**
//      * 解析连接
//      *
//      * @param input 输入内容
//      * @return 连接
//      */
//     public static cn.oyzh.easyshell.dto.mysql.MysqlConnect parse(String input) {
//         if (input == null) {
//             return null;
//         }
//         try {
//             String[] words = input.split(" ");
//             cn.oyzh.easyshell.dto.mysql.MysqlConnect connect = new cn.oyzh.easyshell.dto.mysql.MysqlConnect();
//             int type = -1;
//             for (String word : words) {
//                 if (type == 0) {
//                     connect.setHost(word.trim());
//                 } else if (type == 1) {
//                     connect.setPort(Integer.parseInt(word.trim()));
//                 } else if (type == 2) {
//                     connect.setPassword(word.trim());
//                 } else if (type == 3) {
//                     connect.setDb(Integer.parseInt(word.trim()));
//                 }
//                 if (word.equalsIgnoreCase("-h")) {
//                     type = 0;
//                 } else if (word.equalsIgnoreCase("-p")) {
//                     type = 1;
//                 } else if (word.equalsIgnoreCase("-a")) {
//                     type = 2;
//                 } else if (word.equalsIgnoreCase("-n")) {
//                     type = 3;
//                 } else {
//                     type = -1;
//                 }
//             }
//             return connect;
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return null;
//     }
// }
