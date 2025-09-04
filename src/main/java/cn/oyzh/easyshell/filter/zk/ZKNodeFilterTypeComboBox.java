package cn.oyzh.easyshell.filter.zk;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.Locale;

/**
 * zk节点过滤类型选择框
 *
 * @author oyzh
 * @since 2025/01/22
 */
public class ZKNodeFilterTypeComboBox extends FXComboBox<String> implements I18nSelectAdapter<String> {

    @Override
    public List<String> values(Locale locale) {
        this.clearItems();
        this.addItem(I18nHelper.allNodes());
        this.addItem(I18nHelper.collectNodes());
        this.addItem(I18nHelper.persistentNodes());
        this.addItem(I18nHelper.temporaryNodes());
        return this.getItems();
    }

    @Override
    public void initNode() {
        super.initNode();
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (MouseUtil.isPrimaryButton(event) && MouseUtil.isSingleClick(event)) {
                this.show();
            } else {
                this.hide();
            }
        });
    }
}
