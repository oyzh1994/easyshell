package cn.oyzh.easyshell.fx.file;

import cn.oyzh.easyshell.domain.ShellFileCollect;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.menu.FXContextMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author oyzh
 * @since 2025-03-27
 */
public class ShellFileLocationTextField extends FXTextField {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
    }

    /**
     * 文件收藏提供方
     */
    private Supplier<List<ShellFileCollect>> fileCollectSupplier;

    public Supplier<List<ShellFileCollect>> getFileCollectSupplier() {
        return fileCollectSupplier;
    }

    public void setFileCollectSupplier(Supplier<List<ShellFileCollect>> fileCollectSupplier) {
        this.fileCollectSupplier = fileCollectSupplier;
    }

    /**
     * 暂存的历史纪录
     */
    private final List<String> tempLocations = new ArrayList<>();

    @Override
    public void text(String text) {
        if (text != null) {
            text = ShellFileUtil.fixFilePath(text);
            if (!text.equals("/") && text.endsWith("/")) {
                text = text.substring(0, text.length() - 1);
            } else if (text.isBlank()) {
                text = "/";
            }
            super.text(text);
            // 移除再新增，保证顺序
            this.tempLocations.remove(text);
            this.tempLocations.add(text);
        }
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ShellFileLocationTextFieldSkin skin() {
        ShellFileLocationTextFieldSkin skin = (ShellFileLocationTextFieldSkin) this.getSkin();
        if (skin == null) {
            skin = this.createDefaultSkin();
            this.setSkin(skin);
        }
        return skin;
    }

    @Override
    protected ShellFileLocationTextFieldSkin createDefaultSkin() {
        ShellFileLocationTextFieldSkin skin = new ShellFileLocationTextFieldSkin(this);
        skin.setItemListSupplier(this::itemList);
        return skin;
    }

    public void setOnJumpLocation(Consumer<String> onJumpLocation) {
        this.skin().setOnJumpLocation(onJumpLocation);
    }

    public Consumer<String> getOnJumpLocation() {
        return this.skin().getOnJumpLocation();
    }

    /**
     * 获取数据列表
     *
     * @return 数据列表
     */
    private List<String> itemList() {
        // 仅保留20条记录
        List<String> list = this.tempLocations.stream()
                .skip(Math.max(0, this.tempLocations.size() - 20))
                .toList();
        // 新数据列表
        List<String> newList = new ArrayList<>(list);
        // 加载收藏列表
        if (this.fileCollectSupplier != null) {
            List<ShellFileCollect> collects = this.fileCollectSupplier.get();
            for (ShellFileCollect collect : collects) {
                if (!list.contains(collect.getContent())) {
                    newList.add(collect.getContent());
                }
            }
        }
        return newList;
    }
}
