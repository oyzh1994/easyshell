//package cn.oyzh.easyssh.domain;
//
//import cn.oyzh.common.util.ObjectComparator;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.Objects;
//
///**
// * ssh搜索历史
// *
// * @author oyzh
// * @since 2023/06/24
// */
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class SSHSearchHistory implements ObjectComparator<SSHSearchHistory> {
//
//    /**
//     * 词汇
//     */
//    private String kw;
//
//    /**
//     * 1 搜索词
//     * 2 替换词
//     */
//    private Integer type;
//
//    @Override
//    public boolean compare(SSHSearchHistory t1) {
//        if (t1 == null) {
//            return false;
//        }
//        if (Objects.equals(this, t1)) {
//            return true;
//        }
//        if (!Objects.equals(this.kw, t1.kw)) {
//            return false;
//        }
//        return Objects.equals(this.type, t1.type);
//    }
//}
