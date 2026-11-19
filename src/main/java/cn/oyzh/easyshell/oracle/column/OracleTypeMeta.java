package cn.oyzh.easyshell.oracle.column;

import java.util.List;

/**
 * Oracle对象/集合类型元数据。
 */
public record OracleTypeMeta(String owner, String name, String typeCode,
                             List<Attribute> attributes, OracleTypeMeta element) {

    public boolean isObject() {
        return "OBJECT".equalsIgnoreCase(typeCode);
    }

    public boolean isCollection() {
        return "COLLECTION".equalsIgnoreCase(typeCode);
    }

    public record Attribute(String name, OracleTypeMeta type) {
    }
}
