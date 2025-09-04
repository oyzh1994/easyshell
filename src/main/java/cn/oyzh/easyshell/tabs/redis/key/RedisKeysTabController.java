package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.common.util.CostUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.filter.RedisKeyFilterTextField;
import cn.oyzh.easyshell.filter.RedisKeyFilterTypeComboBox;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.trees.redis.key.RedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.RedisKeyTreeView;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.svg.pane.SortSVGPane;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeWidthResizer;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.TreeItem;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-03
 */
public class RedisKeysTabController extends ParentTabController {

    // /**
    //  * 根节点
    //  */
    // @FXML
    // private FXHBox root;

    /**
     * tab节点
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 键数据
     */
    @FXML
    private RedisKeyDataController keyDataController;

    /**
     * 键信息
     */
    @FXML
    private RedisKeyInfoController keyInfoController;

    /**
     * 左侧节点
     */
    @FXML
    private FXVBox leftBox;

    /**
     * redis客户端
     */
    private ShellRedisClient client;

    public ShellRedisClient getClient() {
        return client;
    }

    public void setClient(ShellRedisClient client) {
        this.client = client;
    }

    // public RedisKeyTreeItem getActiveItem() {
    //     return activeItem;
    // }

    // public void setActiveItem(RedisKeyTreeItem activeItem) {
    //     this.activeItem = activeItem;
    // }

    /**
     * 当前激活的节点
     */
    private RedisKeyTreeItem activeItem;

    /**
     * 节点数
     */
    @FXML
    private RedisKeyTreeView treeView;

    /**
     * 过滤内容
     */
    @FXML
    private RedisKeyFilterTextField filterKW;

    /**
     * 过滤类型
     */
    @FXML
    private RedisKeyFilterTypeComboBox filterType;

    // /**
    //  * 收藏面板
    //  */
    // @FXML
    // private CollectSVGPane collectPane;

    /**
     * 排序面板
     */
    @FXML
    private SortSVGPane sortPane;

    // /**
    //  * 初始化
    //  */
    // public void init(ShellConnect connect) {
    //     this.client = new ShellRedisClient(connect);
    //     // 加载根节点
    //     StageManager.showMask(() -> {
    //         try {
    //             this.client.start();
    //             if (!this.client.isConnected()) {
    //                 MessageBox.warn(I18nHelper.connectFail());
    //                 return;
    //             }
    //             this.treeView.setClient(client);
    //             this.treeView.loadItems();
    //         } catch (Throwable ex) {
    //             ex.printStackTrace();
    //             MessageBox.exception(ex);
    //         }
    //     });
    // }

    /**
     * 初始化
     */
    public void init(ShellRedisClient client) {
        this.client = client;
        this.treeView.setClient(client);
        this.treeView.loadItems();
    }

    @FXML
    private void doFilter() {
        String kw = this.filterKW.getTextTrim();
        // 过滤模式
        byte mode = this.filterKW.filterMode();
        // 过滤范围
        byte scope = this.filterKW.filterScope();
        // 过滤类型
        int type = this.filterType.getSelectedIndex();
        // 设置高亮是否匹配大小写
        this.treeView.setHighlightMatchCase(mode == 3 || mode == 1);
        // 仅在过滤键的情况下设置节点高亮
        if (scope == 2 || scope == 0) {
            this.treeView.setHighlightText(kw);
        } else {
            this.treeView.setHighlightText(null);
        }
//        // 仅在过滤数据的情况下设置内容高亮
//        if (scope == 2 || scope == 1&&this.keyDataController) {
//            this.nodeData.setHighlightText(kw);
//        } else {
//            this.nodeData.setHighlightText(this.dataSearch.getTextTrim());
//        }
        this.treeView.getItemFilter().setKw(kw);
        this.treeView.getItemFilter().setScope(scope);
        this.treeView.getItemFilter().setMatchMode(mode);
        this.treeView.getItemFilter().setType((byte) type);
        this.treeView.filter();
    }

    // @FXML
    // private void addKey() {
    //     ShellViewFactory.addRedisKey(this.client, this.dbIndex(), null);
    // }

    // @FXML
    // private void deleteKey() {
    //     if (this.activeItem != null) {
    //         this.activeItem.delete();
    //     }
    // }

