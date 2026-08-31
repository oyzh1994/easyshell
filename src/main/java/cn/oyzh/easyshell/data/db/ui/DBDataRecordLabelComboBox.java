package cn.oyzh.easyshell.data.db.ui;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * @author oyzh
 * @since 2024/8/27
 */
public class DBDataRecordLabelComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem("(Root)");
        this.addItem("RECORDS");
        super.initNode();
    }

    public boolean isRoot() {
        return this.getSelectedIndex() == 0;
    }
}
