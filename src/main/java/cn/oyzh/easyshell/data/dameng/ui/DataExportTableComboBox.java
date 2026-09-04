package cn.oyzh.easyshell.data.dameng.ui;

import cn.oyzh.easyshell.data.dameng.dto.ShellDamengDataExportTable;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

/**
 * @author oyzh
 * @since 2024/8/27
 */
public class DataExportTableComboBox extends FXComboBox<ShellDamengDataExportTable> {

    @Override
   public void initNode(){
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(ShellDamengDataExportTable object) {
                if (object != null) {
                    return object.getName();
                }
                return null;
            }
        });
        super.initNode();
    }
}
