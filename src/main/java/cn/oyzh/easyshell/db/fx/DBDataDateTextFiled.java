package cn.oyzh.easyshell.db.fx;


import cn.oyzh.fx.gui.text.field.SelectTextFiled;

/**
 * @author oyzh
 * @since 2024/9/2
 */
public class DBDataDateTextFiled extends SelectTextFiled<String> {

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
