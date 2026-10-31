package cn.oyzh.easyshell.llm;

import java.util.Objects;

/**
 * Built-in LLM provider connection template.
 *
 * @author oyzh
 * @since 2026-10-31
 */
public final class LLMProviderTemplate {

    private final String id;

    private final String name;

    private final String baseUrl;

    private final LLMApiType apiType;

    public LLMProviderTemplate(String id, String name, String baseUrl, LLMApiType apiType) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Template id cannot be empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Template name cannot be empty");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Template base url cannot be empty");
        }
        this.id = id;
        this.name = name.trim();
        this.baseUrl = normalizeUrl(baseUrl);
        this.apiType = Objects.requireNonNull(apiType, "apiType");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public LLMApiType getApiType() {
        return apiType;
    }

    public String getApiTypeName() {
        return this.apiType.name();
    }

    /**
     * Display name includes the API type when it adds useful context.
     *
     * @return display name
     */
    public String getDisplayName() {
        if (this.name.equalsIgnoreCase(this.apiType.name())) {
            return this.name;
        }
        return this.name + " · " + this.apiType.name();
    }

    public boolean matches(String baseUrl, LLMApiType apiType) {
        return apiType == this.apiType && baseUrl != null
                && normalizeUrl(baseUrl).equalsIgnoreCase(this.baseUrl);
    }

    private static String normalizeUrl(String url) {
        String result = url.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LLMProviderTemplate that)) {
            return false;
        }
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return this.getDisplayName();
    }
}
