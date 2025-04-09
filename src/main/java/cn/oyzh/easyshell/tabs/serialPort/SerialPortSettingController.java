package cn.oyzh.easyshell.tabs.serialPort;

import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.event.serialPort.SerialPortSetting;
import cn.oyzh.easyshell.serialPort.FXPortMonitor;
import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class SerialPortSettingController {
    private final BooleanProperty portOpened = new SimpleBooleanProperty(false);
    @FXML
    public CheckBox checkBoxCustomBaudRate;
    @FXML
    public TextField textFieldCustomBaudRate;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    public ComboBox<Integer> comboBoxFlowControl;
    @FXML
    public ComboBox<Charset> comboBoxInCharset;
    @FXML
    public ComboBox<Charset> comboBoxOutCharset;
    @FXML
    private ComboBox<SerialPort> comboBoxPort;
    @FXML
    private ComboBox<Integer> comboBoxBaudRate;
    @FXML
    private ComboBox<Integer> comboBoxDataBits;
    @FXML
    private ComboBox<Integer> comboBoxStopBits;
    @FXML
    private ComboBox<Integer> comboBoxParity;
    @FXML
    private Button buttonOpen;


    @FXML
    public void handleOpenPort(ActionEvent ignoredActionEvent) {
        portOpened.set(!portOpened.get());
        var comport = comboBoxPort.getValue();
        ShellEventUtil.openSerialPort(new SerialPortSetting(
                comport, checkBoxCustomBaudRate.isSelected() ? Integer.valueOf(textFieldCustomBaudRate.getText()) : comboBoxBaudRate.getValue(), comboBoxDataBits.getValue(), comboBoxStopBits.getValue(), comboBoxParity.getValue(), comboBoxFlowControl.getValue(), comboBoxOutCharset.getValue(), comboBoxInCharset.getValue()
        ));

    }

    /**
     * FXML控制器初始化方法
     * 主要功能：
     * 1. 初始化串口监控组件
     * 2. 配置波特率选择组件
     * 3. 设置自定义波特率输入验证
     * 注意：本方法无参数和返回值
     */
    @FXML
    public void initialize() {
        // 初始化通信参数组件
        initComBoxPort();
        initComBoxBaudRate();
        initDataBits();
        initStopBits();
        initParity();
        initFlowControl();
        initComboBoxCharset();
    }

    private void initComboBoxCharset() {


        // 添加常用字符集选项（按需调整顺序和内容）
        Charset[] charsets = {StandardCharsets.ISO_8859_1,
                StandardCharsets.UTF_8,
                StandardCharsets.UTF_16,
                StandardCharsets.UTF_16BE,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_32,
                StandardCharsets.UTF_32BE,
                StandardCharsets.UTF_32LE,
                StandardCharsets.US_ASCII
        };
        for (Charset charset : charsets) {
            comboBoxOutCharset.getItems().add(charset);
            comboBoxInCharset.getItems().add(charset);
        }
        comboBoxOutCharset.setValue(StandardCharsets.UTF_8);
        comboBoxInCharset.setValue(StandardCharsets.UTF_8);

    }

    private void initComBoxPort() {
        var portMonitor = new FXPortMonitor(comboBoxPort);
        portMonitor.startMonitoring(500);
        // 注册窗口关闭事件处理：停止端口监控
        Platform.runLater(() -> {

        });

    }

    private void initFlowControl() {
        // 添加流控选项
        comboBoxFlowControl.getItems().addAll(
                SerialPort.FLOW_CONTROL_DISABLED,
                SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED,
                SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED,
                SerialPort.FLOW_CONTROL_RTS_ENABLED,
                SerialPort.FLOW_CONTROL_CTS_ENABLED,
                SerialPort.FLOW_CONTROL_DSR_ENABLED,
                SerialPort.FLOW_CONTROL_DTR_ENABLED
        );
        comboBoxFlowControl.setValue(SerialPort.FLOW_CONTROL_DISABLED);

        // 配置显示文本转换（解决原代码误用停止位常量的问题）
        comboBoxFlowControl.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "" : getFlowControlText(item));
            }
        });

        comboBoxFlowControl.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("流控选项");
                } else {
                    setText(getFlowControlText(item));
                }
            }
        });
    }

    /**
     * 功能说明：
     * 1. 创建并启动串口设备监控线程，实时刷新可用串口列表
     * 2. 配置标准波特率下拉框及自定义波特率输入组件
     * 3. 实现窗口关闭时的资源清理逻辑
     * 4. 建立界面组件间的状态绑定和输入验证机制
     */
    private void initComBoxBaudRate() {
        // 初始化端口监控组件，绑定到端口选择下拉框

        // 初始化标准波特率选项（常用值）并设置默认值
        comboBoxBaudRate.getItems().addAll(9600, 19200, 38400, 57600, 115200);
        comboBoxBaudRate.setValue(9600);

        // 绑定自定义波特率组件状态到复选框
        textFieldCustomBaudRate.setDisable(false);
        textFieldCustomBaudRate.disableProperty().bind(checkBoxCustomBaudRate.selectedProperty().not());
        comboBoxBaudRate.disableProperty().bind(checkBoxCustomBaudRate.selectedProperty());

        // 自定义波特率输入验证
        textFieldCustomBaudRate.textProperty().addListener((_, oldValue, newValue) -> {
            if (!isValidBaudrate(newValue)) {
                var a = showInputDialog();
                Platform.runLater(() -> textFieldCustomBaudRate.setText(a == null ? oldValue : a));
            }
        });

    }

    /**
     * 初始化停止位配置
     * 选项包含：1位、1.5位、2位
     */
    private void initStopBits() {
        // 添加停止位选项
        comboBoxStopBits.getItems().addAll(
                SerialPort.ONE_STOP_BIT,
                SerialPort.ONE_POINT_FIVE_STOP_BITS,
                SerialPort.TWO_STOP_BITS
        );
        comboBoxStopBits.setValue(SerialPort.ONE_STOP_BIT);

        // 配置显示文本转换
        comboBoxStopBits.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "" : getStopBitsText(item));
            }
        });

        comboBoxStopBits.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("停止位");
                } else {
                    setText(getStopBitsText(item));
                }
            }
        });
    }

    /**
     * 初始化校验位配置
     * 选项包含：None（无）、Even（偶校验）、Odd（奇校验）
     */
    private void initParity() {
        // 添加校验位选项
        comboBoxParity.getItems().addAll(
                SerialPort.NO_PARITY,
                SerialPort.EVEN_PARITY,
                SerialPort.ODD_PARITY,
                SerialPort.MARK_PARITY,
                SerialPort.SPACE_PARITY
        );
        comboBoxParity.setValue(SerialPort.NO_PARITY);

        // 配置显示文本转换
        comboBoxParity.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "" : getParityText(item));
            }
        });

        comboBoxParity.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("校验位");
                } else {
                    setText(getParityText(item));
                }
            }
        });
    }

    private String getFlowControlText(int item) {
        return switch (item) {
            case SerialPort.FLOW_CONTROL_DISABLED -> "无流控";
            case SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED -> "XON/XOFF 输入";
            case SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED -> "XON/XOFF 输出";
            case SerialPort.FLOW_CONTROL_RTS_ENABLED -> "RTS 流控";
            case SerialPort.FLOW_CONTROL_CTS_ENABLED -> "CTS 流控";
            case SerialPort.FLOW_CONTROL_DSR_ENABLED -> "DSR 流控";
            case SerialPort.FLOW_CONTROL_DTR_ENABLED -> "DTR 流控";
            default -> "未知";
        };
    }

    private String getStopBitsText(int item) {
        return switch (item) {
            case SerialPort.ONE_STOP_BIT -> "1位";
            case SerialPort.ONE_POINT_FIVE_STOP_BITS -> "1.5位";
            case SerialPort.TWO_STOP_BITS -> "2位";
            default -> "未知";
        };
    }

    private String getParityText(int item) {
        return switch (item) {
            case SerialPort.NO_PARITY -> "无校验";
            case SerialPort.ODD_PARITY -> "奇校验";
            case SerialPort.EVEN_PARITY -> "偶校验";
            case SerialPort.MARK_PARITY -> "标记校验（1）";
            case SerialPort.SPACE_PARITY -> "空格校验（0）";
            default -> "未知";
        };
    }

    /**
     * 初始化数据位配置
     * 主要功能：
     * 1. 添加标准数据位选项（5,6,7,8位）
     * 2. 设置默认数据位为8（最常用值）
     */
    private void initDataBits() {
        comboBoxDataBits.getItems().addAll(5, 6, 7, 8);
        comboBoxDataBits.setValue(8);
    }

    // 验证波特率是否为有效数字
    private boolean isValidBaudrate(String value) {
        try {
            int baudrate = Integer.parseInt(value);
            return baudrate > 0; // 波特率必须为正整数
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String showInputDialog() {
        // 创建 TextInputDialog 对象
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Invalid Input");               // 设置标题
        dialog.setHeaderText("Invalid Baudrate");         // 设置头部文本
        dialog.setContentText("Please enter a valid baudrate.");       // 设置内容提示

        // 显示对话框并等待用户输入
        return dialog.showAndWait()           // 返回 Optional<String>
                .orElse(null);               // 如果用户取消，则返回 null
    }

    @FXML
    public void checkBoxCustomBaudrate(ActionEvent ignoredActionEvent) {

    }

    @FXML
    public void customBaudrateInput(ActionEvent ignoredActionEvent) {

    }
}
