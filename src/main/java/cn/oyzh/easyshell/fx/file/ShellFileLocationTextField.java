package cn.oyzh.easyshell.fx.file;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellFileCollect;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 文件路径输入框
 *
 * @author oyzh
 * @since 2025-03-27
 */
public class ShellFileLocationTextField extends FXTextField {

//    {
//        // 覆盖默认菜单
//        this.setContextMenu(FXContextMenu.EMPTY);
//    }

    /**
     * 文件收藏提供方
     */
    private Supplier<List<ShellFileCollect>> fileCollectSupplier;

    /**
     * 获取文件收藏提供方
     *
     * @return 文件收藏提供方
     */
    public Supplier<List<ShellFileCollect>> getFileCollectSupplier() {
        return fileCollectSupplier;
    }

    /**
     * 设置文件收藏提供方
     *
     * @param fileCollectSupplier 文件收藏提供方
     */
    public void setFileCollectSupplier(Supplier<List<ShellFileCollect>> fileCollectSupplier) {
        this.fileCollectSupplier = fileCollectSupplier;
    }

    /**
     * 暂存的历史纪录
     */
    private final List<String> tempLocations = new ArrayList<>();

    @Override
    public void text(String text) {
        if (text == null) {
            return;
        }
        text = text.trim();
        if (StringUtil.equals(text, this.getTextTrim())) {
            return;
        }
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

    @Override
    public ShellFileLocationTextFieldSkin skin() {
        return (ShellFileLocationTextFieldSkin) super.skin();
    }

    @Override
    protected ShellFileLocationTextFieldSkin createDefaultSkin() {
        ShellFileLocationTextFieldSkin skin = new ShellFileLocationTextFieldSkin(this);
        skin.setItemListSupplier(this::itemList);
        return skin;
    }

    /**
     * 设置路径跳转回调
     *
     * @param onJumpLocation 路径跳转回调
     */
    public void setOnJumpLocation(Consumer<String> onJumpLocation) {
        this.skin().setOnJumpLocation(onJumpLocation);
    }

    /**
     * 获取路径跳转回调
     *
     * @return 路径跳转回调
     */
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
