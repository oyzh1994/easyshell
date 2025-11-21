package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.trees.redis.ShellRedisStringKeyTreeItem;
import cn.oyzh.easyshell.util.redis.ShellRedisViewFactory;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * hyLog键tab内容组件
 *
 * @author oyzh
 * @since 2024/05/17
 */
public class ShellRedisHylogKeyController extends ShellRedisKeyController<ShellRedisStringKeyTreeItem> {

    /**
     * 二进制数据
     */
    @FXML
    private FXText binary;

    /**
     * 统计值
     */
    @FXML
    private FXText count;

    /**
     * 数据
     */
    @FXML
    private ShellDataEditor nodeData;

    @Override
    protected void initKey() {
        // 数据处理
        this.firstShowData();
        // 刷新二进制处理
        this.flushBinary();
        // 统计值
        this.count.setText(I18nHelper.count() + ": " + this.treeItem.count());
    }

    /**
     * 刷新二进制处理
     */
    private void flushBinary() {
        // 如果是raw格式，则选择binary
        if (this.treeItem.isRawEncoding()) {
            this.binary.display();
        } else {
            this.binary.disappear();
        }
    }

    /**
     * 重载数据
     */
    @FXML
    private void reloadData() {
        // 放弃保存
        if (this.treeItem.isDataUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            return;
        }
        StageManager.showMask(() -> {
            // 刷新数据
            try {
                this.treeItem.refreshKeyValue();
                // 数据变更
                this.initKey();
                // 刷新内存占用
                this.treeItem.flushMemoryUsage();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 添加统计值
     */
    @FXML
    private void addRow() {
        StageAdapter adapter = ShellRedisViewFactory.redisHylogElementsAdd(this.treeItem);
        // 操作成功
        if (adapter != null && BooleanUtil.isTrue(adapter.getProp("result"))) {
            // 刷新键值
            this.treeItem.refreshKeyValue();
            // 刷新统计值
            this.treeItem.flushCount();
            // 刷新内存占用
            this.treeItem.flushMemoryUsage();
            // 初始化键
            this.initKey();
        }
    }

    @Override
    protected void firstShowData() {
        this.nodeData.showData(this.treeItem.rawValue());
        // 首次设置数据要清除历史
        this.nodeData.forgetHistory();
    }

    // @Override
    // protected void showData(EditorFormatType formatType) {
    //     this.nodeData.showData(this.treeItem.rawValue(), formatType);
    // }

//    /**
//     * hyLog元素添加事件
//     *
//     * @param msg 消息
//     */
//    @EventSubscribe
//    private void onHyLogElementAdded(ShellRedisHyLogElementsAddedEvent msg) {
//        if (this.treeItem == msg.data()) {
//            // 刷新统计值
//            this.treeItem.flushCount();
//            // 初始化键
//            this.initKey();
//            // 刷新内存占用
//            this.treeItem.flushMemoryUsage();
//        }
//    }
}
