package cn.oyzh.easyshell.mysql.data;

import cn.oyzh.common.file.FileUtil;
import com.alibaba.fastjson.JSONReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-03
 */
public class MysqlJsonTypeFileReader extends MysqlTypeFileReader {

    /**
     * json读取器
     */
    private  JSONReader reader;

    /**
     * 导入配置
     */
    private MysqlDataImportConfig config;

    public MysqlJsonTypeFileReader( File file, MysqlDataImportConfig config) throws FileNotFoundException {
        this.config = config;
        this.reader = new JSONReader(FileUtil.getReader(file, Charset.forName(config.getCharset())));
        this.init();
    }

    @Override
    protected void init() {
        // 初始化
        if (this.reader.hasNext()) {
            if (this.config.getRecordLabel() == null) {
                this.reader.startArray();
            } else {
                this.reader.startObject();
                String key = this.reader.readString();
                if (key.equalsIgnoreCase(this.config.getRecordLabel())) {
                    this.reader.startArray();
                }
            }
        }
    }

    @Override
    public Map<String, Object> readObject() {
        if (this.reader.hasNext()) {
            return this.reader.readObject(HashMap.class);
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if (this.config.getRecordLabel() == null) {
            this.reader.endArray();
            this.reader.endArray();
        } else {
            this.reader.endArray();
            this.reader.endObject();
        }
        this.reader.close();
        this.reader = null;
        this.config = null;
    }
}
