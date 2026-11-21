package cn.oyzh.easyshell.util.db;

import java.util.ArrayList;
import java.util.List;

/**
 * SET 字段值工具。
 */
public class ShellDBSetValueUtil {

    private ShellDBSetValueUtil() {
    }

    /**
     * 解析逗号分隔的 SET 值。
     *
     * @param value SET 值
     * @return 值列表
     */
    public static List<String> parseValues(Object value) {
        if (value == null) {
            return List.of();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (String item : text.split(",")) {
            String val = item.trim();
            if (val.length() >= 2
                    && ((val.startsWith("'") && val.endsWith("'"))
                    || (val.startsWith("\"") && val.endsWith("\"")))) {
                val = val.substring(1, val.length() - 1);
            }
            if (!val.isEmpty()) {
                values.add(val);
            }
        }
        return values;
    }

    /**
     * 拼接 SET 值。
     *
     * @param values 值列表
     * @return 逗号分隔的 SET 值
     */
    public static String joinValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join(",", values);
    }
}
