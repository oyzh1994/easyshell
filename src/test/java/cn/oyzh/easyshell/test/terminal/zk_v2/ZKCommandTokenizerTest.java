package cn.oyzh.easyshell.test.terminal.zk_v2;

import cn.oyzh.easyshell.terminal.zk_v2.ZKCommandTokenizer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ZKCommandTokenizerTest {

    @Test
    public void tokenizesQuotedAndEscapedArguments() {
        assertEquals(List.of("create", "/node", "hello world", ""), ZKCommandTokenizer.tokenize(
                "create /node \"hello world\" \"\""));
        assertEquals(List.of("set", "/node", "a b"), ZKCommandTokenizer.tokenize(
                "set /node 'a b'"));
        assertEquals(List.of("set", "/node", "a b"), ZKCommandTokenizer.tokenize(
                "set /node a\\ b"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnterminatedQuote() {
        ZKCommandTokenizer.tokenize("get \"/node");
    }
}
