//package cn.oyzh.easyshell.sqlite.generator.index;
//
//import cn.oyzh.easyshell.sqlite.index.SQLiteIndex;
//import cn.oyzh.fx.db.DBSqlGenerator;
//
///**
// * SQLite创建索引SQL生成器
// *
// * @author oyzh
// * @since 2026-11-02
// */
//public class SQLiteIndexCreateSqlGenerator extends DBSqlGenerator {
//
//    /**
//     * 生成SQL片段
//     *
//     * @param index 索引对象
//     */
//    private void _generate(SQLiteIndex index) {
//        this.sqlBuilder.append(index.normalizedDefinition(null, index.getTableName()));
//    }
//
//    /**
//     * 生成单条SQL
//     *
//     * @param index 索引对象
//     * @return SQL语句
//     */
//    public String generateSingle(SQLiteIndex index) {
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
//    public static String generateSqlSingle(SQLiteIndex index) {
//        return new SQLiteIndexCreateSqlGenerator().generateSingle(index);
//    }
//}
