package cn.oyzh.easyshell.fx.key;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;

import java.util.List;

/**
 * shell密钥选择框
 *
 * @author oyzh
 * @since 25/03/09
 */
public class ShellKeyComboBox extends FXComboBox<ShellKey> {

    {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(ShellKey o) {
                if (o == null) {
                    return "";
                }
                return o.getName();
            }
        });
        List<ShellKey> keys = ShellKeyStore.INSTANCE.selectList();
        this.setItem(keys);
    }

    public String getKeyId() {
        ShellKey key = this.getSelectedItem();
        if (key == null) {
            return null;
        }
        return key.getId();
    }

    public void selectById(String certificate) {
        for (ShellKey item : this.getItems()) {
            if (StringUtil.equals(certificate, item.getId())) {
                this.select(item);
                break;
            }
        }
    }
}
