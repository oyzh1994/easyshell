package cn.oyzh.easyshell.test.mongo;

import cn.oyzh.easyshell.mongo.script.MongoScriptParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MongoScriptParserTest {

    @Test
    public void parsesIndentedStatements() {
        String script = """
                db.getCollection('users').find();
                    db.getCollection('users').countDocuments({status: 'active'});
                """;

        List<String> statements = MongoScriptParser.getParser(script).parseScript();

        assertEquals(2, statements.size());
        assertEquals("db.getCollection('users').find();", statements.get(0));
        assertEquals("    db.getCollection('users').countDocuments({status: 'active'});", statements.get(1));
    }

    @Test
    public void keepsMultilineStatementTogether() {
        String script = """
                db.getCollection('users').aggregate([
                    {$match: {status: 'active'}},
                    {$group: {_id: '$city', total: {$sum: 1}}}
                ]);
                """;

        List<String> statements = MongoScriptParser.getParser(script).parseScript();

        assertEquals(1, statements.size());
        assertTrue(statements.getFirst().startsWith("db.getCollection('users').aggregate(["));
        assertTrue(statements.getFirst().endsWith("]);"));
    }

    @Test
    public void ignoresStatementsThatAreNotMongoDbCommands() {
        String script = """
                use users;
                show collections;
                """;

        assertTrue(MongoScriptParser.getParser(script).parseScript().isEmpty());
    }
}
