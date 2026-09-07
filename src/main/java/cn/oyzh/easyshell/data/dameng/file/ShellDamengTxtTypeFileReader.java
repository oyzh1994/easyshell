//package cn.oyzh.easyshell.data.dameng.file;
//
//import cn.oyzh.common.file.SkipAbleFileReader;
//import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
///**
// * @author oyzh
// * @since 2024-09-04
// */
//public class ShellDamengTxtTypeFileReader extends ShellDamengTypeFileReader {
//
//    /**
//     * 字段列表
//     */
//    private List<String> columns;
//
//    /**
//     * 导入配置
//     */
//    private DBDataImportConfig config;
//
//    /**
//     * 文件读取器
//     */
//    private SkipAbleFileReader reader;
//
//    public ShellDamengTxtTypeFileReader(File file, DBDataImportConfig config) throws IOException {
//        this.config = config;
//        this.reader = new SkipAbleFileReader(file, Charset.forName(config.getCharset()));
//        // 设置换行符
//        if (!Objects.equals(config.getRecordSeparator(), System.lineSeparator())) {
//            this.reader.lineBreak(config.getRecordSeparator());
//        }
//        this.init();
//    }
//
//    // private List<String> parseLine(String line) throws IOException {
//    //     List<String> list = new ArrayList<>();
//    //     StringReader reader = new StringReader(line);
//    //     StringBuilder sb = new StringBuilder();
//    //     char fieldSeparator = this.config.fieldSeparator().charAt(0);
//    //     boolean fieldStart = false;
//    //     while (reader.ready()) {
//    //         int i = reader.read();
//    //         if (i == -1) {
//    //             break;
//    //         }
//    //         char c = (char) i;
//    //         if (c == fieldSeparator) {
//    //             if (fieldStart) {
//    //                 list.add(sb.toString());
//    //                 sb.delete(0, sb.length());
//    //                 fieldStart = false;
//    //             } else {
//    //                 fieldStart = true;
//    //             }
//    //         }
//    //     }
//    //     reader.close();
//    //     // String[] arr = line.split(this.config.fieldSeparator());
//    //     // String[] arr1 = new String[arr.length];
//    //     // for (int i = 0; i < arr.length; i++) {
//    //     //     String str = arr[i];
//    //     //     arr1[i] = (str.substring(1, str.length() - 1));
//    //     // }
//    //     // return arr1;
//    //     return list;
//    // }
//
//    @Override
//    protected void init() throws IOException {
//        this.reader.jumpLine(this.config.getColumnIndex());
//        this.columns = new ArrayList<>();
//        String line = this.reader.readLine();
//        this.columns.addAll(this.parseLine(line, this.config.txtIdentifierChar(), this.config.fieldSeparatorChar()));
//        this.reader.jumpLine(this.config.getDataStartIndex());
//    }
//
//    @Override
//    public Map<String, Object> readObject() throws IOException {
//        String line = this.reader.readLine();
//        if (line != null) {
//            List<String> arr = this.parseLine(line, this.config.txtIdentifierChar(), this.config.fieldSeparatorChar());
//            Map<String, Object> map = new HashMap<>();
//            for (int i = 0; i < arr.size(); i++) {
//                map.put(this.columns.get(i), arr.get(i));
//            }
//            return map;
//        }
//        return null;
//    }
//
//    @Override
//    public void close() throws IOException {
//        this.reader.close();
//        this.reader = null;
//        this.config = null;
//        this.columns.clear();
//        this.columns = null;
//    }
//}
