package cn.oyzh.easyshell.fx.connect;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 连接输入框，可搜索
 *
 * @author oyzh
 * @since 2025-06-06
 */
public class ShellConnectTextField extends SelectTextFiled<ShellConnect> {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
        this.setTipText(I18nHelper.pleaseSelectConnect());
    }

    /**
     * 连接列表
     */
    private List<ShellConnect> connects;

    // @Override
    // public ShellConnectTextFieldSkin skin() {
    //     return (ShellConnectTextFieldSkin) super.skin();
    // }
    //
    // @Override
    // protected ShellConnectTextFieldSkin createDefaultSkin() {
    //     if (this.getSkin() != null) {
    //         return (ShellConnectTextFieldSkin) this.getSkin();
    //     }
    //     return new ShellConnectTextFieldSkin(this);
    // }

    @Override
    protected void onTextChanged(String newValue) {
        if (!this.isFocused()) {
            return;
        }
        if (this.skin().isTexting()) {
            this.skin().clearTexting();
            return;
        }
        // 移除选区
        this.clearSelection();
        // 隐藏弹窗
        if (StringUtil.isBlank(newValue)) {
            this.setItemList(this.connects);
            this.skin().hidePopup();
            return;
        }
        // 过滤内容
        List<ShellConnect> newList = this.connects.stream()
                .filter(t -> StringUtil.containsIgnoreCase(t.getName(), newValue))
                .collect(Collectors.toList());
        // 设置内容
        this.setItemList(newList);
        // 内容为空，隐藏弹窗
        if (newList.isEmpty()) {
            this.skin().hidePopup();
        } else {
            this.skin().showPopup();
        }
    }

    /**
     * 移除内容
     *
     * @param connect 连接
     */
    public void removeItem(ShellConnect connect) {
        this.connects.removeIf(c -> StringUtil.equals(c.getId(), connect.getId()));
        this.skin().setItemList(this.connects);
    }

    /**
     * 过滤模式
     * ssh ssh连接
     * file ssh、sftp、ftp连接
     * term ssh、local、串口、telnet、rlogin连接
     * zk zk连接
     * mysql mysql连接
     * redis redis连接
     * all 全部连接
     */
    private String filterMode = "ssh";

    public String getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(String filterMode) {
        this.filterMode = filterMode;
        if (this.connects != null) {
            this.loadConnects();
        }
    }

    /**
     * 加载连接
     */
    protected void loadConnects() {
        ShellConnectStore store = ShellConnectStore.INSTANCE;
        if (ShellPrototype.SSH.equalsIgnoreCase(this.filterMode)) {
            this.connects = store.loadSSHType();
        } else if ("file".equalsIgnoreCase(this.filterMode)) {
            this.connects = store.loadFileType();
        } else if ("term".equalsIgnoreCase(this.filterMode)) {
            this.connects = store.loadTermType();
        } else if (ShellPrototype.REDIS.equalsIgnoreCase(this.filterMode)) {
            this.connects = store.loadRedisType();
        } else if (ShellPrototype.ZOOKEEPER.equalsIgnoreCase(this.filterMode)) {
            this.connects = store.loadZKType();
        } else if (ShellPrototype.MYSQL.equalsIgnoreCase(this.filterMode)) {
            this.connects = store.loadMysqlType();
        } else {
            this.connects = store.load();
        }
        this.setItemList(this.connects);
    }

    @Override
    public void initNode() {
        super.initNode();
        this.loadConnects();
        // this.setItemList(this.connects);
        this.skin().setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(ShellConnect o) {
                if (o == null) {
                    return "";
                }
                return o.getName();
            }
        });
    }
}
