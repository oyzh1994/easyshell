//package cn.oyzh.easymongo.mongo;
//
//import cn.oyzh.common.util.CollectionUtil;
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easymongo.util.MongoUtil;
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
//public class MongoRecordData {
//
//    private Map<MongoColumn, Object> dataList;
//
//    public Set<String> columns() {
//        if (this.dataList == null) {
//            return Collections.emptySet();
//        }
//        return this.dataList.keySet().stream().map(MongoColumn::getName).collect(Collectors.toSet());
//    }
//
//    public Set<String> notNullColumns() {
//        Set<String> columns = this.columns();
//        return columns.parallelStream().filter(this::hasValue).collect(Collectors.toSet());
//    }
//
//    public MongoColumn column(String column) {
//        if (this.dataList != null) {
//            for (MongoColumn dbColumn : this.dataList.keySet()) {
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
//            for (Map.Entry<MongoColumn, Object> entry : this.dataList.entrySet()) {
//                if (StringUtil.equalsAnyIgnoreCase(column, entry.getKey().getName())) {
//                    return entry.getValue();
//                }
//            }
//        }
//        return null;
//    }
//
//    public void put(MongoColumn column, Object value) {
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
//    public void remove(String column) {
//        if (this.dataList != null) {
//            MongoColumn dbColumn = this.column(column);
//            if (dbColumn != null) {
//                this.dataList.remove(dbColumn);
//            }
//        }
//    }
//
//    public Set<Map.Entry<MongoColumn, Object>> entries() {
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
//
//    public Object id() {
//        return this.value(MongoUtil.ID);
//    }
//}
