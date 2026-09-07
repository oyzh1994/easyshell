//package cn.oyzh.easyshell.data.dameng.file;
//
//import cn.oyzh.common.file.FileUtil;
//import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
//import com.alibaba.fastjson2.JSONReader;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.Map;
//
///**
// * @author oyzh
// * @since 2024-09-03
// */
//public class ShellDamengJsonTypeFileReader extends ShellDamengTypeFileReader {
//
//    /**
//     * json读取器
//     */
//    private JSONReader reader;
//
//    /**
//     * 导入配置
//     */
//    private DBDataImportConfig config;
//
//    public ShellDamengJsonTypeFileReader(File file, DBDataImportConfig config) throws FileNotFoundException {
//        this.config = config;
//        this.reader = JSONReader.of(FileUtil.getReader(file, Charset.forName(config.getCharset())));
//        this.init();
//    }
//
//    @Override
//    protected void init() {
//        // 初始化
//        if (this.reader.isEnd()) {
//            return;
//        }
//        if (this.config.getRecordLabel() == null) {
//            // 纯数组格式: [...]
//            this.reader.startArray();
//        } else {
//            // 对象包装格式: {"recordLabel": [...]}
//            this.reader.nextIfObjectStart();
//            String key = this.reader.readFieldName();
//            if (key != null && key.equalsIgnoreCase(this.config.getRecordLabel())) {
//                this.reader.startArray();
//            }
//        }
//    }
//
//    @Override
//    public Map<String, Object> readObject() {
//        // 到达流末尾或数组末尾（]）时返回 null
//        if (this.reader.isEnd() || this.reader.nextIfMatch(']')) {
//            return null;
//        }
//        Map<String, Object> object = this.reader.readObject();
//        // 消费元素间的逗号分隔符
//        this.reader.nextIfComma();
//        return object;
//    }
//
//    @Override
//    public void close() throws IOException {
//        if (this.reader != null) {
//            if (this.config.getRecordLabel() != null) {
//                // 消费包装对象的结束符 '}'
//                this.reader.nextIfObjectEnd();
//            }
//            this.reader.close();
//            this.reader = null;
//            this.config = null;
//        }
//    }
//}
