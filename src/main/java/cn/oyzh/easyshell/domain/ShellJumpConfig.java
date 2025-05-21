package cn.oyzh.easyshell.domain;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * shell跳板配置
 *
 * @author oyzh
 * @since 2025-03-15
 */
@Table("t_jump_config")
public class ShellJumpConfig extends SSHConnect implements Serializable {

    /**
     * id
     *
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

    public FXToggleSwitch getEnabledStatus() {
        FXToggleSwitch toggleSwitch = new FXToggleSwitch();
        toggleSwitch.setSelected(this.isEnabled());
        toggleSwitch.selectedChanged((observable, oldValue, newValue) -> {
            this.setEnabled(newValue);
        });
        return toggleSwitch;
    }

    public static List<ShellJumpConfig> clone(List<ShellJumpConfig> configs) {
        if (CollectionUtil.isEmpty(configs)) {
            return Collections.emptyList();
        }
        List<ShellJumpConfig> list = new ArrayList<>();
        for (ShellJumpConfig config : configs) {
            ShellJumpConfig jumpConfig = new ShellJumpConfig();
            jumpConfig.copy(config);
            list.add(jumpConfig);
        }
        return list;
    }

}
