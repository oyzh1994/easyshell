package cn.oyzh.easyshell.tabs.serialPort;

import cn.oyzh.easyshell.event.serialPort.SerialPortSetting;
import cn.oyzh.easyshell.serialPort.SerialPortService;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.function.Function;

/**
 * @author Iammm
 * 2025/4/7 19:28
 */
public class SerialPortTabController extends ParentTabController {
    private final SerialPortService serialPortService = SerialPortService.getInstance();
    @FXML
    public FXTextArea textAreaInput;
    @FXML
    public FXTextArea textAreaOutPut;
    @FXML
    public Tab settings;
    @FXML
    public FXTabPane root;
    @FXML
    public Tab serialPort;
    private SerialPortSetting serialPortSetting;
    private Function<byte[], Boolean> input;

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
    }

    public void init(SerialPortSetting serialPortSetting) {
        if (this.input != null) {
            serialPortService.closePort(this.serialPortSetting);
        }
        this.serialPortSetting = serialPortSetting;

        this.input = serialPortService.openPort(this.serialPortSetting, this::flush);
    }

    public Void flush(byte[] bytes) {
        Platform.runLater(() -> {
            String receivedData = new String(bytes, serialPortSetting.inCharset());
            textAreaOutPut.appendText(receivedData);
        });
        return null;
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            if (input != null) {
                input.apply(textAreaInput.getText().getBytes(serialPortSetting.outCharset()));
            }
            Platform.runLater(textAreaInput::clear);
        }
    }

    public void closePort() {
        serialPortService.closePort(this.serialPortSetting);
    }
}
