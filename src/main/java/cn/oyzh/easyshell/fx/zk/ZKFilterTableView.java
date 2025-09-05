// package cn.oyzh.easyshell.fx.zk;
//
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.easyshell.domain.zk.ZKFilter;
// import cn.oyzh.fx.plus.controls.table.FXTableView;
// import javafx.scene.control.SelectionMode;
//
// import java.util.ArrayList;
// import java.util.List;
//
// /**
//  * @author oyzh
//  * @since 2024-12-19
//  */
// public class ZKFilterTableView extends FXTableView<ZKFilter> {
//
//     /**
//      * 当前过滤列表
//      */
//     private List<ZKFilter> list;
//
//     /**
//      * 关键字
//      */
//     private String kw;
//
//     public boolean hasData(){
//         return list != null;
//     }
//
//     public void setFilters(List<ZKFilter> filters) {
//         this.list = filters;
//         this.initDataList();
//     }
//
//     public void setKw(String kw) {
//         this.kw = kw;
//         this.initDataList();
//     }
//
//     public List<ZKFilter> getFilters() {
//         List<ZKFilter> list = new ArrayList<>(this.list.size());
//         for (ZKFilter filterVO : this.list) {
//             if (filterVO != null && StringUtil.isNotBlank(filterVO.getKw())) {
//                 list.add(filterVO);
//             }
//         }
//         return list;
//     }
//
//     private void initDataList() {
//         List<ZKFilter> list = new ArrayList<>(12);
//         if (this.list != null) {
//             for (ZKFilter filter : this.list) {
//                 if (StringUtil.isBlank(this.kw) || StringUtil.containsIgnoreCase(filter.getKw(), this.kw)) {
//                     list.add(filter);
//                 }
//             }
//         }
//         super.setItem(list);
//     }
//
//     public void addFilter(ZKFilter filter) {
//         if (this.list == null) {
//             this.list = new ArrayList<>(12);
//         }
//         this.list.add(filter);
//         this.initDataList();
//     }
//
//     @Override
//     public void removeItem(Object item) {
//         if (this.list != null) {
//             this.list.remove(item);
//         }
//         super.removeItem(item);
//         this.initDataList();
//     }
//
//     @Override
//     public void removeItem(List<?> item) {
//         if (this.list != null) {
//             this.list.removeAll(item);
//         }
//         super.removeItem(item);
//         this.initDataList();
//     }
//
//     @Override
//     public void initNode() {
//         super.initNode();
//         this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//     }
// }
