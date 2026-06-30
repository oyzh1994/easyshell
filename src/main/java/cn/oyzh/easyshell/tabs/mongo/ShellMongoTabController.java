package cn.oyzh.easyshell.tabs.mongo;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeView;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoViewFactory;
import cn.oyzh.fx.gui.text.field.FilterTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyHandler;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.node.NodeWidthResizer;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoTabController extends ShellBaseTabController {

    /**
     * 客户端
     */
    private ShellMongoClient client;

    /**
     * 根节点
     */
    @FXML
    private FXHBox root;

    /**
     * 左侧节点
     */
    @FXML
    private FXVBox leftBox;

    /**
     * 根节点
     */
    @FXML
    private ShellMongoTabPane tabPane;

    /**
     * mongodb树
     */
    @FXML
    private ShellMongoTreeView treeView;

    /**
     * 过滤参数
     */
    @FXML
    private FilterTextField filterKW;

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.client = ShellClientUtil.newClient(connect);
        // 加载根节点
        StageManager.showMask(() -> {
            try {
                this.client.start();
                if (!this.client.isConnected()) {
                    this.client.close();
                    MessageBox.warn(I18nHelper.connectFail());
                    this.closeTab();
                    return;
                }
                this.tabPane.setClient(this.client);
                this.treeView.setClient(this.client);
                this.treeView.root().loadChild();
                this.treeView.root().expend();
                this.hideLeft();
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    public ShellMongoClient getClient() {
        return client;
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        IOUtil.close(this.client);
    }

    /**
     * 执行过滤
     */
    private void doFilter() {
        String kw = this.filterKW.getTextTrim();
        // 匹配大小写
        boolean matchCase = this.filterKW.isMatchCase();
        // 全字模式
        boolean wholeWord = this.filterKW.isWholeWord();
        // 设置高亮是否匹配大小写
        this.treeView.setHighlightMatchCase(matchCase);
        this.treeView.setHighlight(kw);
        this.treeView.getItemFilter().setKw(kw);
        this.treeView.getItemFilter().setMatchCase(matchCase);
        this.treeView.getItemFilter().setWholeWord(wholeWord);
        ThreadUtil.start(() -> this.treeView.filter());
    }

    /**
     * 导入数据
     */
    @FXML
    private void importData() {
        ShellMongoViewFactory.importData(this.client, null);
    }

    /**
     * 导出数据
     */
    @FXML
    private void exportData() {
        ShellMongoViewFactory.exportData(this.client, null, null);
    }

    /**
     * 运行脚本文件
     */
    @FXML
    private void runScriptFile() {
        ShellMongoViewFactory.runScriptFile(this.client, null);
    }

    /**
     * 定位节点
     */
    @FXML
    private void positionNode() {
        this.treeView.positionItem();
    }

    /**
     * 传输数据
     */
    @FXML
    private void transportData() {
        ShellMongoViewFactory.transportData(this.client.getShellConnect(), null);
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
    }

    private NodeWidthResizer widthResizer;

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 过滤
        KeyHandler searchKeyHandler = new KeyHandler();
        searchKeyHandler.setHandler(e -> this.filterKW.requestFocus());
        searchKeyHandler.setKeyCode(KeyCode.F);
        searchKeyHandler.setMainModifierDown(true);
        searchKeyHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.root, searchKeyHandler);
        // 拉伸辅助
        this.widthResizer = NodeWidthResizer.of(this.leftBox, this::resizeLeft, 240, 750);
        // 内容过滤
        this.filterKW.textProperty().addListener((observable, oldValue, newValue) -> {
            this.doFilter();
        });
        this.filterKW.wholeWordPropery().addListener((observable, oldValue, newValue) -> {
            this.doFilter();
        });
        this.filterKW.matchCasePropery().addListener((observable, oldValue, newValue) -> {
            this.doFilter();
        });
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
            this.tabPane.setFlexWidth("100% - " + newWidth);
        }
    }

    @Override
    public void destroy() {
        this.tabPane.destroy();
        this.treeView.destroy();
        this.widthResizer.destroy();
        super.destroy();
    }
}
