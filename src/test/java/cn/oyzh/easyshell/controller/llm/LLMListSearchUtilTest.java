package cn.oyzh.easyshell.controller.llm;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LLMListSearchUtilTest {

    @Test
    public void filtersByAnyConfiguredFieldIgnoringCase() {
        List<Item> items = List.of(
                new Item("Production SSH", "root@10.0.0.1", "model-a"),
                new Item("Local Redis", "127.0.0.1", "cache-model"));

        assertEquals(List.of(items.get(0)), LLMListSearchUtil.filter(
                items, "ssh", Item::name, Item::endpoint, Item::model));
        assertEquals(List.of(items.get(1)), LLMListSearchUtil.filter(
                items, "CACHE", Item::name, Item::endpoint, Item::model));
    }

    @Test
    public void blankSearchReturnsAllAndNoMatchReturnsEmpty() {
        List<Item> items = List.of(
                new Item("one", "a", "m1"),
                new Item("two", "b", "m2"));

        assertEquals(items, LLMListSearchUtil.filter(
                items, " ", Item::name, Item::endpoint));
        assertEquals(List.of(), LLMListSearchUtil.filter(
                items, "missing", Item::name, Item::endpoint));
    }

    private record Item(String name, String endpoint, String model) {
    }
}
