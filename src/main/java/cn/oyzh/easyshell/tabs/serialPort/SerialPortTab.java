package cn.oyzh.easyshell.tabs.serialPort;

import cn.oyzh.easyshell.event.serialPort.SerialPortSetting;
import cn.oyzh.fx.gui.svg.glyph.key.KeySVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * @author Iammm
 * 2025/4/7 19:20
 */
public class SerialPortTab extends RichTab {
    public SerialPortTab(Object data) {
        super();
        super.flush();
        this.controller().root.select(this.controller().settings);
    }

    public void initSerialPort(Object serialPortSetting) {
        try {
            this.controller().init((SerialPortSetting) serialPortSetting);
            setOnClosed(_ -> this.controller().closePort());
            this.controller().root.select(this.controller().serialPort);
            this.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public SerialPortTabController controller() {
        return (SerialPortTabController) super.controller();
    }

    @Override
    protected String url() {
        return "/tabs/serialPort/serialPortTab.fxml";
    }

    @Override
    public void flushGraphic() {
        KeySVGGlyph glyph = (KeySVGGlyph) this.getGraphic();
        if (glyph == null) {
            glyph = new KeySVGGlyph("12");
            glyph.setCursor(Cursor.DEFAULT);
            this.graphic(glyph);
        }
    }

    @Override
    public String getTabTitle() {
        return "串口通讯";
    }

    private void c(Event event) {
        this.controller().closePort();
    }
}
