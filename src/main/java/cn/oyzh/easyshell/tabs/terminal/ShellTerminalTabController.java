package cn.oyzh.easyshell.tabs.terminal;

import cn.oyzh.easyshell.terminal.DefaultTermWidget;
import cn.oyzh.easyshell.terminal.DefaultTtyConnector;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter;
import javafx.fxml.FXML;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * shell终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/20
 */
public class ShellTerminalTabController extends RichTabController {

    /**
     * 软件信息
     */
    @FXML
    private FXVBox root;

    private void initWidget() throws IOException {
        DefaultTermWidget widget = new DefaultTermWidget();
        widget.onTermination(exitCode -> widget.close());
        widget.addHyperlinkFilter(new DefaultHyperlinkFilter());
        DefaultTtyConnector connector = (DefaultTtyConnector) widget.createTtyConnector();
        widget.openSession(connector);
        this.root.setChild(widget.getPane());
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

}
