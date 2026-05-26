package cn.oyzh.easyshell.fx.zk;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.store.zk.ShellZKAuthStore;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/4/23
 */
public class ShellZKAuthComboBox extends FXComboBox<ShellZKAuth> {

    {
        NodeManager.init(this);
    }

    /**
     * 初始化
     *
     * @param iid 连接id
     */
    public void init(String iid) {
        List<ShellZKAuth> authList = ShellZKAuthStore.INSTANCE.loadByIid(iid);
        if (CollectionUtil.isNotEmpty(authList)) {
            this.setItem(authList);
            this.selectFirst();
        }
    }

    @Override
    public void initNode() {
        this.setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(ShellZKAuth auth) {
                String text = "";
                if (auth != null) {
                    text = I18nHelper.userName() + ":" + auth.getUser() +
                            " " + I18nHelper.password() + ":" + auth.getPassword();
                }
                return text;
            }
        });
        super.initNode();
    }
}
