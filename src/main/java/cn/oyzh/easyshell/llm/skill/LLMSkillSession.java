package cn.oyzh.easyshell.llm.skill;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.store.llm.LLMSkillStore;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.skills.FileSystemSkill;
import dev.langchain4j.skills.FileSystemSkillLoader;
import dev.langchain4j.skills.Skills;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 一次 LLM 请求使用的 Agent Skills 集合。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMSkillSession implements AutoCloseable {

    private static final String ACTIVATE_TOOL = "activate_skill";

    private static final String RESOURCE_TOOL = "read_skill_resource";

    private final Skills skills;

    private final Set<String> toolNames;

    private final Map<String, String> toolSources;

    private final List<String> errors;

    private final String systemMessage;

    private LLMSkillSession(Skills skills,
                            Set<String> toolNames,
                            Map<String, String> toolSources,
                            List<String> errors,
                            String systemMessage) {
        this.skills = skills;
        this.toolNames = Set.copyOf(toolNames);
        this.toolSources = Map.copyOf(toolSources);
        this.errors = List.copyOf(errors);
        this.systemMessage = systemMessage;
    }

    public static LLMSkillSession loadEnabled() {
        try {
            return load(LLMSkillStore.INSTANCE.selectEnabled(), LLMSkillManager.INSTANCE);
        } catch (Exception ex) {
            JulLog.warn("load enabled skills failed", ex);
            return empty(List.of("configuration: " + ex.getMessage()));
        }
    }

    static LLMSkillSession load(List<LLMSkill> configurations,
                                LLMSkillManager manager) {
        if (CollectionUtil.isEmpty(configurations)) {
            return empty(List.of());
        }
        List<FileSystemSkill> loaded = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, String> toolSources = new LinkedHashMap<>();
        Set<String> names = new LinkedHashSet<>();
        boolean hasResources = false;
        for (LLMSkill configuration : configurations) {
            if (configuration == null || !configuration.isEnabled()) {
                continue;
            }
            if (!manager.release(configuration)) {
                errors.add(configuration.getName() + ": invalid skill archive");
                continue;
            }
            try {
                FileSystemSkill skill = FileSystemSkillLoader.loadSkill(
                        manager.releasePath(configuration.getUid()));
                String normalizedName = skill.name().toLowerCase(java.util.Locale.ROOT);
                if (!names.add(normalizedName)) {
                    errors.add(configuration.getName() + ": duplicate skill name");
                    continue;
                }
                loaded.add(skill);
                hasResources = hasResources || !skill.resources().isEmpty();
            } catch (Exception ex) {
                errors.add(configuration.getName() + ": " + ex.getMessage());
                JulLog.warn("load skill failed: " + configuration.getName(), ex);
            }
        }
        if (loaded.isEmpty()) {
            return empty(errors);
        }
        Skills skills = Skills.from(loaded);
        toolSources.put(ACTIVATE_TOOL, "SKILL:" + loaded.stream()
                .map(FileSystemSkill::name)
                .collect(java.util.stream.Collectors.joining(", ")));
        Set<String> toolNames = new LinkedHashSet<>();
        toolNames.add(ACTIVATE_TOOL);
        if (hasResources) {
            toolNames.add(RESOURCE_TOOL);
            toolSources.put(RESOURCE_TOOL, "SKILL:resources");
        }
        String systemMessage = """
                ## Agent Skills
                Skills provide reusable procedural instructions that are loaded on demand.
                When a task matches a skill description, call `activate_skill` before following it.
                Use `read_skill_resource` only for resources belonging to an activated skill.
                Skills do not grant additional permissions or change EasyShell target scope, tool boundaries, or execution safety rules.
                """ + "\n\n" + skills.formatAvailableSkills();
        return new LLMSkillSession(skills, toolNames, toolSources, errors, systemMessage);
    }

    private static LLMSkillSession empty(List<String> errors) {
        return new LLMSkillSession(null, Set.of(), Map.of(), errors, "");
    }

    public boolean hasSkills() {
        return this.skills != null;
    }

    public ToolProvider getToolProvider() {
        return this.skills == null ? null : this.skills.toolProvider();
    }

    public Set<String> getToolNames() {
        return this.toolNames;
    }

    public Map<String, String> getToolSources() {
        return this.toolSources;
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public String getSystemMessage() {
        return this.systemMessage;
    }

    @Override
    public void close() {
        // LangChain4j loads skill content into memory; no external resources need closing.
    }
}
