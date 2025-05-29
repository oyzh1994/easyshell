package cn.oyzh.easyshell.domain;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.ssh.domain.SSHTunneling;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * shell隧道配置
 *
 * @author oyzh
 * @since 2025-04-16
 */
@Table("t_tunneling_config")
public class ShellTunnelingConfig extends SSHTunneling implements Serializable {

    /**
     * id
     */
    @Column
    @PrimaryKey
    private String id;

    /**
     * 连接id
     *
     * @see ShellConnect
     */
    @Column
    private String iid;

    /**
     * 是否启用
     */
    @Column
    private Boolean enabled;

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled == null || this.enabled;
    }

    @JSONField(serialize = false, deserialize = false)
    public FXToggleSwitch getEnabledStatus() {
        FXToggleSwitch toggleSwitch = new FXToggleSwitch();
        toggleSwitch.setSelected(this.isEnabled());
        toggleSwitch.selectedChanged((observable, oldValue, newValue) -> {
            this.setEnabled(newValue);
        });
        return toggleSwitch;
    }

    @JSONField(serialize = false, deserialize = false)
    public String getTypeName() {
        if (this.isLocalType()) {
            return I18nHelper.local();
        }
        if (this.isRemoteType()) {
            return I18nHelper.remote();
        }
        return I18nHelper.dynamic();
    }

    public static List<ShellTunnelingConfig> clone(List<ShellTunnelingConfig> configs) {
        if (CollectionUtil.isEmpty(configs)) {
            return Collections.emptyList();
        }
        List<ShellTunnelingConfig> list = new ArrayList<>();
        for (ShellTunnelingConfig config : configs) {
            ShellTunnelingConfig tunnelingConfig = new ShellTunnelingConfig();
            tunnelingConfig.copy(config);
            list.add(tunnelingConfig);
        }
        return list;
    }
}
