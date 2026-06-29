package cn.oyzh.easyshell.fx.mongo;

import cn.oyzh.easyshell.mongo.condition.MongoCondition;
import cn.oyzh.easyshell.mongo.condition.MongoConditionUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

/**
 * @author oyzh
 * @since 2024/06/26
 */
public class MongoConditionComboBox extends FXComboBox<MongoCondition> {

    @Override
    public void initNode() {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(MongoCondition o) {
                if (o == null) {
                    return "";
                }
                return o.getName();
            }
        });
        this.addItem(MongoConditionUtil.conditions());
    }
}
