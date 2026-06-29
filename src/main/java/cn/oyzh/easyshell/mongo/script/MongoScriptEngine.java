package cn.oyzh.easyshell.mongo.script;

import cn.oyzh.easyshell.mongo.script.function.MongoScriptBinaryFcuntion;
import cn.oyzh.easyshell.mongo.script.function.MongoScriptCodeFunction;
import cn.oyzh.easyshell.mongo.script.function.MongoScriptISODateFunction;
import cn.oyzh.easyshell.mongo.script.function.MongoScriptInit32Function;
import cn.oyzh.easyshell.mongo.script.function.MongoScriptLongFunction;
import cn.oyzh.easyshell.mongo.script.function.MongoScriptObjectIdFunction;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 *
 * @author oyzh
 * @since 2026-06-08
 */
public class MongoScriptEngine {

    private final MongoClient mongoClient;

    public MongoScriptEngine(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        this.initEngine();
    }

    private javax.script.ScriptEngine engine;

    private Bindings bindings;

    private void initEngine() {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        this.engine = factory.getScriptEngine("--language=es6", "-scripting");
        this.bindings = this.engine.getBindings(ScriptContext.ENGINE_SCOPE);

        // 注入 MongoDB 特殊类型构造函数
        this.engine.put("Code", new MongoScriptCodeFunction());
        this.engine.put("Long", new MongoScriptLongFunction());
        this.engine.put("Int32", new MongoScriptInit32Function());
        this.engine.put("Binary", new MongoScriptBinaryFcuntion());
        this.engine.put("ISODate", new MongoScriptISODateFunction());
        this.engine.put("ObjectId", new MongoScriptObjectIdFunction());
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public void db(String dbName) {
        // 注入包装后的 db 对象
        MongoDatabase database = this.mongoClient.getDatabase(dbName);
        this.bindings.put("db", new MongoScriptDatabase(database));
        this.engine.put("dbName", dbName);
    }

    public Object eval(String script) throws ScriptException {
        return this.engine.eval(script);
    }

    public void put(String key, Object val) {
        this.engine.put(key, val);
    }
}
