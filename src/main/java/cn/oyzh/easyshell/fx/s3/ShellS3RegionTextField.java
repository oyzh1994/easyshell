package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.i18n.I18nHelper;
import software.amazon.awssdk.regions.Region;

import java.util.List;
import java.util.stream.Collectors;

/**
 * s3区域输入框，可搜索
 *
 * @author oyzh
 * @since 2025-07-15
 */
public class ShellS3RegionTextField extends SelectTextFiled<Region> {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
        this.setTipText(I18nHelper.pleaseSelectRegion());
    }

    /**
     * 区域列表
     */
    private List<Region> regions;

    // @Override
    // public ShellS3RegionTextFieldSkin skin() {
    //     return (ShellS3RegionTextFieldSkin) super.skin();
    // }
    //
    // @Override
    // protected ShellS3RegionTextFieldSkin createDefaultSkin() {
    //     if (this.getSkin() != null) {
    //         return (ShellS3RegionTextFieldSkin) this.getSkin();
    //     }
    //     return new ShellS3RegionTextFieldSkin(this);
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
        this.skin().clearSelection();
        // 隐藏弹窗
        if (StringUtil.isBlank(newValue)) {
            this.setItemList(this.regions);
            this.skin().hidePopup();
            return;
        }
        // 过滤内容
        List<Region> newList = this.regions.stream()
                .filter(t -> StringUtil.containsIgnoreCase(t.toString(), newValue))
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

    public void select(String region) {
        this.select(Region.of(region));
    }

    public void select(Region connect) {
        this.skin().selectItem(connect);
    }

    public Region getSelectedItem() {
        return this.skin().getSelectedItem();
    }

    /**
     * 加载区域
     */
    protected void loadRegions() {
        this.regions = Region.regions();
    }

    @Override
    public void initNode() {
        super.initNode();
        this.loadRegions();
        this.setItemList(this.regions);
        this.skin().setConverter(new SimpleStringConverter<>() {
            @Override
            public String toString(Region o) {
                if (o == null) {
                    return "";
                }
                return o.toString();
            }
        });
    }
}