    // @FXML
    // private void collectKey() {
    //     if (this.activeItem != null) {
    //         if (this.collectPane.isCollect()) {
    //             this.activeItem.unCollect();
    //             this.collectPane.unCollect();
    //         } else {
    //             this.activeItem.collect();
    //             this.collectPane.collect();
    //         }
    //     }
    // }

    // @FXML
    // private void refreshKey() {
    //     StageManager.showMask(() -> {
    //         try {
    //             this.treeView.loadItems();
    //         } catch (Exception ex) {
    //             ex.printStackTrace();
    //             MessageBox.exception(ex);
    //         }
    //     });
    // }

    @FXML
    private void positionNode() {
        this.treeView.positionItem();
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 监听选中变化
        this.treeView.selectItemChanged(this::initItem);
        // 过滤处理
        this.filterType.selectedIndexChanged((observable, oldValue, newValue) -> this.doFilter());
        // 拉伸辅助
        NodeWidthResizer resizer = new NodeWidthResizer(this.leftBox, Cursor.DEFAULT, this::resizeLeft);
        resizer.widthLimit(240f, 750f);
        resizer.initResizeEvent();
    }

    /**
     * 左侧组件重新布局
     *
     * @param newWidth 新宽度
     */
    private void resizeLeft(Float newWidth) {
        if (newWidth != null && !Float.isNaN(newWidth)) {
            // 设置组件宽
            this.leftBox.setRealWidth(newWidth);
            this.tabPane.setLayoutX(newWidth);
            this.tabPane.setFlexWidth("100% - " + newWidth);
            this.leftBox.parentAutosize();
        }
    }

    /**
     * 初始化节点
     *
     * @param treeItem 节点
     */
    private void initItem(TreeItem<?> treeItem) {
        StageManager.showMask(() -> {
            CostUtil.record();
            try {
                if (treeItem instanceof RedisKeyTreeItem keyTreeItem) {
                    // 设置激活节点
                    this.activeItem = keyTreeItem;
                    // 初始化数据
                    this.initData();
                    // 刷新tab
                    this.flushTab();
                    // 启用组件
                    this.tabPane.enable();
                    // 设置焦点
                    this.treeView.focusNode();
                } else {
                    this.tabPane.disable();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            } finally {
                CostUtil.printCost();
            }
        });
    }

    /**
     * 初始化数据
     */
    public void initData() {
        if (this.activeItem != null) {
            this.keyDataController.init(this.activeItem);
            this.keyInfoController.init(this.activeItem);
            // this.collectPane.setCollect(this.activeItem.isCollect());
        }
    }

    /**
     * 刷新ttl
     */
    public void flushTTL() {
        this.keyDataController.flushTTL();
    }

    @FXML
    private void sortTree() {
        if (this.sortPane.isAsc()) {
            this.treeView.sortAsc();
            this.sortPane.desc();
        } else {
            this.treeView.sortDesc();
            this.sortPane.asc();
        }
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.keyDataController, this.keyInfoController);
    }

    /**
     * 导入数据
     */
    @FXML
    public void importData() {
        ShellViewFactory.redisImportData(this.shellConnect());
    }

    /**
     * 导出数据
     */
    @FXML
    private void exportData() {
        ShellViewFactory.redisExportData(this.shellConnect(), null);
    }

    /**
     * 获取连接
     *
     * @return 连接
     */
    public ShellConnect shellConnect() {
        return this.client.shellConnect();
    }

    // /**
    //  * 键过滤
    //  */
    // @FXML
    // private void doKeyFilter(MouseEvent event) {
    //     String filterPattern = this.treeView.getFilterPattern();
    //     PopupAdapter popup = PopupManager.parsePopup(ShellRedisKeyFilterPopupController.class);
    //     popup.setProp("pattern", filterPattern);
    //     SVGGlyph glyph = (SVGGlyph) event.getSource();
    //     if (glyph == null) {
    //         glyph = (SVGGlyph) event.getTarget();
    //     }
    //     popup.setSubmitHandler(o -> {
    //         if (o instanceof String pattern && !StringUtil.equals(pattern, filterPattern)) {
    //             this.treeView.setFilterPattern(pattern);
    //             RedisEventUtil.keyFiltered(this.dbIndex());
    //         }
    //     });
    //     popup.showPopup(glyph);
    // }

    // public Integer dbIndex() {
    //     return this.treeView.getDbIndex();
    // }
}
