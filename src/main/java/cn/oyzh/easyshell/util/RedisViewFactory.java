package cn.oyzh.easyshell.util;

import cn.oyzh.easyshell.controller.redis.data.RedisExportDataController;
import cn.oyzh.easyshell.controller.redis.data.RedisImportDataController;
import cn.oyzh.easyshell.controller.redis.data.RedisTransportDataController;
import cn.oyzh.easyshell.controller.redis.key.RedisKeyCopyController;
import cn.oyzh.easyshell.controller.redis.key.RedisKeyMoveController;
import cn.oyzh.easyshell.controller.redis.key.RedisKeyTTLController;
import cn.oyzh.easyshell.controller.redis.row.RedisHashFieldAddController;
import cn.oyzh.easyshell.controller.redis.row.RedisHylogElementsAddController;
import cn.oyzh.easyshell.controller.redis.row.RedisListElementAddController;
import cn.oyzh.easyshell.controller.redis.row.RedisSetMemberAddController;
import cn.oyzh.easyshell.controller.redis.row.RedisStreamMessageAddController;
import cn.oyzh.easyshell.controller.redis.row.RedisZSetCoordinateAddController;
import cn.oyzh.easyshell.controller.redis.row.RedisZSetMemberAddController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.redis.key.RedisHashKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisListKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisSetKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisStreamKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisStringKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisZSetKeyTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;

/**
 * redis页面工厂
 *
 * @author oyzh
 * @since 2025-05-20
 */
public class RedisViewFactory {

