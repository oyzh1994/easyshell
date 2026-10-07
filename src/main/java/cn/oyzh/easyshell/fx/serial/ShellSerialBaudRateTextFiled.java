package cn.oyzh.easyshell.fx.serial;

import cn.oyzh.fx.gui.text.field.SelectTextFiled;

/**
 * 串口波特率输入框
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialBaudRateTextFiled extends SelectTextFiled<String> {

    {
        super.addItem("9600");
        super.addItem("19200");
        super.addItem("38400");
        super.addItem("57600");
        super.addItem("115200");
        this.setText("9600");
    }

    /**
     * 获取波特率
     *
     * @return 波特率
     */
    public int getBaudRate() {
        return Integer.parseInt(this.getText());
    }
}
