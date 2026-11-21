package cn.oyzh.easyshell.llm.skill;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.skills.FileSystemSkill;
import dev.langchain4j.skills.FileSystemSkillLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class LLMSkillArchiveUtilTest {

    private Path root;

    @Before
    public void setUp() throws Exception {
        this.root = Files.createTempDirectory("easy-shell-skill-test-");
    }

    @After
    public void tearDown() throws Exception {
        LLMSkillArchiveUtil.deleteTree(this.root);
    }

    @Test
    public void preparesAndExtractsDirectorySkill() throws Exception {
        Path source = this.createSkillDirectory("demo");
        Path work = this.root.resolve("work");

        LLMSkillArchiveUtil.PreparedArchive archive =
                LLMSkillArchiveUtil.prepare(source.toString(), work);

        assertTrue(Files.isRegularFile(archive.zip()));
        assertEquals(64, archive.hash().length());
        assertTrue(Files.size(archive.zip()) <= LLMSkillArchiveUtil.MAX_FILE_BYTES);

        Path extracted = this.root.resolve("extracted");
        LLMSkillArchiveUtil.extract(archive.zip(), extracted,
                LLMSkillArchiveUtil.inspectArchive(archive.zip()));
        assertTrue(Files.isRegularFile(extracted.resolve("SKILL.md")));
        assertTrue(Files.readString(extracted.resolve("SKILL.md"))
                .contains("Demo instructions"));
        assertTrue(Files.isRegularFile(extracted.resolve("references/example.md")));
    }

    @Test
    public void wrapsSingleSkillMarkdown() throws Exception {
        Path skillMd = this.root.resolve("prd-SKILL.md");
        Files.writeString(skillMd, validSkill("single", "Single file skill"),
                StandardCharsets.UTF_8);

        LLMSkillArchiveUtil.PreparedArchive archive =
                LLMSkillArchiveUtil.prepare(skillMd.toString(), this.root.resolve("work"));

        Path extracted = this.root.resolve("single");
        LLMSkillArchiveUtil.extract(archive.zip(), extracted,
                LLMSkillArchiveUtil.inspectArchive(archive.zip()));
        assertTrue(Files.isRegularFile(extracted.resolve("SKILL.md")));
    }

    @Test
    public void stripsSingleTopLevelDirectoryFromImportedZip() throws Exception {
        Path zip = this.root.resolve("wrapped.zip");
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(zip))) {
            output.putNextEntry(new ZipEntry("wrapped/SKILL.md"));
            output.write(validSkill("wrapped", "Wrapped skill")
                    .getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
            output.putNextEntry(new ZipEntry("wrapped/references/example.md"));
            output.write("Reference".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }

        LLMSkillArchiveUtil.PreparedArchive archive =
                LLMSkillArchiveUtil.prepare(zip.toString(), this.root.resolve("work"));
        assertEquals("wrapped/", LLMSkillArchiveUtil.inspectArchive(archive.zip()));

        Path extracted = this.root.resolve("wrapped");
        LLMSkillArchiveUtil.extract(archive.zip(), extracted,
                LLMSkillArchiveUtil.inspectArchive(archive.zip()));
        assertTrue(Files.isRegularFile(extracted.resolve("SKILL.md")));
        assertTrue(Files.isRegularFile(extracted.resolve("references/example.md")));
    }

    @Test
    public void acceptsNestedSkillMarkdownAsResource() throws Exception {
        Path zip = this.root.resolve("nested.zip");
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(zip))) {
            output.putNextEntry(new ZipEntry("repo/SKILL.md"));
            output.write(validSkill("root-skill", "Primary skill")
                    .getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
            output.putNextEntry(new ZipEntry("repo/pptx/SKILL.md"));
            output.write(validSkill("nested-skill", "Nested resource skill")
                    .getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }

        LLMSkillArchiveUtil.PreparedArchive archive =
                LLMSkillArchiveUtil.prepare(zip.toString(), this.root.resolve("nested-work"));
        assertEquals("repo/", LLMSkillArchiveUtil.inspectArchive(archive.zip()));

        Path extracted = this.root.resolve("nested");
        LLMSkillArchiveUtil.extract(archive.zip(), extracted,
                LLMSkillArchiveUtil.inspectArchive(archive.zip()));
        assertTrue(Files.isRegularFile(extracted.resolve("SKILL.md")));
        assertTrue(Files.isRegularFile(extracted.resolve("pptx/SKILL.md")));
    }

    @Test
    public void rejectsZipWithoutSkillMarkdown() throws Exception {
        Path zip = this.root.resolve("invalid.zip");
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(zip))) {
            output.putNextEntry(new ZipEntry("README.md"));
            output.write("No skill".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }

        assertThrows(IOException.class,
                () -> LLMSkillArchiveUtil.prepare(zip.toString(), this.root.resolve("invalid-work")));
    }

    @Test
    public void rejectsHashMismatchAndMissingArchive() throws Exception {
        Path source = this.createSkillDirectory("hash");
        LLMSkillArchiveUtil.PreparedArchive archive =
                LLMSkillArchiveUtil.prepare(source.toString(), this.root.resolve("work"));

        assertThrows(IOException.class,
                () -> LLMSkillArchiveUtil.validateIntegrity(archive.zip(), "not-the-hash"));
        assertThrows(IOException.class,
                () -> LLMSkillArchiveUtil.validateIntegrity(
                        this.root.resolve("missing.zip"), archive.hash()));
    }

    @Test
    public void releasesOnlyVerifiedSkillAndCanCreateSession() throws Exception {
        Path source = this.createSkillDirectory("session");
        LLMSkillArchiveUtil.PreparedArchive prepared =
                LLMSkillArchiveUtil.prepare(source.toString(), this.root.resolve("work"));

        String uid = "skill-uid";
        Path archivePath = this.root.resolve(uid + ".zip");
        Files.copy(prepared.zip(), archivePath);

        LLMSkill skill = new LLMSkill();
        skill.setUid(uid);
        skill.setName("session");
        skill.setFilePath(archivePath.toString());
        skill.setHash(prepared.hash());
        skill.setEnabled(true);

        Path releaseRoot = this.root.resolve("cache/skills");
        LLMSkillManager manager = new LLMSkillManager(this.root, releaseRoot, null);
        assertEquals(LLMSkillValidationState.NORMAL, manager.check(skill));
        assertFalse(new LLMSkill().isEnabled());
        assertTrue(manager.verify(skill));
        assertTrue(manager.release(skill));
        assertTrue(Files.isRegularFile(releaseRoot.resolve(uid).resolve("SKILL.md")));
        assertFalse(Files.exists(this.root.resolve(uid)));

        LLMSkillSession session = LLMSkillSession.load(List.of(skill), manager);
        assertTrue(session.hasSkills());
        assertTrue(session.getToolNames().contains("activate_skill"));
        assertTrue(session.getSystemMessage().contains("session"));
        assertFalse(session.getToolProviders().isEmpty());

        skill.setScriptEnabled(true);
        LLMSkillSession noScriptSession = LLMSkillSession.load(
                List.of(skill), manager, LLMSkillScriptMode.NONE);
        assertFalse(noScriptSession.getToolNames().stream()
                .anyMatch(name -> name.startsWith("run_skill_script_")));
        assertFalse(noScriptSession.getToolNames().contains("skill_run_shell_command"));

        LLMSkillSession safeSession = LLMSkillSession.load(
                List.of(skill), manager, LLMSkillScriptMode.SAFE);
        assertTrue(safeSession.getToolNames().stream()
                .anyMatch(name -> name.startsWith("run_skill_script_")));
        assertFalse(safeSession.getToolNames().contains("skill_run_shell_command"));

        LLMSkillSession unsafeSession = LLMSkillSession.load(
                List.of(skill), manager, LLMSkillScriptMode.UNSAFE);
        assertTrue(unsafeSession.getToolNames().contains("skill_run_shell_command"));
        assertEquals(2, unsafeSession.getToolProviders().size());

        skill.setHash("bad");
        assertEquals(LLMSkillValidationState.HASH_INVALID, manager.check(skill));
        assertFalse(manager.release(skill));

        skill.setHash(prepared.hash());
        Files.delete(archivePath);
        assertEquals(LLMSkillValidationState.FILE_MISSING, manager.check(skill));
    }

    @Test
    public void safeScriptToolRunsOnlyScriptsInsideSkillRoot() throws Exception {
        Path source = this.createSkillDirectory("script-safe");
        Path scripts = source.resolve("scripts");
        Files.createDirectories(scripts);
        Files.writeString(scripts.resolve("hello.sh"),
                "printf 'hello-script'\n", StandardCharsets.UTF_8);

        LLMSkillArchiveUtil.PreparedArchive prepared =
                LLMSkillArchiveUtil.prepare(source.toString(), this.root.resolve("script-work"));
        String uid = "script-safe-uid";
        Path archive = this.root.resolve(uid + ".zip");
        Files.copy(prepared.zip(), archive);

        LLMSkill skill = new LLMSkill();
        skill.setUid(uid);
        skill.setName("script-safe");
        skill.setFilePath(archive.toString());
        skill.setHash(prepared.hash());
        skill.setEnabled(true);
        skill.setScriptEnabled(true);

        LLMSkillManager manager = new LLMSkillManager(
                this.root, this.root.resolve("cache/skills"), null);
        assertTrue(manager.release(skill));
        FileSystemSkill fileSystemSkill = FileSystemSkillLoader.loadSkill(
                manager.releasePath(uid));
        LLMSafeSkillScriptTool.SafeScriptTool tool =
                LLMSafeSkillScriptTool.create(fileSystemSkill);

        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("script-call")
                .name(tool.name())
                .arguments("{\"script_path\":\"scripts/hello.sh\"}")
                .build();
        String result = tool.executor().execute(request, null);
        assertTrue(result.contains("exitCode: 0"));
        assertTrue(result.contains("hello-script"));

        ToolExecutionRequest traversal = ToolExecutionRequest.builder()
                .id("script-traversal")
                .name(tool.name())
                .arguments("{\"script_path\":\"../outside.sh\"}")
                .build();
        assertThrows(IllegalArgumentException.class,
                () -> tool.executor().execute(traversal, null));
    }

    private Path createSkillDirectory(String name) throws IOException {
        Path skillRoot = this.root.resolve("source-" + name);
        Path references = skillRoot.resolve("references");
        Files.createDirectories(references);
        Files.writeString(skillRoot.resolve("SKILL.md"), validSkill(name, "Demo skill"),
                StandardCharsets.UTF_8);
        Files.writeString(references.resolve("example.md"), "Reference content",
                StandardCharsets.UTF_8);
        return skillRoot;
    }

    private static String validSkill(String name, String description) {
        return """
                ---
                name: %s
                description: %s
                ---

                # Demo instructions

                Follow these instructions.
                """.formatted(name, description);
    }
}
