package cn.oyzh.easyshell.controller.main;

import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeView;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.svg.pane.SortSVGPane;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.List;


/**
 * shell连接业务
 *
 * @author oyzh
 * @since 2025/04/23
 */
public class ConnectController extends SubStageController {

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
     * 定位节点
     */
    @FXML
    private void positionNode() {
        this.tree.scrollTo(this.tree.getSelectedItem());
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        // 取消F5按键监听
        KeyListener.unListenReleased(this.tree, KeyCode.F5);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // ssh树变化事件
        this.tree.selectItemChanged(ShellEventUtil::treeItemChanged);
        // 文件拖拽初始化
        this.stage.initDragFile(this.tree.getDragContent(), this::dragFile);
        // 刷新触发事件
        KeyListener.listenReleased(this.tree, KeyCode.F5, keyEvent -> this.tree.reload());
    }

    /**
     * 拖拽文件
     *
     * @param files 文件列表
     */
    private void dragFile(List<File> files) {
        ShellEventUtil.fileDragged(files);
    }

    /**
     * 对树进行排序
     */
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

    /**
     * 数据导入
     */
    @FXML
    private void dataImport() {
        ShellViewFactory.dataImport(null);
    }

    /**
     * 数据导出
     */
    @FXML
    private void dataExport() {
        ShellViewFactory.dataExport();
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        this.filter.addTextChangeListener((observableValue, s, t1) -> {
            this.tree.setHighlight(t1);
            this.tree.filter();
        });
    }
}
