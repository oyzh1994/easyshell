package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.i18n.I18nHelper;
import software.amazon.awssdk.regions.Region;

import java.util.List;

/**
 * s3区域输入框，可搜索
 *
 * @author oyzh
 * @since 2025-07-15
 */
public class ShellS3RegionTextField extends SelectTextFiled<Region> {


    /**
     * 区域列表
     */
    private List<Region> regions;

    @Override
    protected boolean onTextChanged(String newValue) {
        if (!super.onTextChanged(newValue)) {
            return false;
        }
        // 隐藏弹窗
        if (StringUtil.isBlank(newValue)) {
            this.setItemList(this.regions);
            this.skin().hidePopup();
            return false;
        }
        // 过滤内容
        List<Region> newList = this.regions.stream()
                .filter(t -> StringUtil.containsIgnoreCase(t.toString(), newValue))
                .toList();
        // 设置内容
        this.setItemList(newList);
        // 内容为空，隐藏弹窗
        if (newList.isEmpty()) {
            this.skin().hidePopup();
        } else {
            this.skin().showPopup();
        }
        return true;
    }

    public void select(String region) {
        this.select(Region.of(region));
    }

    public void select(Region connect) {
        this.skin().selectItem(connect);
    }

    //public Region getSelectedItem() {
    //    return this.skin().getSelectedItem();
    //}

    /**
     * 加载区域
     */
    protected void loadRegions() {
        this.regions = Region.regions();
    }

    @Override
    public void initNode() {
        this.loadRegions();
        this.setItemList(this.regions);
        this.setTipText(I18nHelper.pleaseSelectRegion());
        this.skin().setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(Region o) {
                if (o == null) {
                    return "";
                }
                return o.toString();
            }
        });
        super.initNode();
    }
}
