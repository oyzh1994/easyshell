package cn.oyzh.easyshell.http;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.http.script.ShellHttpScriptContext;
import cn.oyzh.easyshell.http.script.ShellHttpScriptEngine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * HTTP 请求脚本引擎测试。
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpScriptEngineTest {

    @Test
    public void runsScriptsAgainstMockServer() throws Throwable {
        try (ShellHttpMockServer server = new ShellHttpMockServer()) {
            server.start();
            ShellConnect connect = new ShellConnect();
            connect.setType(ShellPrototype.HTTP);
            connect.setName("Script Mock");
            ShellHttpClient client = new ShellHttpClient(connect);
            client.start();
            try {
                ShellHttpWorkspace workspace = new ShellHttpWorkspace();
                ShellHttpRequest request = new ShellHttpRequest();
                request.setUrl(server.baseUrl() + "/api/json");
                ShellHttpScriptContext context = new ShellHttpScriptContext(workspace, request, null);
                ShellHttpScriptEngine engine = new ShellHttpScriptEngine(context);

                engine.evalPreRequest("""
                        pm.variables.set('token', 'script-token');
                        pm.request.headers.add({key: 'X-Script', value: pm.variables.get('token')});
                        """);
                ShellHttpResponse response = client.execute(request, workspace);
                context.setResponse(response);
                engine.evalTests("""
                        pm.test('status is 200', function () {
                            pm.response.to.have.status(200);
                        });
                        pm.test('message is ok', function () {
                            var body = pm.response.json();
                            if (body.message !== 'ok') throw new Error('bad message');
                        });
                        """);
                assertEquals(2, context.getTestResults().size());
                assertTrue(context.getTestResults().stream()
                        .allMatch(ShellHttpScriptContext.TestResult::passed));
            } finally {
                client.close();
            }
        }
    }

    @Test
    public void runsPreRequestScriptAndTests() throws Exception {
        ShellHttpWorkspace workspace = new ShellHttpWorkspace();
        ShellHttpRequest request = new ShellHttpRequest();
        ShellHttpScriptContext context = new ShellHttpScriptContext(workspace, request, null);
        ShellHttpScriptEngine engine = new ShellHttpScriptEngine(context);

        engine.evalPreRequest("""
                pm.variables.set('token', 'abc');
                pm.request.url = 'https://example.com/api';
                pm.request.method = 'POST';
                pm.request.headers.add({key: 'X-Test', value: '1'});
                """);
        assertEquals("https://example.com/api", request.getUrl());
        assertEquals("POST", request.getMethod());
        assertEquals("1", request.getHeaders().getFirst().getValue());

        ShellHttpResponse response = new ShellHttpResponse();
        response.setStatusCode(200);
        context.setResponse(response);
        engine.evalTests("""
                pm.test('token exists', function () {
                    if (pm.variables.get('token') !== 'abc') throw new Error('bad token');
                });
                pm.response.to.have.status(200);
                pm.expect(pm.response.code).to.equal(200);
                tests['legacy test'] = true;
                """);
        assertTrue(context.getTestResults().stream().allMatch(ShellHttpScriptContext.TestResult::passed));
        assertTrue(context.getTestResults().size() >= 2);
    }
}
