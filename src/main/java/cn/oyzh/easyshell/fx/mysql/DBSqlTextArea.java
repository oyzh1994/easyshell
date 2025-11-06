package cn.oyzh.easyshell.fx.mysql;// package cn.oyzh.easymysql.mysql;
//
// import cn.oyzh.easyshell.mysql.DBDialect;
// import cn.oyzh.easyshell.query.mysql.MysqlQueryUtil;
// import cn.oyzh.easyshell.mysql.sql.DBSqlParser;
// import cn.oyzh.mysql.editor.incubator.Editor;
//
// import java.util.HashSet;
// import java.util.Set;
//
// /**
//  * db查询文本域
//  *
//  * @author oyzh
//  * @since 2024/02/18
//  */
// public class DBSqlTextArea extends Editor {
//
//     {
//         this.showLineNum();
//         // this.addTextChangeListener((observable, oldValue, newValue) -> this.initTextStyle());
//     }
//
//     /**
//      * 方言
//      */
//     private DBDialect dialect;
//
//     /**
//      * 美化sql
//      *
//      */
//     public void pretty() throws Exception {
//         String sql = this.getText();
//         String prettySql = DBSqlParser.prettySql(sql, this.dialect);
//         this.setText(prettySql);
//         // this.initTextStyle();
//     }
//
//     // /**
//     //  * sql关键字正则模式
//     //  */
//     // private static Pattern Sql_Symbol_Pattern;
//     //
//     // private static Pattern sqlSymbolPattern() {
//     //     if (Sql_Symbol_Pattern == null) {
//     //         StringBuilder keywords = new StringBuilder();
//     //         for (String keyword : MysqlQueryUtil.getKeywords()) {
//     //             keywords.append("|").append(keyword);
//     //         }
//     //         String regex = "(?i)\\b(" + keywords.substring(1) + ")\\b";
//     //         Sql_Symbol_Pattern = Pattern.compile(regex);
//     //     }
//     //     return Sql_Symbol_Pattern;
//     // }
//     //
//     // @Override
//     // public void initTextStyle() {
//     //     Runnable task = () -> {
//     //         this.clearTextStyle();
//     //         String text = this.getText();
//     //         if (!text.isEmpty()) {
//     //             Matcher matcher1 = sqlSymbolPattern().matcher(text);
//     //             List<RichTextStyle> styles = new ArrayList<>();
//     //             while (matcher1.find()) {
//     //                 styles.add(new RichTextStyle(matcher1.start(), matcher1.end(), "-mysql-fill: #4169E1;"));
//     //             }
//     //             for (RichTextStyle style : styles) {
//     //                 this.setStyle(style);
//     //             }
//     //         }
//     //     };
//     //     TaskManager.startDelay("db:sql:initTextStyle:" + this.hashCode(), () -> FXUtil.runLater(task), 150);
//     // }
//
//     @Override
//     public Set<String> getPrompts() {
//         if (super.getPrompts() == null) {
//             // 设置内容提示符
//             Set<String> set = new HashSet<>(MysqlQueryUtil.getKeywords());
//             this.setPrompts(set);
//         }
//         return super.getPrompts();
//     }
//
//     public DBDialect getDialect() {
//         return dialect;
//     }
//
//     public void setDialect(DBDialect dialect) {
//         this.dialect = dialect;
//     }
// }
