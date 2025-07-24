package cn.oyzh.easyshell.fx.serial;

import cn.oyzh.fx.gui.text.field.SelectTextFiled;

/**
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

    public int getBaudRate() {
        return Integer.parseInt(this.getText());
    }
}
