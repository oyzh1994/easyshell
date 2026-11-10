package cn.oyzh.easyshell.domain;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

/**
 * Fixed macOS-style tags for connections and groups.
 *
 * @author oyzh
 * @since 2026-11-10
 */
public enum ShellConnectTag {

    RED("red", Color.web("#FF3B30"), ShellI18nHelper::tagRed),

    ORANGE("orange", Color.web("#FF9500"), ShellI18nHelper::tagOrange),

    YELLOW("yellow", Color.web("#FFCC00"), ShellI18nHelper::tagYellow),

    GREEN("green", Color.web("#34C759"), ShellI18nHelper::tagGreen),

    BLUE("blue", Color.web("#007AFF"), ShellI18nHelper::tagBlue),

    PURPLE("purple", Color.web("#AF52DE"), ShellI18nHelper::tagPurple),

    GRAY("gray", Color.web("#8E8E93"), ShellI18nHelper::tagGray);

    private final String id;

    private final Color color;

    private final Supplier<String> displayName;

    ShellConnectTag(String id, Color color, Supplier<String> displayName) {
        this.id = id;
        this.color = color;
        this.displayName = displayName;
    }

    public String id() {
        return this.id;
    }

    public Color color() {
        return this.color;
    }

    public String displayName() {
        return this.displayName.get();
    }

    /**
     * Parses persisted comma-separated tag ids.
     */
    public static EnumSet<ShellConnectTag> parse(String tags) {
        EnumSet<ShellConnectTag> result = EnumSet.noneOf(ShellConnectTag.class);
        if (StringUtil.isNotBlank(tags)) {
            for (String value : tags.split(",")) {
                ShellConnectTag tag = fromId(value.trim());
                if (tag != null) {
                    result.add(tag);
                }
            }
        }
        return result;
    }

    /**
     * Serializes tags in the fixed preset order.
     */
    public static String serialize(Collection<ShellConnectTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        List<String> values = new ArrayList<>();
        for (ShellConnectTag tag : values()) {
            if (tags.contains(tag)) {
                values.add(tag.id);
            }
        }
        return values.isEmpty() ? null : String.join(",", values);
    }

    /**
     * Returns display colors for a persisted tag string.
     */
    public static List<Color> colors(String tags) {
        return parse(tags).stream().map(ShellConnectTag::color).toList();
    }

    public static ShellConnectTag fromId(String id) {
        for (ShellConnectTag tag : values()) {
            if (tag.id.equalsIgnoreCase(id)) {
                return tag;
            }
        }
        return null;
    }
}
