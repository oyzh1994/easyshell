package cn.oyzh.easyshell.mysql.data;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-03
 */
public abstract class MysqlTypeFileReader implements Closeable {

    // public TypeFileReader( String filePath) {
    //     this(new File(filePath), StandardCharsets.UTF_8);
    // }
    //
    // public TypeFileReader( File file) {
    //     this(file, StandardCharsets.UTF_8);
    // }
    //
    // public TypeFileReader( String filePath, Charset charset) {
    //     this(new File(filePath), charset);
    // }
    //
    // public TypeFileReader( File file, Charset charset) {
    // }

    protected void init() throws Exception {

    }

    public abstract Map<String, Object> readObject() throws Exception;

    public List<Map<String, Object>> readObjects(int count) throws Exception {
        // 数据列表
        List<Map<String, Object>> records = new ArrayList<>();
        // 读取数据
        while (records.size() < count) {
            Map<String, Object> item = this.readObject();
            if (item == null) {
                break;
            }
            records.add(item);
        }
        return records;
    }

    protected List<String> parseLine(String line, char txtIdentifier, char fieldSeparator) throws IOException {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean txtStart = false;
        try (StringReader reader = new StringReader(line)) {
            while (reader.ready()) {
                int i = reader.read();
                if (i == -1) {
                    break;
                }
                char c = (char) i;
                if (txtStart && c == fieldSeparator) {
                    txtStart = false;
                    continue;
                }
                if (c == txtIdentifier) {
                    if (txtStart) {
                        list.add(sb.toString());
                        sb.delete(0, sb.length());
                    } else {
                        txtStart = true;
                    }
                } else if (txtStart) {
                    sb.append(c);
                }
            }
        }
        return list;
    }

}
