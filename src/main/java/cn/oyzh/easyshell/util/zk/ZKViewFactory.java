// package cn.oyzh.easyshell.util.zk;
//
// /**
//  * zk页面工厂
//  *
//  * @author oyzh
//  * @since 2025-05-29
//  */
// public class ZKViewFactory {
//
//     // /**
//     //  * 新增SSH连接
//     //  *
//     //  * @param group 分组
//     //  */
//     // public static void addConnect(ZKGroup group) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ZKAddConnectController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("group", group);
//     //         adapter.display();
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//     //
//     // /**
//     //  * 修改连接
//     //  *
//     //  * @param connect 连接
//     //  */
//     // public static void updateConnect(ShellConnect connect) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ZKUpdateConnectController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("zkConnect", connect);
//     //         adapter.display();
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//     //
//     // /**
//     //  * 关于
//     //  */
//     // public static void about() {
//     //     try {
//     //         StageManager.showStage(AboutController.class, StageManager.getPrimaryStage());
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//
//     // /**
//     //  * 传输数据
//     //  *
//     //  * @param connect zk连接
//     //  */
//     // public static void transportData(ShellConnect connect) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ShellZKTransportDataController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("sourceConnect", connect);
//     //         adapter.display();
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//
//     // /**
//     //  * 工具
//     //  */
//     // public static void tool() {
//     //     try {
//     //         StageManager.showStage(ZKToolController.class, StageManager.getPrimaryStage());
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//
//     // /**
//     //  * 导入连接
//     //  *
//     //  * @param file 文件
//     //  */
//     // public static void importConnect(File file) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ZKImportConnectController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("file", file);
//     //         adapter.display();
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//
//     // /**
//     //  * 导出连接
//     //  */
//     // public static void exportConnect() {
//     //     try {
//     //         StageManager.showStage(ZKExportConnectController.class, StageManager.getPrimaryStage());
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//
//     // /**
//     //  * 导入数据
//     //  *
//     //  * @param connect zk连接
//     //  */
//     // public static void importData(ShellConnect connect) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ShellZKImportDataController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("connect", connect);
//     //         adapter.display();
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//     //
//     // /**
//     //  * 导出数据
//     //  *
//     //  * @param connect  zk连接
//     //  * @param nodePath 节点路径
//     //  */
//     // public static void exportData(ShellConnect connect, String nodePath) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ShellZKExportDataController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("connect", connect);
//     //         adapter.setProp("nodePath", nodePath);
//     //         adapter.display();
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//
//     // /**
//     //  * 迁移数据
//     //  */
//     // public static void migrationData() {
//     //     try {
//     //         StageManager.showStage(ZKMigrationDataController.class, StageManager.getPrimaryStage());
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//
//     // /**
//     //  * 添加权限
//     //  *
//     //  * @param nodeItem zk节点
//     //  * @param client   zk客户端
//     //  */
//     // public static StageAdapter addACL(ShellZKNodeTreeItem nodeItem, ShellZKClient client) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ShellZKAddACLController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("zkItem", nodeItem);
//     //         adapter.setProp("zkClient", client);
//     //         adapter.showAndWait();
//     //         return adapter;
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     //     return null;
//     // }
//     //
//     // /**
//     //  * 修改权限
//     //  *
//     //  * @param nodeItem zk节点
//     //  * @param client   zk客户端
//     //  * @param acl      权限
//     //  */
//     // public static StageAdapter updateACL(ShellZKNodeTreeItem nodeItem, ShellZKClient client, ZKACL acl) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ShellZKUpdateACLController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("acl", acl);
//     //         adapter.setProp("zkItem", nodeItem);
//     //         adapter.setProp("zkClient", client);
//     //         adapter.showAndWait();
//     //         return adapter;
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     //     return null;
//     // }
//
//     //
//     // /**
//     //  * 设置
//     //  */
//     // public static void setting() {
//     //     try {
//     //         StageAdapter adapter = StageManager.getStage(SettingController2.class);
//     //         if (adapter != null) {
//     //             JulLog.info("front setting.");
//     //             adapter.toFront();
//     //         } else {
//     //             JulLog.info("show setting.");
//     //             StageManager.showStage(SettingController2.class, StageManager.getPrimaryStage());
//     //         }
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//
//     /**
//     //  * 主页
//     //  */
//     // public static void main() {
//     //     try {
//     //         StageAdapter adapter = StageManager.getStage(MainController.class);
//     //         if (adapter != null) {
//     //             JulLog.info("front main.");
//     //             adapter.toFront();
//     //         } else {
//     //             JulLog.info("show main.");
//     //             StageManager.showStage(MainController.class);
//     //         }
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//
//     // /**
//     //  * 认证节点
//     //  *
//     //  * @param nodeItem zk节点
//     //  * @param client   zk客户端
//     //  */
//     // public static void authNode(ShellZKNodeTreeItem nodeItem, ShellZKClient client) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ShellZKAuthNodeController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("zkItem", nodeItem);
//     //         adapter.setProp("zkClient", client);
//     //         adapter.display();
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//     //
//     // /**
//     //  * 添加zk子节点
//     //  *
//     //  * @param nodeItem zk节点
//     //  * @param client   zk客户端
//     //  */
//     // public static void addNode(ShellZKNodeTreeItem nodeItem, ShellZKClient client) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ShellZKAddNodeController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("zkItem", nodeItem);
//     //         adapter.setProp("zkClient", client);
//     //         adapter.display();
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     // }
//
//     // /**
//     //  * 添加跳板
//     //  */
//     // public static StageAdapter addJump() {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ZKAddJumpController.class, StageManager.getPrimaryStage());
//     //         adapter.showAndWait();
//     //         return adapter;
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     //     return null;
//     // }
//     //
//     // /**
//     //  * 编辑跳板
//     //  *
//     //  * @param config 配置
//     //  */
//     // public static StageAdapter updateJump(ZKJumpConfig config) {
//     //     try {
//     //         StageAdapter adapter = StageManager.parseStage(ZKUpdateJumpController.class, StageManager.getPrimaryStage());
//     //         adapter.setProp("config", config);
//     //         adapter.showAndWait();
//     //         return adapter;
//     //     } catch (Exception ex) {
//     //         ex.printStackTrace();
//     //         MessageBox.exception(ex);
//     //     }
//     //     return null;
//     // }
//
// }
