package cn.oyzh.easyshell.fx.serial;

import cn.oyzh.fx.gui.text.field.SelectTextFiled;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialBaudRateTextFiled extends SelectTextFiled {

    {
        super.addData("9600");
        super.addData("19200");
        super.addData("38400");
        super.addData("57600");
        super.addData("115200");
        this.setText("9600");
    }

    public int getBaudRate() {
        return Integer.parseInt(this.getText());
    }
}
