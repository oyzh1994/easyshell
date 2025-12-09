package cn.oyzh.easyshell.tabs.main;

import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeView;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.svg.pane.SortSVGPane;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.List;


/**
 * shell连接业务
 *
 * @author oyzh
 * @since 2025/04/23
 */
public class ShellMainTabController extends RichTabController {

    /**
     * 左侧ssh树
     */
    @FXML
    private ShellConnectTreeView tree;

    /**
     * 节点排序组件
     */
    @FXML
    private SortSVGPane sortPane;

    /**
     * 连接过滤
     */
    @FXML
    private ClearableTextField filter;

    /**
     * 打开终端
     */
    @FXML
    private void openTerminal() {
        ShellEventUtil.showTerminal();
    }

    /**
     * 定位节点
     */
    @FXML
    private void positionNode() {
        this.tree.scrollTo(this.tree.getSelectedItem());
    }

    /**
     * 终端分屏
     */
    @FXML
    private void splitView() {
        ShellViewFactory.splitGuid();
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // ssh树变化事件
        this.tree.selectItemChanged(ShellEventUtil::treeItemChanged);
        // 刷新触发事件
        KeyListener.listenReleased(this.tree, KeyCode.F5, keyEvent -> this.tree.reload());
        // 监听过滤
        this.filter.addTextChangeListener((observableValue, s, t1) -> {
            this.tree.setHighlightText(t1);
            this.tree.filter();
        });
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        // // 窗口就绪
        // TabPaneUtil.onWindowReady(tab, window -> {
        //     // 文件拖拽初始化
        //     StageAdapter adapter = StageManager.getAdapter(window);
        //     if (adapter != null) {
        //         adapter.initDragFile(this.tree.getDragContent(), this::dragFile);
        //     }
        // });
    }

    /**
     * 拖拽文件
     *
     * @param files 文件列表
     */
    private void dragFile(List<File> files) {
        ShellEventUtil.fileDragged(files);
    }

    @FXML
    private void addConnect() {
        ShellViewFactory.addConnectGuid(null);
    }

    @FXML
    private void addGroup() {
        this.tree.addGroup(null);
    }

    @FXML
    private void sortTree() {
        if (this.sortPane.isAsc()) {
            this.tree.sortAsc();
            this.sortPane.desc();
        } else {
            this.tree.sortDesc();
            this.sortPane.asc();
        }

    }

    @FXML
    private void dataImport() {
        ShellViewFactory.dataImport(null);
    }

    @FXML
    private void dataExport() {
        ShellViewFactory.dataExport();
    }

    /**
     * 更新日志
     */
    @FXML
    private void changelog() {
        ShellEventUtil.changelog();
    }
}
