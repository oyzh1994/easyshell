package cn.oyzh.easyshell.http.script;

import cn.oyzh.common.util.StringUtil;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Map;

/**
 * HTTP 请求脚本引擎，基于 Nashorn。
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpScriptEngine {

    private final ShellHttpScriptContext context;

    public ShellHttpScriptEngine(ShellHttpScriptContext context) {
        this.context = context;
    }

    /**
     * 执行请求前脚本
     *
     * @param script 脚本
     * @throws ScriptException 脚本异常
     */
    public void evalPreRequest(String script) throws ScriptException {
        this.eval(script);
    }

    /**
     * 执行测试脚本
     *
     * @param script 脚本
     * @throws ScriptException 脚本异常
     */
    public void evalTests(String script) throws ScriptException {
        this.eval(script);
    }

    /**
     * 执行脚本
     *
     * @param script 脚本
     * @throws ScriptException 脚本异常
     */
    private void eval(String script) throws ScriptException {
        if (StringUtil.isBlank(script)) {
            return;
        }
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine("--language=es6", "-scripting");
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        ShellHttpScriptApi api = new ShellHttpScriptApi(this.context);
        bindings.put("pm", api);
        bindings.put("console", new Console());
        Object tests = engine.eval("({})");
        bindings.put("tests", tests);
        engine.eval(script);
        // 兼容 Postman 旧式 tests["name"] = true 写法
        if (tests instanceof ScriptObjectMirror mirror) {
            for (Map.Entry<String, Object> entry : mirror.entrySet()) {
                Object value = entry.getValue();
                boolean passed = value instanceof Boolean bool && bool;
                this.context.addTestResult(entry.getKey(), passed,
                        passed ? null : String.valueOf(value));
            }
        }
    }

    /**
     * 脚本控制台
     */
    public static class Console {

        public void log(Object... values) {
            StringBuilder builder = new StringBuilder();
            for (Object value : values) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(value);
            }
            System.out.println("[HTTP Script] " + builder);
        }
    }
}
