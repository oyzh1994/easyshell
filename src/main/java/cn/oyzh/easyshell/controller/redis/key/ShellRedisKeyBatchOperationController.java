package cn.oyzh.easyshell.controller.redis.key;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.redis.ShellRedisDatabaseComboBox;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyUtil;
import cn.oyzh.easyshell.redis.batch.ShellRedisScanSimpleResult;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import redis.clients.jedis.params.ScanParams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * redis键批量操作业务
 *
 * @author oyzh
 * @since 2020/10/09
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "redis/key/shellRedisKeyBatchOperation.fxml"
)
public class ShellRedisKeyBatchOperationController extends StageController {

    /**
     * 根节点
     */
    @FXML
    private FXTabPane root;

    /**
     * ttl值
     */
    @FXML
    private NumberTextField ttl;

    /**
     * 删除键表达式
     */
    @FXML
    private ClearableTextField pattern1;

    /**
     * 设置ttl键表达式
     */
    @FXML
    private ClearableTextField pattern2;

    /**
     * 移动键表达式
     */
    @FXML
    private ClearableTextField pattern4;

    /**
     * 复制键表达式
     */
    @FXML
    private ClearableTextField pattern5;

    /**
     * 移动键表达式
     */
    @FXML
    private ClearableTextField pattern6;

    /**
     * 删除键表达式
     */
    @FXML
    private MsgTextArea keys1;

    /**
     * 设置ttl键表达式
     */
    @FXML
    private MsgTextArea keys2;

    /**
     * 清空库键表达式
     */
    @FXML
    private MsgTextArea keys3;

    /**
     * 移动键表达式
     */
    @FXML
    private MsgTextArea keys4;

    /**
     * 复制键表达式
     */
    @FXML
    private MsgTextArea keys5;

    /**
     * 移动键表达式
     */
    @FXML
    private MsgTextArea keys6;

    /**
     * db索引
     */
    private Integer dbIndex;

    /**
     * redis客户端对象
     */
    private ShellRedisClient client;

    // /**
    //  * 树键
    //  */
    // private Integer dbIndex;

    /**
     * 移动目标库
     */
    @FXML
    private ShellRedisDatabaseComboBox moveTargetDB;

    /**
     * 复制目标库
     */
    @FXML
    private ShellRedisDatabaseComboBox copyTargetDB;

    /**
     * 复制时替换
     */
    @FXML
    private FXCheckBox replaceOnCopy;

    /**
     * 异步任务
     */
    private Thread execTask;

    /**
     * 删除键
     */
    @FXML
    private void delKeys() {
        this.client.throwSentinelException();
        // 扫描参数
        String pattern = StringUtil.isBlank(this.pattern1.getText()) ? "*" : this.pattern1.getText();
        if (MessageBox.confirm(I18nHelper.deleteKeys())) {
            this.execTask = ThreadUtil.start(() -> {
                try {
                    NodeGroupUtil.disable(this.root, "exec");
                    this.stage.appendTitle("====" + I18nHelper.executeIng() + "====");
                    // 扫描键
                    List<String> keys = this.findKeys(this.keys1, pattern);
                    int succCount = 0;
                    int failCount = 0;
                    for (String key : keys) {
                        if (ThreadUtil.isInterrupted()) {
                            break;
                        }
                        // 扫描数据
                        long result = this.client.del(this.dbIndex, key);
                        // 查询结束
                        if (result == 1) {
                            succCount++;
                            this.keys1.appendLine(I18nHelper.deleteKey() + ": [" + key + "] " + I18nHelper.success());
                        } else {
                            failCount++;
                            this.keys1.appendLine(I18nHelper.deleteKey() + ": [" + key + "] " + I18nHelper.fail());
                        }
                    }
                    ShellEventUtil.redisKeyFlushed(this.client.getShellConnect(), this.dbIndex);
                    // 提示信息
                    String msg = I18nHelper.success() + ":" + succCount + ", " + I18nHelper.fail() + ":" + failCount;
                    MessageBox.info(msg);
                } finally {
                    this.stopExec();
                }
            }, 200);
        }
    }

