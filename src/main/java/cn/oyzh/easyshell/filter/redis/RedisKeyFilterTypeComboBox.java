package cn.oyzh.easyshell.filter.redis;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.mouse.MouseUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.Locale;

/**
 * @author oyzh
 * @since 2024/4/19
 */
public class RedisKeyFilterTypeComboBox extends FXComboBox<String> implements I18nSelectAdapter<String> {

    {
        NodeManager.init(this);
    }

    @Override
    public List<String> values(Locale locale) {
        this.clearItems();
        this.addItem(I18nHelper.allKeys());
        this.addItem(I18nHelper.collectKeys());
        this.addItem("STRING");
        this.addItem("LIST");
        this.addItem("SET");
        this.addItem("ZSET");
        this.addItem("HASH");
        this.addItem("STREAM");
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
