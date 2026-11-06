//package cn.oyzh.easyshell.postgresql.record;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * @author oyzh
// * @since 2024/7/5
// */
//public class PostgresqlRecordData {
//
//    private Map<PostgresqlColumn, Object> dataList;
//
//    public Set<String> columns() {
//        if (this.dataList == null) {
//            return Collections.emptySet();
//        }
//        return this.dataList.keySet().stream().map(PostgresqlColumn::getName).collect(Collectors.toSet());
//    }
//
//    public Set<String> notNullColumns() {
//        Set<String> columns = this.columns();
//        return columns.parallelStream().filter(this::hasValue).collect(Collectors.toSet());
//    }
//
//    public PostgresqlColumn column(String column) {
//        if (this.dataList != null) {
//            for (PostgresqlColumn dbColumn : dataList.keySet()) {
//                if (StringUtil.equalsAnyIgnoreCase(column, dbColumn.getName())) {
//                    return dbColumn;
//                }
//            }
//        }
//        return null;
//    }
//
//    public boolean hasValue(String column) {
//        return this.value(column) != null;
//    }
//
//    public Object value(String column) {
//        if (this.dataList != null) {
//            for (Map.Entry<PostgresqlColumn, Object> entry : dataList.entrySet()) {
//                if (StringUtil.equalsAnyIgnoreCase(column, entry.getKey().getName())) {
//                    return entry.getValue();
//                }
//            }
//        }
//        return null;
//    }
//
//    public void put(PostgresqlColumn column, Object value) {
//        if (this.dataList == null) {
//            this.dataList = new HashMap<>();
//        }
//        this.dataList.put(column, value);
//    }
//
//    public boolean isEmpty() {
//        return CollectionUtil.isEmpty(this.dataList);
//    }
//
////    public boolean isTypeGeometry(String column) {
////        if (CollectionUtil.isEmpty(this.dataList)) {
////            return false;
////        }
////        PostgresqlColumn dbColumn = this.column(column);
////        if (dbColumn == null) {
////            return false;
////        }
////        return dbColumn.supportGeometry();
////    }
//
//    public void remove(String column) {
//        if (this.dataList != null) {
//            PostgresqlColumn dbColumn = this.column(column);
//            if (dbColumn != null) {
//                this.dataList.remove(dbColumn);
//            }
//        }
//    }
//
//    public Set<Map.Entry<PostgresqlColumn, Object>> entries() {
//        if (this.dataList == null) {
//            return Collections.emptySet();
//        }
//        return this.dataList.entrySet();
//    }
//
//    public Collection<Object> values() {
//        if (this.dataList == null) {
//            return Collections.emptyList();
//        }
//        return this.dataList.values();
//    }
//
//    public int columnSize() {
//        if (this.dataList == null) {
//            return 0;
//        }
//        return this.dataList.size();
//    }
//
//    public boolean notNull(String column) {
//        return this.value(column) != null;
//    }
//}
