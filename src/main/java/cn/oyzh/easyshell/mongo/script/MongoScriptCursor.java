package cn.oyzh.easyshell.mongo.script;

import cn.oyzh.common.json.JSONUtil;
import com.mongodb.client.MongoIterable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oyzh
 * @since 2026-06-08
 */
public class MongoScriptCursor {

    private final MongoIterable<?> cursor;

    public MongoScriptCursor(MongoIterable<?> cursor) {
        this.cursor = cursor;
    }

    public String pretty() {
        List list = new ArrayList<>();
        this.cursor.forEach(list::add);
        return JSONUtil.toPretty(list);
    }

    public List<?> toArray() {
//        if (this.cursor.first() instanceof Document) {
//            List<Map<String, Object>> list = new ArrayList<>();
//            for (Object doc : this.cursor) {
//                Document d = (Document) doc;
//                list.add(new LinkedHashMap<>(d));
//            }
//            return list;
//        }
//
//        if (this.cursor.first() instanceof String) {
//            List list = new ArrayList<>();
//            this.cursor.forEach(list::add);
//            return list;
//        }
//        return Collections.emptyList();
        List list = new ArrayList<>();
        this.cursor.forEach(list::add);
        return list;

    }

    @Override
    public String toString() {
        return this.pretty();
    }
}