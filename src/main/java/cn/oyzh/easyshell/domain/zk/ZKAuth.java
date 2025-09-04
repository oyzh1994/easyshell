package cn.oyzh.easyshell.domain.zk;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.zk.ZKAuthUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.toggle.EnabledToggleSwitch;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * zk认证信息
 *
 * @author oyzh
 * @since 2022/6/9
 */
@Table("t_zk_auth")
public class ZKAuth implements ObjectComparator<ZKAuth>, ObjectCopier<ZKAuth>, Serializable {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String uid;

    /**
     * iid
     *
     * @see ShellConnect
     */
    @Column
    private String iid;

    /**
     * 用户名
     */
    @Column
    private String user;

    /**
     * 密码
     */
    @Column
    private String password;

    /**
     * 是否启用
     */
    @Column
    private Boolean enable;

    public ZKAuth() {
    }

    public ZKAuth(String iid, String user, String password) {
        this.iid = iid;
        this.user = user;
        this.password = password;
    }

    /**
     * 生成摘要
     *
     * @return 摘要信息
     */
    public String digest() {
        if (StringUtil.isBlank(this.getUser()) || StringUtil.isBlank(this.getPassword())) {
            return "";
        }
        return ZKAuthUtil.digest(this.getUser(), this.getPassword());
    }

    @Override
    public boolean compare(ZKAuth auth) {
        if (auth == null) {
            return false;
        }
        if (Objects.equals(this, auth)) {
            return true;
        }
        if (!Objects.equals(auth.user, this.user)) {
            return false;
        }
        return Objects.equals(auth.password, this.password);
    }

    @Override
    public void copy(ZKAuth auth) {
        this.iid = auth.iid;
        this.user = auth.user;
        this.enable = auth.enable;
        this.password = auth.password;
    }

    public boolean getEnable() {
        return this.isEnable();
    }

    public boolean isEnable() {
        return this.enable == null || this.enable;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    /**
     * 用户名控件
     */
    @JSONField(serialize = false, deserialize = false)
    public ClearableTextField getUserControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setFlexWidth("100% - 12");
        textField.setValue(this.getUser());
        textField.addTextChangeListener((obs, o, n) -> this.setUser(n));
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 密码控件
     */
    @JSONField(serialize = false, deserialize = false)
    public ClearableTextField getPasswordControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setFlexWidth("100% - 12");
        textField.setValue(this.getPassword());
        textField.addTextChangeListener((obs, o, n) -> this.setPassword(n));
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 状态控件
     */
    @JSONField(serialize = false, deserialize = false)
    public FXToggleSwitch getStatusControl() {
        EnabledToggleSwitch toggleSwitch = new EnabledToggleSwitch();
        toggleSwitch.setFontSize(11);
        toggleSwitch.setSelected(this.getEnable());
        toggleSwitch.selectedChanged((abs, o, n) -> {
            this.setEnable(n);
        });
        return toggleSwitch;
    }

    public static List<ZKAuth> clone(List<ZKAuth> auths) {
        if (CollectionUtil.isEmpty(auths)) {
            return Collections.emptyList();
        }
        List<ZKAuth> list = new ArrayList<>();
        for (ZKAuth auth : auths) {
            ZKAuth zkAuth = new ZKAuth();
            zkAuth.copy(auth);
            list.add(zkAuth);
        }
        return list;
    }
}
