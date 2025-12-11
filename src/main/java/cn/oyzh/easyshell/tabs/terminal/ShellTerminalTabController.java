package cn.oyzh.easyshell.tabs.terminal;

import cn.oyzh.easyshell.tabs.ShellSnippetAdapter;
import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * shell终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/20
 */
public class ShellTerminalTabController extends RichTabController  implements ShellSnippetAdapter{

    /**
     * 根节点
     */
    @FXML
    private ShellDefaultTermWidget widget;

    /**
     * 终端大小
     */
    @FXML
    private FXText termSize;

    private void initWidget() throws IOException {
        ShellDefaultTtyConnector connector = (ShellDefaultTtyConnector) this.widget.createTtyConnector();
        // 监听窗口大小
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.termSize.text(newValue.getRows() + "x" + newValue.getColumns());
            }
        });
        // this.widget.setAlwaysShowThumbs(true);
        this.widget.openSession(connector);
        // this.widget.onTermination(exitCode -> this.widget.close());
        // // 初始化部分参数
        // connector.write("export LANG=en_US.utf-8\n");
        // connector.write("export TERM=xterm-256color\n");
    }

    @Override
    public void initialize(URL url, ResourceBundle resource) {
        super.initialize(url, resource);
        try {
            this.initWidget();
        } catch (IOException ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.widget.close();
    }

    /**
     * 片段列表
     *
     * @param event 事件
     */
    @FXML
    private void snippet(MouseEvent event) {
        ShellSnippetAdapter.super.snippetList((Node) event.getSource());
    }

    @Override
    public void runSnippet(String content) throws IOException {
        this.widget.getTtyConnector().write(content);
    }
}
