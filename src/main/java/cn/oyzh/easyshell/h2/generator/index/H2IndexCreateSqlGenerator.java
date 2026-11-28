//package cn.oyzh.easyshell.h2.generator.index;
//
//import cn.oyzh.easyshell.h2.index.H2Index;
//import cn.oyzh.fx.db.DBSqlGenerator;
//
///**
// * H2创建索引SQL生成器
// *
// * @author oyzh
// * @since 2026-11-02
// */
//public class H2IndexCreateSqlGenerator extends DBSqlGenerator {
//
//    /**
//     * 生成SQL片段
//     *
//     * @param index 索引对象
//     */
//    private void _generate(H2Index index) {
//        this.sqlBuilder.append(index.normalizedDefinition(null, index.getTableName()));
//    }
//
//    /**
//     * 生成单条SQL
//     *
//     * @param index 索引对象
//     * @return SQL语句
//     */
//    public String generateSingle(H2Index index) {
//        this._generate(index);
//        return this.buildSqlSingle();
//    }
//
//    /**
//     * 生成单条SQL
//     *
//     * @param index 索引对象
//     * @return SQL语句
//     */
//    public static String generateSqlSingle(H2Index index) {
//        return new H2IndexCreateSqlGenerator().generateSingle(index);
//    }
//}
