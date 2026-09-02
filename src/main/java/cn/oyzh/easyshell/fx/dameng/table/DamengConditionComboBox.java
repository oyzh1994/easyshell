package cn.oyzh.easyshell.fx.dameng.table;//package cn.oyzh.easyshell.fx.dameng.table;
//
//import cn.oyzh.easydameng.condition.DamengCondition;
//import cn.oyzh.easydameng.condition.DamengConditionUtil;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//import cn.oyzh.fx.plus.converter.SimpleStringConverter;
//
///**
// * @author oyzh
// * @since 2024/06/26
// */
//public class DamengConditionComboBox extends FXComboBox<DamengCondition> {
//
//    {
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(DamengCondition o) {
//                if (o == null) {
//                    return "";
//                }
//                return o.getName();
//            }
//        });
//        this.addItem(DamengConditionUtil.conditions());
//    }
//}
