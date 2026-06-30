//package cn.oyzh.easymongo.data;
//
//import cn.oyzh.common.file.SkipAbleFileReader;
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
//public class ShellMysqlTxtTypeFileReader extends ShellMongoTypeFileReader {
//
//    /**
//     * 字段列表
//     */
//    private List<String> columns;
//
//    /**
//     * 导入配置
//     */
//    private ShellMongoDataImportConfig config;
//
//    /**
//     * 文件读取器
//     */
//    private SkipAbleFileReader reader;
//
//    public ShellMysqlTxtTypeFileReader(File file, ShellMongoDataImportConfig config) throws IOException {
//        super(file);
//        this.config = config;
//        this.reader = new SkipAbleFileReader(file, Charset.forName(config.getCharset()));
//        // 设置换行符
//        if (!Objects.equals(config.getRecordSeparator(), System.lineSeparator())) {
//            this.reader.lineBreak(config.getRecordSeparator());
//        }
//        this.init();
//    }
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
//                Object val = ShellMongoDataImportHelper.parseValue(arr.get(i));
//                map.put(this.columns.get(i), val);
//            }
//            return map;
//        }
//        return null;
//    }
//
//    @Override
//    public void close() throws IOException {
//        if (this.reader != null) {
//            this.reader.close();
//            this.reader = null;
//            this.config = null;
//            this.columns.clear();
//            this.columns = null;
//        }
//    }
//}
