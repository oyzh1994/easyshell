package cn.oyzh.easyshell.domain.redis;

import cn.oyzh.common.object.ObjectComparator;
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
 * redis过滤配置
 *
 * @author oyzh
 * @since 2023/06/20
 */
@Table("t_filter")
public class RedisFilter implements ObjectComparator<RedisFilter>, Serializable {

    /**
     * id
     */
    @Column
    @PrimaryKey
    private String uid;

    /**
     * redis连接id
     *
     * @see ShellConnect
     */
    @Column
    private String iid;

    /**
     * 关键词
     */
    @Column
    private String kw;

    /**
     * 模糊匹配
     * true 模糊匹配
     * false 完全匹配
     */
    @Column
    private boolean partMatch;

    /**
     * 是否启用
     */
    @Column
    private boolean enable;

    /**
     * 复制对象
     *
     * @param filter 过滤信息
     * @return 当前对象
     */
    public RedisFilter copy(RedisFilter filter) {
        this.kw = filter.kw;
        this.iid = filter.iid;
        this.enable = filter.enable;
        this.partMatch = filter.partMatch;
        return this;
    }

    @Override
    public boolean compare(RedisFilter filter) {
        if (Objects.equals(this, filter)) {
            return true;
        }
        return Objects.equals(filter.kw, this.kw);
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

    public boolean isPartMatch() {
        return partMatch;
    }

    public void setPartMatch(boolean partMatch) {
        this.partMatch = partMatch;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
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

    public static List<RedisFilter> clone(List<RedisFilter> filters) {
        if (CollectionUtil.isEmpty(filters)) {
            return Collections.emptyList();
        }
        List<RedisFilter> list = new ArrayList<>();
        for (RedisFilter filter : filters) {
            RedisFilter redisFilter = new RedisFilter();
            redisFilter.copy(filter);
            list.add(redisFilter);
        }
        return list;
    }
}
