package cn.oyzh.easyshell.domain.zk;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.toggle.EnabledToggleSwitch;
import cn.oyzh.fx.gui.toggle.MatchToggleSwitch;
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
 * zk过滤配置
 *
 * @author oyzh
 * @since 2022/12/20
 */
@Table("t_zk_filter")
public class ZKFilter implements ObjectComparator<ZKFilter>, ObjectCopier<ZKFilter>, Serializable {

    /**
     * id
     */
    @Column
    @PrimaryKey
    private String uid;

    /**
     * iid
     *
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    @Column
    private String iid;

    /**
     * 关键词
     */
    @Column
    private String kw;

    /**
     * 是否启用
     */
    @Column
    private boolean enable;

    /**
     * 模糊匹配
     * true 模糊匹配
     * false 完全匹配
     */
    @Column
    private boolean partMatch;

    @Override
    public void copy(ZKFilter filter) {
        this.kw = filter.kw;
        this.enable = filter.enable;
        this.partMatch = filter.partMatch;
    }

    @Override
    public boolean compare(ZKFilter filter) {
        if (this.equals(filter)) {
            return true;
        }
        return Objects.equals(filter.kw, this.kw);
    }

    /**
     * 比较信息
     *
     * @param kw 关键字
     * @return 结果
     */
    public boolean compare(String kw) {
        return Objects.equals(kw, this.kw);
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

    public String getKw() {
        return kw;
    }

    public void setKw(String kw) {
        this.kw = kw;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isPartMatch() {
        return partMatch;
    }

    public void setPartMatch(boolean partMatch) {
        this.partMatch = partMatch;
    }

    /**
     * 关键字控件
     */
    @JSONField(serialize = false, deserialize = false)
    public ClearableTextField getKwControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setFlexWidth("100% - 12");
        textField.setValue(this.getKw());
        textField.addTextChangeListener((obs, o, n) -> this.setKw(n));
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 匹配模式控件
     */
    @JSONField(serialize = false, deserialize = false)
    public MatchToggleSwitch getMatchModeControl() {
        MatchToggleSwitch toggleSwitch = new MatchToggleSwitch();
        toggleSwitch.setFontSize(11);
        toggleSwitch.setSelected(this.isPartMatch());
        toggleSwitch.selectedChanged((obs, o, n) -> this.setPartMatch(n));
        TableViewUtil.selectRowOnMouseClicked(toggleSwitch);
        return toggleSwitch;
    }

    /**
     * 状态控件
     */
    @JSONField(serialize = false, deserialize = false)
    public EnabledToggleSwitch getStatusControl() {
        EnabledToggleSwitch toggleSwitch = new EnabledToggleSwitch();
        toggleSwitch.setFontSize(11);
        toggleSwitch.setSelected(this.isEnable());
        toggleSwitch.selectedChanged((abs, o, n) -> this.setEnable(n));
        TableViewUtil.selectRowOnMouseClicked(toggleSwitch);
        return toggleSwitch;
    }

    public static List<ZKFilter> clone(List<ZKFilter> filters) {
        if (CollectionUtil.isEmpty(filters)) {
            return Collections.emptyList();
        }
        List<ZKFilter> list = new ArrayList<>();
        for (ZKFilter filter : filters) {
            ZKFilter zkFilter = new ZKFilter();
            zkFilter.copy(filter);
            list.add(zkFilter);
        }
        return list;
    }
}
