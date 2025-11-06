package cn.oyzh.easyshell.fx.mysql.data;


import cn.oyzh.fx.gui.text.field.SelectTextFiled;

/**
 * @author oyzh
 * @since 2024/9/2
 */
public class DataDateTextFiled extends SelectTextFiled<String> {

    {
        this.addItem("yyyy-MM-dd HH:mm:ss");
        this.addItem("yyyy/MM/dd HH:mm:ss");

        this.addItem("yyyy-M-d HH:mm:ss");
        this.addItem("yyyy/M/d HH:mm:ss");

        this.addItem("dd-MM-yyyy HH:mm:ss");
        this.addItem("dd/MM/yyyy HH:mm:ss");

        this.addItem("MM-dd-yyyy HH:mm:ss");
        this.addItem("MM/dd/yyyy HH:mm:ss");

        this.addItem("M-d-yyyy HH:mm:ss");
        this.addItem("M/d/yyyy HH:mm:ss");

        this.addItem("d-M-yyyy HH:mm:ss");
        this.addItem("d/M/yyyy HH:mm:ss");
    }
}
