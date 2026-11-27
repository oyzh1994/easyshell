package cn.oyzh.easyshell.util;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.record.ShellRecordHeader;
import cn.oyzh.easyshell.store.ShellSettingStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 记录表格列头工具。
 * <p>
 * 负责「字段类型」「字段注释」的显示开关、文案格式化与列头刷新，
 * 开关值保存在应用设置中，重启后依然生效。
 *
 * @author oyzh
 * @since 2026-11-27
 */
public class ShellRecordHeaderUtil {

    /**
     * 主键标记
     */
    public static final String PRIMARY_KEY_TAG = "PK";

    /**
     * 已创建的列头（弱引用，避免影响列对象的回收）
     */
    private static final Set<ShellRecordHeader> HEADERS =
            Collections.newSetFromMap(new WeakHashMap<ShellRecordHeader, Boolean>());

    /**
     * 是否显示字段类型
     *
     * @return 结果
     */
    public static boolean showType() {
        return ShellSettingStore.SETTING.isShowColumnType();
    }

    /**
     * 是否显示字段注释
     *
     * @return 结果
     */
    public static boolean showComment() {
        return ShellSettingStore.SETTING.isShowColumnComment();
    }

    /**
     * 切换字段类型显示状态
     */
    public static void toggleShowType() {
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setShowColumnType(!setting.isShowColumnType());
        ShellSettingStore.INSTANCE.update(setting);
        refreshAll();
    }

    /**
     * 切换字段注释显示状态
     */
    public static void toggleShowComment() {
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setShowColumnComment(!setting.isShowColumnComment());
        ShellSettingStore.INSTANCE.update(setting);
        refreshAll();
    }

    /**
     * 格式化字段类型文本
     *
     * @param type       字段类型
     * @param size       字段长度
     * @param supportSize 是否支持长度
     * @return 类型文本
     */
    public static String typeText(String type, Integer size, boolean supportSize) {
        if (StringUtil.isBlank(type)) {
            return null;
        }
        if (supportSize && size != null) {
            return type + "(" + size + ")";
        }
        return type;
    }

    /**
     * 格式化字段注释文本
     *
     * @param comment    字段注释
     * @param primaryKey 是否主键
     * @return 注释文本，没有注释时返回 null（主键返回 PK 标记）
     */
    public static String commentText(String comment, boolean primaryKey) {
        if (StringUtil.isNotBlank(comment)) {
            return comment;
        }
        // 没有注释时不显示占位符；主键仍保留 PK 标记
        return primaryKey ? PRIMARY_KEY_TAG : null;
    }

    /**
     * 注册列头
     *
     * @param header 列头
     */
    public static void register(ShellRecordHeader header) {
        if (header != null) {
            HEADERS.add(header);
        }
    }

    /**
     * 刷新所有已创建的记录表格列头。
     * <p>
     * 包含未激活标签页中的表格，切换后无需重新打开即可生效。
     */
    public static void refreshAll() {
        for (ShellRecordHeader header : new ArrayList<>(HEADERS)) {
            try {
                header.refreshHeader();
            } catch (Exception ignored) {
            }
        }
    }
}
