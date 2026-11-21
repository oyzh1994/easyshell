package cn.oyzh.easyshell.util.oracle;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleTypeMeta;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Oracle对象/集合类型与JSON结构互转。
 */
public final class OracleComplexValueUtil {

    private OracleComplexValueUtil() {
    }

    /**
     * 将JDBC对象/集合值转换为JSON友好的Map/List。
     *
     * @param connection 连接
     * @param value      JDBC值
     * @return JSON结构
     * @throws SQLException 异常
     */
    public static Object toJsonValue(Connection connection, Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        if (value instanceof Struct struct) {
            String[] typeName = parseTypeName(struct.getSQLTypeName());
            List<AttributeMeta> attributes = objectAttributes(connection, typeName[0], typeName[1]);
            Object[] values = struct.getAttributes();
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < values.length; i++) {
                String attributeName = i < attributes.size() ? attributes.get(i).name : "ATTR" + (i + 1);
                map.put(attributeName, toJsonValue(connection, values[i]));
            }
            return map;
        }
        if (value instanceof Array array) {
            Object elements = array.getArray();
            if (elements instanceof Object[] objects) {
                List<Object> list = new ArrayList<>(objects.length);
                for (Object object : objects) {
                    list.add(toJsonValue(connection, object));
                }
                return list;
            }
            return elements;
        }
        return value;
    }

    /**
     * 将JSON字符串/Map/List转换为Oracle JDBC对象或集合。
     *
     * @param connection 连接
     * @param column     字段
     * @param value      值
     * @return JDBC值
     * @throws SQLException 异常
     */
    public static Object toJdbcValue(Connection connection, OracleColumn column, Object value) throws SQLException {
        Object json = parse(value);
        if (json == null) {
            return null;
        }
        if (column.isCollectionType()) {
            return toArray(connection, column.getTypeOwner(), column.getType(), toList(json));
        }
        if (column.isObjectType()) {
            return toStruct(connection, column.getTypeOwner(), column.getType(), toMap(json));
        }
        return value;
    }

    /**
     * 为对象/集合列绑定NULL。
     *
     * @param statement 预处理语句
     * @param index     索引
     * @param column    字段
     * @throws SQLException 异常
     */
    public static void setNull(PreparedStatement statement, int index, OracleColumn column) throws SQLException {
        int sqlType = column.isCollectionType() ? Types.ARRAY : Types.STRUCT;
        statement.setNull(index, sqlType, typeName(column.getTypeOwner(), column.getType()));
    }

    /**
     * 加载对象/集合类型元数据。
     *
     * @param connection 连接
     * @param owner      类型所有者
     * @param type       类型名称
     * @return 类型元数据
     * @throws SQLException 异常
     */
    public static OracleTypeMeta loadTypeMeta(Connection connection, String owner, String type) throws SQLException {
        return loadTypeMeta(connection, owner, type, new HashMap<>(), new HashSet<>());
    }

    private static OracleTypeMeta loadTypeMeta(Connection connection, String owner, String type,
                                               Map<String, OracleTypeMeta> cache, Set<String> loading)
            throws SQLException {
        if (StringUtil.isBlank(type)) {
            return null;
        }
        String key = (StringUtil.isBlank(owner) ? "" : owner.toUpperCase(Locale.ROOT)) + "."
                + type.toUpperCase(Locale.ROOT);
        OracleTypeMeta cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        String typeCode = typeCode(connection, owner, type);
        if (loading.contains(key)) {
            return new OracleTypeMeta(owner, type, typeCode, List.of(), null);
        }
        loading.add(key);
        List<OracleTypeMeta.Attribute> attributes = List.of();
        OracleTypeMeta element = null;
        if (StringUtil.equalsIgnoreCase(typeCode, "OBJECT")) {
            List<OracleTypeMeta.Attribute> list = new ArrayList<>();
            for (AttributeMeta attribute : objectAttributes(connection, owner, type)) {
                list.add(new OracleTypeMeta.Attribute(attribute.name,
                        loadTypeMeta(connection, attribute.owner, attribute.type, cache, loading)));
            }
            attributes = list;
        } else if (StringUtil.equalsIgnoreCase(typeCode, "COLLECTION")) {
            ElementMeta meta = collectionElement(connection, owner, type);
            element = loadTypeMeta(connection, meta.owner, meta.type, cache, loading);
        }
        OracleTypeMeta result = new OracleTypeMeta(owner, type, typeCode, attributes, element);
        cache.put(key, result);
        loading.remove(key);
        return result;
    }

    /**
     * 将对象/集合值生成Oracle SQL构造函数字面量。
     *
     * @param column 字段
     * @param value  值
     * @return SQL字面量
     */
    public static String toSqlLiteral(OracleColumn column, Object value) {
        return toSqlLiteral(column, value, true);
    }

    /**
     * 将对象/集合值生成Oracle SQL构造函数字面量。
     *
     * @param column       字段
     * @param value        值
     * @param qualifyType  是否携带类型所有者
     * @return SQL字面量
     */
    public static String toSqlLiteral(OracleColumn column, Object value, boolean qualifyType) {
        Object json = parse(value);
        if (json == null) {
            return "NULL";
        }
        OracleTypeMeta meta = column.getTypeMeta();
        if (meta == null) {
            throw new IllegalArgumentException("Missing Oracle type metadata: "
                    + column.getTypeOwner() + "." + column.getType());
        }
        return toSqlLiteral(meta, json, qualifyType);
    }

    private static String toSqlLiteral(OracleTypeMeta meta, Object value, boolean qualifyType) {
        if (value == null) {
            return "NULL";
        }
        if (meta.isObject()) {
            Map<?, ?> map = toMap(value);
            Map<String, Object> values = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                values.put(String.valueOf(entry.getKey()).toLowerCase(Locale.ROOT), entry.getValue());
            }
            StringBuilder builder = new StringBuilder(
                    qualifyType ? typeName(meta.owner(), meta.name()) : DBUtil.wrap(meta.name(), DBDialect.ORACLE))
                    .append("(");
            for (OracleTypeMeta.Attribute attribute : meta.attributes()) {
                Object attributeValue = values.get(attribute.name().toLowerCase(Locale.ROOT));
                builder.append(toSqlLiteral(attribute.type(), attributeValue, qualifyType)).append(", ");
            }
            if (builder.toString().endsWith(", ")) {
                builder.delete(builder.length() - 2, builder.length());
            }
            return builder.append(")").toString();
        }
        if (meta.isCollection()) {
            List<?> list = toList(value);
            StringBuilder builder = new StringBuilder(
                    qualifyType ? typeName(meta.owner(), meta.name()) : DBUtil.wrap(meta.name(), DBDialect.ORACLE))
                    .append("(");
            for (Object element : list) {
                builder.append(toSqlLiteral(meta.element(), element, qualifyType)).append(", ");
            }
            if (builder.toString().endsWith(", ")) {
                builder.delete(builder.length() - 2, builder.length());
            }
            return builder.append(")").toString();
        }
        OracleColumn column = new OracleColumn();
        column.setType(meta.name());
        column.setTypeOwner(meta.owner());
        column.setTypeCode(meta.typeCode());
        return String.valueOf(ShellOracleDataUtil.parameterizedForSql(column, value));
    }

    private static Object parse(Object value) {
        if (value instanceof CharSequence sequence) {
            String text = sequence.toString().trim();
            if (text.isEmpty()) {
                return null;
            }
            return text.startsWith("[") ? JSONUtil.parseArray(text) : JSONUtil.parseObject(text);
        }
        return value;
    }

    private static Struct toStruct(Connection connection, String owner, String type, Map<?, ?> map)
            throws SQLException {
        List<AttributeMeta> attributes = objectAttributes(connection, owner, type);
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            values.put(String.valueOf(entry.getKey()).toLowerCase(Locale.ROOT), entry.getValue());
        }
        Object[] attributeValues = new Object[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            AttributeMeta attribute = attributes.get(i);
            Object value = values.get(attribute.name.toLowerCase(Locale.ROOT));
            attributeValues[i] = convertValue(connection, attribute.owner, attribute.type, value);
        }
        return connection.createStruct(typeName(owner, type), attributeValues);
    }

    private static Array toArray(Connection connection, String owner, String type, List<?> list)
            throws SQLException {
        ElementMeta element = collectionElement(connection, owner, type);
        Object[] values = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            values[i] = convertValue(connection, element.owner, element.type, list.get(i));
        }
        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor(typeName(owner, type), connection);
        return new ARRAY(descriptor, connection, values);
    }

    private static Object convertValue(Connection connection, String owner, String type, Object value)
            throws SQLException {
        if (value == null) {
            return null;
        }
        String typeCode = typeCode(connection, owner, type);
        if (StringUtil.equalsIgnoreCase(typeCode, "OBJECT")) {
            return toStruct(connection, owner, type, toMap(value));
        }
        if (StringUtil.equalsIgnoreCase(typeCode, "COLLECTION")) {
            return toArray(connection, owner, type, toList(value));
        }
        return scalar(value);
    }

    private static List<AttributeMeta> objectAttributes(Connection connection, String owner, String type)
            throws SQLException {
        if (StringUtil.isBlank(owner) || StringUtil.isBlank(type)) {
            return List.of();
        }
        String sql = """
                SELECT ATTR_NAME, ATTR_TYPE_OWNER, ATTR_TYPE_NAME
                FROM ALL_TYPE_ATTRS
                WHERE OWNER = ?
                AND TYPE_NAME = ?
                ORDER BY ATTR_NO
                """;
        List<AttributeMeta> attributes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, owner);
            statement.setString(2, type);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    attributes.add(new AttributeMeta(
                            resultSet.getString("ATTR_NAME"),
                            resultSet.getString("ATTR_TYPE_OWNER"),
                            resultSet.getString("ATTR_TYPE_NAME")));
                }
            }
        }
        return attributes;
    }

    private static ElementMeta collectionElement(Connection connection, String owner, String type)
            throws SQLException {
        String sql = """
                SELECT ELEM_TYPE_OWNER, ELEM_TYPE_NAME
                FROM ALL_COLL_TYPES
                WHERE OWNER = ?
                AND TYPE_NAME = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, owner);
            statement.setString(2, type);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new ElementMeta(
                            resultSet.getString("ELEM_TYPE_OWNER"),
                            resultSet.getString("ELEM_TYPE_NAME"));
                }
            }
        }
        throw new SQLException("Unable to resolve Oracle collection element type: " + owner + "." + type);
    }

    private static String typeCode(Connection connection, String owner, String type) throws SQLException {
        if (StringUtil.isBlank(owner) || StringUtil.isBlank(type)) {
            return null;
        }
        String sql = "SELECT TYPECODE FROM ALL_TYPES WHERE OWNER = ? AND TYPE_NAME = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, owner);
            statement.setString(2, type);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        }
    }

    private static String typeName(String owner, String type) {
        String result = DBUtil.wrap(type, DBDialect.ORACLE);
        if (StringUtil.isBlank(owner)) {
            return result;
        }
        return DBUtil.wrap(owner, DBDialect.ORACLE) + "." + result;
    }

    private static String[] parseTypeName(String sqlTypeName) {
        if (StringUtil.isBlank(sqlTypeName)) {
            return new String[]{null, null};
        }
        String result = sqlTypeName.replace("\"", "");
        int index = result.lastIndexOf('.');
        if (index > 0) {
            return new String[]{result.substring(0, index), result.substring(index + 1)};
        }
        return new String[]{null, result};
    }

    private static Object scalar(Object value) {
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return value;
    }

    private static Map<?, ?> toMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map;
        }
        if (value instanceof CharSequence sequence) {
            return JSONUtil.parseObject(sequence.toString());
        }
        throw new IllegalArgumentException("Object value must be a JSON object: " + value);
    }

    private static List<?> toList(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        if (value instanceof CharSequence sequence) {
            return JSONUtil.parseArray(sequence.toString());
        }
        throw new IllegalArgumentException("Collection value must be a JSON array: " + value);
    }

    private record AttributeMeta(String name, String owner, String type) {
    }

    private record ElementMeta(String owner, String type) {
    }
}
