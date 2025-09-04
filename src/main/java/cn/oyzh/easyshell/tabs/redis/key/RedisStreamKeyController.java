package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.fx.svg.pane.ExpandListSVGPane;
import cn.oyzh.easyshell.redis.key.ShellRedisKeyRow;
import cn.oyzh.easyshell.redis.key.ShellRedisStreamValue;
import cn.oyzh.easyshell.trees.redis.key.RedisStreamKeyTreeItem;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

import java.util.List;
import java.util.stream.Collectors;

/**
 * stream键tab内容组件
 *
 * @author oyzh
 * @since 2023/07/07
 */
public class RedisStreamKeyController extends RedisRowKeyController<RedisStreamKeyTreeItem, ShellRedisStreamValue.RedisStreamRow> {

    /**
     * 消息id
     */
    @FXML
    private ReadOnlyTextField streamID;

    /**
     * 数据
     */
    @FXML
    private ShellDataEditor nodeData;

    /**
     * 展开列表面板
     */
    @FXML
    private ExpandListSVGPane expandPane;

    @Override
    protected void initKey() {
        // 初始化表单
        this.initTable();
        // 显示首页
        this.firstPage();
    }

    @Override
    protected List<ShellRedisStreamValue.RedisStreamRow> getRows() {
        List<ShellRedisStreamValue.RedisStreamRow> rows = this.treeItem.rows();
        String filterKW = this.filter.getText();
        if (StringUtil.isNotEmpty(filterKW)) {
            rows = rows.parallelStream()
                    .filter(r -> StringUtil.containsIgnoreCase(r.getValue(), filterKW) ||
                            StringUtil.containsIgnoreCase(String.valueOf(r.getId()), filterKW))
                    .collect(Collectors.toList());
        }
        return rows;
    }

    @FXML
    @Override
    protected void addRow() {
        StageAdapter adapter = ShellViewFactory.redisStreamMessageAdd(this.treeItem);
        // 操作成功
        if (adapter != null && BooleanUtil.isTrue(adapter.getProp("result"))) {
            this.firstPage();
            // 刷新内存占用
            this.treeItem.flushMemoryUsage();
        }
    }

    @Override
    protected void initRow(ShellRedisStreamValue.RedisStreamRow row) {
        super.initRow(row);
        if (row == null) {
            this.nodeData.clear();
            this.nodeData.disable();
            this.streamID.clear();
            this.streamID.disable();
        } else {
            this.streamID.setText(row.getId());
            this.streamID.enable();
            this.nodeData.enable();
        }
    }

    @FXML
    @Override
    protected void copyRow() {
        if (this.treeItem.isSelectRow()) {
            String builder = I18nHelper.keyName() + " : " + this.treeItem.key() + System.lineSeparator() +
                    I18nHelper.messageId() + " : " + this.treeItem.currentRow().getId() + System.lineSeparator() +
                    I18nHelper.content() + " : " + this.treeItem.currentRow().getValue();
            ClipboardUtil.setStringAndTip(builder);
        }
    }

    @Override
    protected void firstShowData() {
        ShellRedisStreamValue.RedisStreamRow row = this.treeItem.rawValue();
        if (row != null) {
            this.nodeData.showData(row.getValue());
            this.nodeData.forgetHistory();
        }
    }

    // @Override
    // protected void showData(EditorFormatType formatType) {
    //     ShellRedisStreamValue.RedisStreamRow row = this.treeItem.rawValue();
    //     if (row != null) {
    //         this.nodeData.showData(row.getValue(), formatType);
    //     }
    // }

    @FXML
    @Override
    protected void deleteRow() {
        if (!this.treeItem.isSelectRow()) {
            return;
        }
        ShellRedisKeyRow row = this.treeItem.currentRow();
        if (!MessageBox.confirm(I18nHelper.deleteMessage() + ":" + row.getValue())) {
            return;
        }
        if (!this.treeItem.deleteRow()) {
            return;
        }
        // 移除
        if (this.listTable.getItemSize() > 1) {
            this.listTable.removeItem(row);
        } else {// 刷新
            this.firstPage();
        }
        // 刷新内存占用
        this.treeItem.flushMemoryUsage();
    }

    @Override
    protected void clearRow() {
        this.nodeData.clear();
        this.nodeData.disable();
    }

    @FXML
    private void expendList() {
        if (this.expandPane.isCollapse()) {
            NodeGroupUtil.disappear(this.getTab(), "stream_list");
            this.nodeData.setFlexHeight("100% - 82");
            this.expandPane.expand();
        } else {
            NodeGroupUtil.display(this.getTab(), "stream_list");
            this.nodeData.setFlexHeight("100% - 408");
            this.expandPane.collapse();
        }
    }

//    /**
//     * stream消息添加事件
//     *
//     * @param msg 消息
//     */
//    @EventSubscribe
//    private void onStreamMessageAdded(RedisStreamMessageAddedEvent msg) {
//        if (this.treeItem == msg.data()) {
//            this.firstPage();
//            // 刷新内存占用
//            this.treeItem.flushMemoryUsage();
//        }
//    }
}
