package cn.oyzh.easyshell.test.terminal.redis_v2;

import cn.oyzh.easyshell.query.redis.ShellRedisQueryParam;
import cn.oyzh.easyshell.terminal.redis_v2.RedisCommandTokenizer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RedisCommandTokenizerTest {

    @Test
    public void tokenizesQuotedAndEscapedArguments() {
        assertEquals(List.of("set", "key", "hello world", ""), RedisCommandTokenizer.tokenize(
                "set key \"hello world\" \"\""));
        assertEquals(List.of("set", "key", "a b"), RedisCommandTokenizer.tokenize("set key 'a b'"));
        assertEquals(List.of("set", "key", "a b"), RedisCommandTokenizer.tokenize("set key a\\ b"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnterminatedQuote() {
        RedisCommandTokenizer.tokenize("get \"key");
    }

    @Test
    public void queryParametersUseTheSameTokenizer() {
        ShellRedisQueryParam param = new ShellRedisQueryParam();
        param.setContent("set key \"hello world\"");
        assertEquals(List.of("set", "key", "hello world"), param.getParams());
    }
}