    // /**
    //  * 新增SSH连接
    //  *
    //  * @param group 分组
    //  */
    // public static void addConnect(RedisGroup group) {
    //     try {
    //         StageAdapter adapter = StageManager.parseStage(RedisAddConnectController.class, StageManager.getPrimaryStage());
    //         adapter.setProp("group", group);
    //         adapter.display();
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }
    //
    // /**
    //  * 修改连接
    //  *
    //  * @param connect 连接
    //  */
    // public static void updateConnect(ShellConnect connect) {
    //     try {
    //         StageAdapter adapter = StageManager.parseStage(RedisUpdateConnectController.class, StageManager.getPrimaryStage());
    //         adapter.setProp("redisConnect", connect);
    //         adapter.display();
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }
    //
    // /**
    //  * 关于
    //  */
    // public static void about() {
    //     try {
    //         StageManager.showStage(AboutController.class, StageManager.getPrimaryStage());
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }

    /**
     * 传输数据
     *
     * @param connect redis连接
     * @param dbIndex db索引
     */
    public static void transportData(ShellConnect connect, Integer dbIndex) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisTransportDataController.class, StageManager.getPrimaryStage());
            adapter.setProp("sourceConnect", connect);
            adapter.setProp("dbIndex", dbIndex);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    // /**
    //  * 工具
    //  */
    // public static void tool() {
    //     try {
    //         StageManager.showStage(RedisToolController.class, StageManager.getPrimaryStage());
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }

    // /**
    //  * 导入连接
    //  *
    //  * @param file 文件
    //  */
    // public static void importConnect(File file) {
    //     try {
    //         StageAdapter adapter = StageManager.parseStage(RedisImportConnectController.class, StageManager.getPrimaryStage());
    //         adapter.setProp("file", file);
    //         adapter.display();
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }
    //
    // /**
    //  * 导出连接
    //  */
    // public static void exportConnect() {
    //     try {
    //         StageManager.showStage(RedisExportConnectController.class, StageManager.getPrimaryStage());
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }

    /**
     * 导入数据
     *
     * @param connect redis连接
     */
    public static void importData(ShellConnect connect) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisImportDataController.class, StageManager.getPrimaryStage());
            adapter.setProp("connect", connect);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 导出数据
     *
     * @param connect redis连接
     * @param dbIndex db索引
     */
    public static void exportData(ShellConnect connect, Integer dbIndex) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisExportDataController.class, StageManager.getPrimaryStage());
            adapter.setProp("connect", connect);
            adapter.setProp("dbIndex", dbIndex);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    // /**
    //  * 迁移数据
    //  */
    // public static void migrationData() {
    //     try {
    //         StageManager.showStage(RedisMigrationDataController.class, StageManager.getPrimaryStage());
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }

    // /**
    //  * 设置
    //  */
    // public static void setting() {
    //     try {
    //         StageAdapter adapter = StageManager.getStage(SettingController2.class);
    //         if (adapter != null) {
    //             JulLog.info("front setting.");
    //             adapter.toFront();
    //         } else {
    //             JulLog.info("show setting.");
    //             StageManager.showStage(SettingController2.class, StageManager.getPrimaryStage());
    //         }
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }
    //
    // /**
    //  * 主页
    //  */
    // public static void main() {
    //     try {
    //         StageAdapter adapter = StageManager.getStage(MainController.class);
    //         if (adapter != null) {
    //             JulLog.info("front main.");
    //             adapter.toFront();
    //         } else {
    //             JulLog.info("show main.");
    //             StageManager.showStage(MainController.class);
    //         }
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }
    //
    // /**
    //  * 添加跳板
    //  */
    // public static StageAdapter addJump() {
    //     try {
    //         StageAdapter adapter = StageManager.parseStage(RedisAddJumpController.class, StageManager.getPrimaryStage());
    //         adapter.showAndWait();
    //         return adapter;
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    //     return null;
    // }
    //
    // /**
    //  * 编辑跳板
    //  *
    //  * @param config 配置
    //  */
    // public static StageAdapter updateJump(RedisJumpConfig config) {
    //     try {
    //         StageAdapter adapter = StageManager.parseStage(RedisUpdateJumpController.class, StageManager.getPrimaryStage());
    //         adapter.setProp("config", config);
    //         adapter.showAndWait();
    //         return adapter;
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    //     return null;
    // }

    // /**
    //  * 添加键
    //  *
    //  * @param dbIndex db库节点
    //  * @param type   键类型
    //  */
    // public static void addKey(Integer dbIndex, RedisKeyType type) {
    //     try {
    //         StageAdapter adapter = StageManager.parseStage(RedisKeyAddController.class, StageManager.getPrimaryStage());
    //         adapter.setProp("dbIndex", dbIndex);
    //         adapter.setProp("type", type);
    //         adapter.display();
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }

    /**
     * 键ttl
     *
     * @param treeItem 键节点
     */
    public static void ttlKey(RedisKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisKeyTTLController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 添加zset坐标
     *
     * @param treeItem 键节点
     */
    public static StageAdapter zSetCoordinateAdd(RedisZSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisZSetCoordinateAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加zset成员
     *
     * @param treeItem 键节点
     */
    public static StageAdapter zSetMemberAdd(RedisZSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisZSetMemberAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加set成员
     *
     * @param treeItem 键节点
     */
    public static StageAdapter setMemberAdd(RedisSetKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisSetMemberAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加hash字段
     *
     * @param treeItem 键节点
     */
    public static StageAdapter hashFieldAdd(RedisHashKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisHashFieldAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加list元素
     *
     * @param treeItem 键节点
     */
    public static StageAdapter listElementAdd(RedisListKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisListElementAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加stream消息
     *
     * @param treeItem 键节点
     */
    public static StageAdapter streamMessageAdd(RedisStreamKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisStreamMessageAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 添加hylog元素
     *
     * @param treeItem 键节点
     */
    public static StageAdapter hylogElementsAdd(RedisStringKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisHylogElementsAddController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.showAndWait();
            return adapter;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    // /**
    //  * 批量操作
    //  *
    //  * @param dbIndex db树节点
    //  */
    // public static void batchOperation(Integer dbIndex) {
    //     try {
    //         StageAdapter adapter = StageManager.parseStage(RedisKeyBatchOperationController.class, StageManager.getPrimaryStage());
    //         adapter.setProp("dbIndex", dbIndex);
    //         adapter.display();
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         MessageBox.exception(ex);
    //     }
    // }

    /**
     * 移动键
     *
     * @param treeItem 键节点
     */
    public static void moveKey(RedisKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisKeyMoveController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 复制键
     *
     * @param treeItem 键节点
     */
    public static void copyKey(RedisKeyTreeItem treeItem) {
        try {
            StageAdapter adapter = StageManager.parseStage(RedisKeyCopyController.class, StageManager.getPrimaryStage());
            adapter.setProp("treeItem", treeItem);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

}
