package cn.oyzh.easyshell.dto.redis;//package cn.oyzh.easyshell.dto.redis;
//
//import cn.oyzh.easyshell.domain.redis.RedisFilter;
//import cn.oyzh.fx.gui.text.field.ClearableTextField;
//import cn.oyzh.fx.gui.toggle.EnabledToggleSwitch;
//import cn.oyzh.fx.gui.toggle.MatchToggleSwitch;
//import cn.oyzh.fx.plus.tableview.TableViewUtil;
//import com.alibaba.fastjson2.annotation.JSONField;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * redis过滤vo信息
// *
// * @author oyzh
// * @since 2023/06/30
// */
//public class RedisFilterVO extends RedisFilter {
//
//    /**
//     * 转换
//     *
//     * @param filter redis过滤信息
//     * @return redis过滤vo
//     */
//    public static RedisFilterVO convert(RedisFilter filter) {
//        RedisFilterVO authVO = new RedisFilterVO();
//        authVO.copy(filter);
//        return authVO;
//    }
//
//    /**
//     * 转换
//     *
//     * @param list redis过滤列表
//     * @return redis过滤vo列表
//     */
//    public static List<RedisFilterVO> convert( List<RedisFilter> list) {
//        List<RedisFilterVO> voList = new ArrayList<>(list.size());
//        for (RedisFilter filter : list) {
//            voList.add(convert(filter));
//        }
//        return voList;
//    }
//
//    /**
//     * 关键字控件
//     */
//    @JSONField(serialize = false, deserialize = false)
//    public ClearableTextField getKwControl() {
//        ClearableTextField textField = new ClearableTextField();
//        textField.setFlexWidth("100% - 12");
//        textField.setValue(this.getKw());
//        textField.addTextChangeListener((obs, o, n) -> this.setKw(n));
//        TableViewUtil.selectRowOnMouseClicked(textField);
//        return textField;
//    }
//
//    /**
//     * 匹配模式控件
//     */
//    @JSONField(serialize = false, deserialize = false)
//    public MatchToggleSwitch getMatchModeControl() {
//        MatchToggleSwitch toggleSwitch = new MatchToggleSwitch();
//        toggleSwitch.fontSize(11);
//        toggleSwitch.setSelected(this.isPartMatch());
//        toggleSwitch.selectedChanged((obs, o, n) -> this.setPartMatch(n));
//        TableViewUtil.selectRowOnMouseClicked(toggleSwitch);
//        return toggleSwitch;
//    }
//
//    /**
//     * 状态控件
//     */
//    @JSONField(serialize = false, deserialize = false)
//    public EnabledToggleSwitch getStatusControl() {
//        EnabledToggleSwitch toggleSwitch = new EnabledToggleSwitch();
//        toggleSwitch.setFontSize(11);
//        toggleSwitch.setSelected(this.isEnable());
//        toggleSwitch.selectedChanged((abs, o, n) -> this.setEnable(n));
//        TableViewUtil.selectRowOnMouseClicked(toggleSwitch);
//        return toggleSwitch;
//    }
//}