    /**
     * 设置键过期时间
     */
    @FXML
    private void expireKeys() {
        try {
            this.client.throwSentinelException();
            String pattern = StringUtil.isBlank(this.pattern2.getText()) ? "*" : this.pattern2.getText();
            this.execTask = ThreadUtil.start(() -> {
                try {
                    NodeGroupUtil.disable(this.root, "exec");
                    this.stage.appendTitle("====" + I18nHelper.executeIng() + "====");
                    // 扫描键
                    List<String> keys = this.findKeys(this.keys2, pattern);
                    int succCount = 0;
                    int failCount = 0;
                    long ttl = this.ttl.getValue();
                    for (String key : keys) {
                        if (ThreadUtil.isInterrupted()) {
                            break;
                        }
                        long result;
                        if (ttl == -1) {
                            result = this.client.persist(this.dbIndex, key);
                        } else {
                            result = this.client.expire(this.dbIndex, key, ttl, null);
                        }
                        if (result == 1) {
                            succCount++;
                            this.keys2.appendLine(I18nHelper.handleKey() + ": [" + key + "] " + I18nHelper.success());
                        } else {
                            failCount++;
                            this.keys2.appendLine(I18nHelper.handleKey() + ": [" + key + "] " + I18nHelper.fail());
                        }
                    }
                    ShellEventUtil.redisKeyFlushed(this.client.shellConnect(), this.dbIndex);
                    // 提示信息
                    String msg = I18nHelper.success() + ":" + succCount + ", " + I18nHelper.fail() + ":" + failCount;
                    MessageBox.info(msg);
                } finally {
                    this.stopExec();
                }
            }, 200);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 清空数据库
     */
    @FXML
    private void flushDB() {
        try {
            this.client.throwSentinelException();
            if (MessageBox.confirm(ShellI18nHelper.redisBatchTip3())) {
                this.execTask = ThreadUtil.start(() -> {
                    try {
                        NodeGroupUtil.disable(this.root, "exec");
                        this.stage.appendTitle("====" + I18nHelper.executeIng() + "====");
                        this.client.flushDB(this.dbIndex);
                        ShellEventUtil.redisKeyFlushed(this.client.getShellConnect(), this.dbIndex);
                        MessageBox.okToast(I18nHelper.operationSuccess());
                    } finally {
                        this.stopExec();
                    }
                }, 200);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 移动键
     */
    @FXML
    private void moveKeys() {
        try {
            this.client.throwClusterException();
            this.client.throwSentinelException();
            int targetDBIndex = this.moveTargetDB.getDB();
            if (targetDBIndex == this.dbIndex) {
                MessageBox.warn(ShellI18nHelper.redisBatchTip4());
                return;
            }
            String pattern = StringUtil.isBlank(this.pattern4.getText()) ? "*" : this.pattern4.getText();
            if (MessageBox.confirm(ShellI18nHelper.redisBatchTip5())) {
                this.execTask = ThreadUtil.start(() -> {
                    try {
                        NodeGroupUtil.disable(this.root, "exec");
                        this.stage.appendTitle("====" + I18nHelper.executeIng() + "====");
                        // 扫描键
                        List<String> keys = this.findKeys(this.keys4, pattern);
                        int succCount = 0;
                        int failCount = 0;
                        for (String key : keys) {
                            if (ThreadUtil.isInterrupted()) {
                                break;
                            }
                            // 扫描数据
                            long result = this.client.move(key, this.dbIndex, targetDBIndex);
                            if (result == 1) {
                                succCount++;
                                this.keys4.appendLine(I18nHelper.moveKey() + ": [" + key + "] " + I18nHelper.success());
                            } else {
                                failCount++;
                                this.keys4.appendLine(I18nHelper.moveKey() + ": [" + key + "] " + I18nHelper.fail());
                            }
                        }
                        ShellEventUtil.redisKeysMoved(this.client.shellConnect(), this.dbIndex, targetDBIndex);
                        // 提示信息
                        String msg = I18nHelper.success() + ":" + succCount + ", " + I18nHelper.fail() + ":" + failCount;
                        MessageBox.info(msg);
                    } finally {
                        this.stopExec();
                    }
                }, 200);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 复制键
     */
    @FXML
    private void copyKeys() {
        try {
            this.client.throwClusterException();
            this.client.throwSentinelException();
            this.client.throwCommandException("copy");
            int targetDBIndex = this.copyTargetDB.getDB();
            if (targetDBIndex == this.dbIndex) {
                MessageBox.warn(ShellI18nHelper.redisBatchTip4());
                return;
            }
            String pattern = StringUtil.isBlank(this.pattern5.getText()) ? "*" : this.pattern5.getText();
            if (MessageBox.confirm(ShellI18nHelper.redisBatchTip6())) {
                this.execTask = ThreadUtil.start(() -> {
                    try {
                        NodeGroupUtil.disable(this.root, "exec");
                        this.stage.appendTitle("====" + I18nHelper.executeIng() + "====");
                        // 扫描键
                        List<String> keys = this.findKeys(this.keys5, pattern);
                        int succCount = 0;
                        int failCount = 0;
                        for (String key : keys) {
                            if (ThreadUtil.isInterrupted()) {
                                break;
                            }
                            // 扫描数据
                            boolean result = this.client.copy(this.dbIndex, key, key, targetDBIndex, this.replaceOnCopy.isSelected());
                            if (result) {
                                succCount++;
                                this.keys5.appendLine(I18nHelper.copyKey() + ": [" + key + "] " + I18nHelper.success());
                            } else {
                                failCount++;
                                this.keys5.appendLine(I18nHelper.copyKey() + ": [" + key + "] " + I18nHelper.fail());
                            }
                        }
                        ShellEventUtil.redisKeysCopied(this.client.shellConnect(), keys, this.dbIndex, targetDBIndex);
                        // 提示信息
                        String msg = I18nHelper.success() + ":" + succCount + ", " + I18nHelper.fail() + ":" + failCount;
                        MessageBox.info(msg);
                    } finally {
                        this.stopExec();
                    }
                }, 200);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 统计键
     */
    @FXML
    private void countKeys() {
        this.execTask = ThreadUtil.start(() -> {
            try {
                NodeGroupUtil.disable(this.root, "exec");
                this.stage.appendTitle("====" + I18nHelper.executeIng() + "====");
                // 扫描参数
                String pattern = StringUtil.isBlank(this.pattern6.getText()) ? "*" : this.pattern6.getText();
                long keySize = 0;
                String cursor = null;
                while (!ThreadUtil.isInterrupted()) {
                    ScanParams params = new ScanParams();
                    params.count(1000);
                    params.match(pattern);
                    ShellRedisScanSimpleResult result = ShellRedisKeyUtil.scanKeysSimple(this.dbIndex, cursor, params, this.client);
                    keySize += result.keySize();
                    cursor = result.getCursor();
                    this.keys6.setTextExt(I18nHelper.found() + ":" + keySize);
                    if (result.isFinish()) {
                        break;
                    }
                }
            } finally {
                this.stopExec();
            }
        }, 200);
    }

    /**
     * 停止执行
     */
    @FXML
    private void stopExec() {
        ThreadUtil.interrupt(this.execTask);
        NodeGroupUtil.enable(this.root, "exec");
        this.stage.restoreTitle();
    }

    /**
     * 显示受影响的键
     *
     * @param keys 键列表
     * @param area 文本域组件
     */
    private void showKeys(Collection<String> keys, FXTextArea area) {
        area.clear();
        if (CollectionUtil.isNotEmpty(keys)) {
            List<String> texts = new ArrayList<>(keys.size());
            int index = 0;
            for (String key : keys) {
                texts.add(++index + ") " + key);
            }
            area.appendLines(texts);
        }
    }

    /**
     * 寻找键
     *
     * @param area    文本域组件
     * @param pattern 模式
     * @return 键列表
     */
    private List<String> findKeys(FXTextArea area, String pattern) {
        List<String> keys = new ArrayList<>(1000);
        String cursor = null;
        while (!ThreadUtil.isInterrupted()) {
            ScanParams params = new ScanParams();
            params.count(1000);
            params.match(pattern);
            ShellRedisScanSimpleResult result = ShellRedisKeyUtil.scanKeysSimple(this.dbIndex, cursor, params, this.client);
            keys.addAll(result.getKeys());
            cursor = result.getCursor();
            area.setTextExt(I18nHelper.found() + ":" + keys.size());
            if (result.isFinish()) {
                break;
            }
        }
        return keys;
    }

    /**
     * 显示受影响的删除键
     */
    @FXML
    private void showKeys1() {
        List<String> keys = ShellRedisKeyUtil.scanKeys(this.dbIndex, this.client, this.pattern1.getText(), 2000);
        this.showKeys(keys, this.keys1);
    }

    /**
     * 显示受影响的ttl键
     */
    @FXML
    private void showKeys2() {
        List<String> keys = ShellRedisKeyUtil.scanKeys(this.dbIndex, this.client, this.pattern2.getText(), 2000);
        this.showKeys(keys, this.keys2);
    }

    /**
     * 显示受影响的ttl键
     */
    @FXML
    private void showKeys3() {
        List<String> keys = ShellRedisKeyUtil.scanKeys(this.dbIndex, this.client, "*", 2000);
        this.showKeys(keys, this.keys3);
    }

    /**
     * 显示受影响的移动键
     */
    @FXML
    private void showKeys4() {
        List<String> keys = ShellRedisKeyUtil.scanKeys(this.dbIndex, this.client, this.pattern4.getText(), 2000);
        this.showKeys(keys, this.keys4);
    }

    /**
     * 显示受影响的复制键
     */
    @FXML
    private void showKeys5() {
        List<String> keys = ShellRedisKeyUtil.scanKeys(this.dbIndex, this.client, this.pattern5.getText(), 2000);
        this.showKeys(keys, this.keys5);
    }

    @Override
    protected void bindListeners() {
        this.pattern1.addTextChangeListener((observable, oldValue, newValue) -> this.keys1.clear());
        this.pattern2.addTextChangeListener((observable, oldValue, newValue) -> this.keys2.clear());
        this.pattern4.addTextChangeListener((observable, oldValue, newValue) -> this.keys4.clear());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        this.client = this.getProp("client");
        this.dbIndex = this.getProp("dbIndex");
        this.moveTargetDB.setDbCount(this.client.databases());
        this.moveTargetDB.selectFirst();
        this.copyTargetDB.setDbCount(this.client.databases());
        this.copyTargetDB.selectFirst();
        this.stage.title(this.stage.title() + "-db" + dbIndex);
        this.root.selectedTabChanged((observableValue, tab, t1) -> {
            if (t1 instanceof NodeGroup group) {
                group.setGroupId("active");
            }
            if (tab instanceof NodeGroup group) {
                group.setGroupId("exec");
            }
        });
    }

    @Override
    public void onWindowHiding(WindowEvent event) {
        super.onWindowHiding(event);
        ThreadUtil.interrupt(this.execTask);
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.redis.title.key.batchOperation");
    }
}
