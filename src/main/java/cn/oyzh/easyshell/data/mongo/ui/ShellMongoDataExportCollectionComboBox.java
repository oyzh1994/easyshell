package cn.oyzh.easyshell.data.mongo.ui;

import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataExportCollection;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

/**
 * @author oyzh
 * @since 2024/8/27
 */
public class ShellMongoDataExportCollectionComboBox extends FXComboBox<ShellMongoDataExportCollection> {

    @Override
    public void initNode(){
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(ShellMongoDataExportCollection object) {
                if (object != null) {
                    return object.getName();
                }
                return null;
            }
        });
        super.initNode();
    }
}
