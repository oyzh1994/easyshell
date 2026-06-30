package cn.oyzh.easyshell.data.mongo;

import cn.oyzh.common.util.RegexUtil;
import com.alibaba.fastjson2.JSONArray;

/**
 * @author oyzh
 * @since 2024/09/02
 */
public class ShellMongoDataImportHelper {

    /**
     * 解析值
     *
     * @param value 值
     * @return 结果
     */
    public static Object parseValue(String value) {
        if (value == null) {
            return null;
        }
        if (RegexUtil.isDecimal(value)) {
            return Double.parseDouble(value);
        }
        if (RegexUtil.isNumber(value)) {
            return Integer.parseInt(value);
        }
        // json array
        if (value.startsWith("[") && value.endsWith("]")) {
            return JSONArray.parseArray(value);
        }
        return value;
    }
}
