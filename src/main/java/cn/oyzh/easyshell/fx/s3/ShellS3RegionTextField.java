package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.converter.SimpleStringConverter;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import software.amazon.awssdk.regions.Region;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * s3区域输入框，可搜索
 *
 * @author oyzh
 * @since 2025-07-15
 */
public class ShellS3RegionTextField extends FXTextField {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
        this.setTipText(I18nHelper.pleaseSelectConnect());
    }

    /**
     * 区域列表
     */
    private List<Region> regions;

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ShellS3RegionTextFieldSkin skin() {
        ShellS3RegionTextFieldSkin skin = (ShellS3RegionTextFieldSkin) this.getSkin();
        if (skin == null) {
            skin = this.createDefaultSkin();
            this.setSkin(skin);
        }
        return skin;
    }

    @Override
    protected ShellS3RegionTextFieldSkin createDefaultSkin() {
        AtomicReference<ShellS3RegionTextFieldSkin> ref = new AtomicReference<>();
        FXUtil.runWait(() -> {
            ShellS3RegionTextFieldSkin skin = new ShellS3RegionTextFieldSkin(this);
            this.loadRegions();
            skin.setItemList(this.regions);
            skin.setConverter(new SimpleStringConverter<>() {
                @Override
                public String toString(Region o) {
                    if (o == null) {
                        return "";
                    }
                    return o.toString();
                }
            });
            this.addTextChangeListener((observable, oldValue, newValue) -> {
                if (this.skin().isTexting()) {
                    return;
                }
                // 移除选区
                this.skin().clearSelection();
                // 隐藏弹窗
                if (StringUtil.isBlank(newValue)) {
                    this.skin().setItemList(this.regions);
                    this.skin().hidePopup();
                    return;
                }
                // 过滤内容
                List<Region> newList = this.regions.stream()
                        .filter(t -> StringUtil.containsIgnoreCase(t.toString(), newValue))
                        .collect(Collectors.toList());
                // 设置内容
                this.skin().setItemList(newList);
                // 内容为空，隐藏弹窗
                if (newList.isEmpty()) {
                    this.skin().hidePopup();
                } else {
                    this.skin().showPopup();
                }
            });
            ref.set(skin);
        });
        return ref.get();
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
     * 移除内容
     *
     * @param connect 连接
     */
    public void removeItem(Region connect) {
        this.regions.remove(connect);
        this.skin().setItemList(this.regions);
    }

    public void selectedItemChanged(ChangeListener<Region> listener) {
        this.skin().selectItemChanged(listener);
    }

    @Override
    public boolean validate() {
        if (this.isRequire() && this.skin().getSelectedItem() == null) {
            return false;
        }
        return super.validate();
    }

    /**
     * 加载区域
     */
    protected void loadRegions() {
        this.regions = Region.regions();
    }
}
