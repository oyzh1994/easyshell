//package cn.oyzh.easyshell.oracle.record;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.oracle.column.OracleColumn;
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
//public class OracleRecordData {
//
//    private Map<OracleColumn, Object> dataList;
//
//    public Set<String> columns() {
//        if (this.dataList == null) {
//            return Collections.emptySet();
//        }
//        return this.dataList.keySet().stream().map(OracleColumn::getName).collect(Collectors.toSet());
//    }
//
//    public Set<String> notNullColumns() {
//        Set<String> columns = this.columns();
//        return columns.parallelStream().filter(this::hasValue).collect(Collectors.toSet());
//    }
//
//    public OracleColumn column(String column) {
//        if (this.dataList != null) {
//            for (OracleColumn dbColumn : dataList.keySet()) {
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
//            for (Map.Entry<OracleColumn, Object> entry : dataList.entrySet()) {
//                if (StringUtil.equalsAnyIgnoreCase(column, entry.getKey().getName())) {
//                    return entry.getValue();
//                }
//            }
//        }
//        return null;
//    }
//
//    public void put(OracleColumn column, Object value) {
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
////        OracleColumn dbColumn = this.column(column);
////        if (dbColumn == null) {
////            return false;
////        }
////        return dbColumn.supportGeometry();
////    }
//
//    public void remove(String column) {
//        if (this.dataList != null) {
//            OracleColumn dbColumn = this.column(column);
//            if (dbColumn != null) {
//                this.dataList.remove(dbColumn);
//            }
//        }
//    }
//
//    public Set<Map.Entry<OracleColumn, Object>> entries() {
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
