package cn.oyzh.easyshell.http;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.domain.http.ShellHttpRequestScript;
import cn.oyzh.easyshell.domain.http.ShellHttpSetting;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.store.http.ShellHttpCollectionStore;
import cn.oyzh.easyshell.store.http.ShellHttpEnvironmentStore;
import cn.oyzh.easyshell.store.http.ShellHttpRequestScriptStore;
import cn.oyzh.easyshell.store.http.ShellHttpRequestStore;
import cn.oyzh.easyshell.store.http.ShellHttpWorkspaceStore;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * LLM tools bound to an HTTP workspace.
 */
public class ShellHttpLLMTools implements LLMTools {

    private static final int DEFAULT_MAX_RESULTS = 200;

    private static final int MAX_RESULTS = 1000;

    private static final int MAX_RESPONSE_CHARS = 64 * 1024;

    private static final Set<String> HTTP_METHODS = Set.of(
            ShellHttpRequest.METHOD_GET, ShellHttpRequest.METHOD_POST,
            ShellHttpRequest.METHOD_PUT, ShellHttpRequest.METHOD_PATCH,
            ShellHttpRequest.METHOD_DELETE, ShellHttpRequest.METHOD_HEAD,
            ShellHttpRequest.METHOD_OPTIONS);

    private static final Set<String> BODY_TYPES = Set.of(
            ShellHttpRequest.BODY_NONE, ShellHttpRequest.BODY_RAW,
            ShellHttpRequest.BODY_FORM, ShellHttpRequest.BODY_FORM_DATA,
            ShellHttpRequest.BODY_BINARY);

    private static final Set<String> AUTH_TYPES = Set.of(
            ShellHttpRequest.AUTH_NONE, ShellHttpRequest.AUTH_BASIC,
            ShellHttpRequest.AUTH_DIGEST, ShellHttpRequest.AUTH_OAUTH2,
            ShellHttpRequest.AUTH_BEARER, ShellHttpRequest.AUTH_API_KEY);

    private static final Set<String> AUTH_PLACEMENTS = Set.of(
            ShellHttpRequest.PLACEMENT_HEADER, ShellHttpRequest.PLACEMENT_QUERY);

    private ShellHttpClient client;

    private ShellHttpWorkspace workspace;

    private Supplier<ShellHttpWorkspace> workspaceSupplier;

    private BiConsumer<String, String> workspaceChangeListener;

    public void setClient(ShellHttpClient client) {
        this.client = client;
    }

    public void setWorkspace(ShellHttpWorkspace workspace) {
        this.workspace = workspace;
    }

    public void setWorkspaceSupplier(Supplier<ShellHttpWorkspace> workspaceSupplier) {
        this.workspaceSupplier = workspaceSupplier;
    }

    public void setWorkspaceChangeListener(BiConsumer<String, String> workspaceChangeListener) {
        this.workspaceChangeListener = workspaceChangeListener;
    }

    @Tool(name = "listCollections", value = "List saved HTTP request collections in the bound HTTP workspace")
    public String listCollections(
            @P(name = "filter", value = "Optional case-insensitive collection id or name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        String query = normalizeFilter(filter);
        List<String> values = new ArrayList<>();
        for (ShellHttpCollection collection : workspace.getCollections()) {
            if (collection == null) {
                continue;
            }
            String line = "id=" + safe(collection.getId())
                    + " | name=" + safe(collection.getName())
                    + " | requests=" + collection.requestCount();
            if (matches(query, line)) {
                values.add(line);
            }
        }
        return formatList("collection", values, maxResults);
    }

    @Tool(name = "listRequests", value = "List saved HTTP requests in the bound HTTP workspace")
    public String listRequests(
            @P(name = "collectionId", value = "Optional collection id; empty means all collections") String collectionId,
            @P(name = "filter", value = "Optional case-insensitive request id, name, method, URL, or collection filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        String requestedCollection = collectionId == null ? "" : collectionId.trim();
        boolean collectionFound = requestedCollection.isBlank();
        String query = normalizeFilter(filter);
        List<String> values = new ArrayList<>();
        for (ShellHttpCollection collection : workspace.getCollections()) {
            if (collection == null) {
                continue;
            }
            if (!requestedCollection.isBlank() && !requestedCollection.equals(collection.getId())) {
                continue;
            }
            collectionFound = true;
            for (ShellHttpRequest request : collection.getRequests()) {
                if (request == null) {
                    continue;
                }
                String line = "id=" + safe(request.getId())
                        + " | name=" + safe(request.getName())
                        + " | method=" + safe(request.normalizedMethod())
                        + " | collection=" + safe(collection.getName()) + " (" + safe(collection.getId()) + ")"
                        + " | url=" + safe(request.getUrl());
                if (matches(query, line)) {
                    values.add(line);
                }
            }
        }
        if (!collectionFound) {
            return "Collection not found: " + requestedCollection;
        }
        return formatList("request", values, maxResults);
    }

    @Tool(name = "getRequestDetails", value = "Get one complete saved HTTP request definition, including parameters, headers, body, authentication, transport settings, and request scripts")
    public String getRequestDetails(
            @P(name = "requestId", value = "Saved HTTP request id") String requestId) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        if (StringUtil.isBlank(requestId)) {
            return "Request id is required.";
        }
        ShellHttpRequest request = workspace.findRequest(requestId.trim());
        if (request == null) {
            return "Request not found: " + requestId;
        }
        return pretty(this.requestJson(request));
    }

    @Tool(name = "listEnvironments", value = "List all HTTP environments with their complete variable values and active state")
    public String listEnvironments() {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        JSONArray values = new JSONArray();
        for (ShellHttpEnvironment environment : workspace.getEnvironments()) {
            if (environment == null) {
                continue;
            }
            values.add(this.environmentJson(environment));
        }
        return pretty(values);
    }

    @Tool(name = "createCollection", value = "Create a new HTTP request collection in the bound workspace")
    public String createCollection(
            @P(name = "name", value = "Collection name") String name) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        if (StringUtil.isBlank(name)) {
            return "Collection name is required.";
        }
        ShellHttpCollection collection = new ShellHttpCollection();
        collection.setIid(workspace.getIid());
        collection.setName(name.trim());
        if (!ShellHttpCollectionStore.INSTANCE.insert(collection)) {
            return "Failed to create collection.";
        }
        workspace.getCollections().add(collection);
        this.notifyWorkspaceChanged("collection-created", collection.getId());
        return "Created collection: id=" + collection.getId() + " | name=" + collection.getName();
    }

    @Tool(name = "updateCollection", value = "Update a collection. Supports name in collectionJson; omitted fields stay unchanged")
    public String updateCollection(
            @P(name = "collectionId", value = "Collection id") String collectionId,
            @P(name = "collectionJson", value = "JSON object, for example {\"name\":\"Users\"}") String collectionJson) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        ShellHttpCollection collection = this.findCollection(collectionId);
        if (collection == null) {
            return "Collection not found: " + safe(collectionId);
        }
        String oldName = collection.getName();
        try {
            JSONObject json = parseJson(collectionJson, "collectionJson");
            if (json.containsKey("name")) {
                String name = json.getString("name");
                if (StringUtil.isBlank(name)) {
                    return "Collection name is required.";
                }
                collection.setName(name.trim());
            }
            if (!ShellHttpCollectionStore.INSTANCE.update(collection)) {
                collection.setName(oldName);
                return "Failed to update collection.";
            }
            this.notifyWorkspaceChanged("collection-updated", collection.getId());
            return "Updated collection: id=" + collection.getId() + " | name=" + collection.getName();
        } catch (Exception ex) {
            collection.setName(oldName);
            return "Failed to update collection: " + message(ex);
        }
    }

    @Tool(name = "deleteCollection", value = "Delete a collection together with all of its requests and request scripts")
    public String deleteCollection(
            @P(name = "collectionId", value = "Collection id") String collectionId) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        ShellHttpCollection collection = this.findCollection(collectionId);
        if (collection == null) {
            return "Collection not found: " + safe(collectionId);
        }
        for (ShellHttpRequest request : collection.getRequests()) {
            if (request != null) {
                ShellHttpRequestScriptStore.INSTANCE.deleteByRequestId(request.getId());
            }
        }
        ShellHttpRequestStore.INSTANCE.deleteByCollectionId(collection.getId());
        if (!ShellHttpCollectionStore.INSTANCE.delete(collection)) {
            return "Failed to delete collection.";
        }
        workspace.getCollections().remove(collection);
        this.notifyWorkspaceChanged("collection-deleted", collection.getId());
        return "Deleted collection: id=" + collection.getId() + " | requests=" + collection.requestCount();
    }

    @Tool(name = "createRequest", value = "Create a saved HTTP request in a collection. requestJson supports name, method, url, params, headers, formBody, bodyType, bodyContentType, body, authType, authUser, authPassword, authToken, authKeyName, authKeyValue, authKeyPlacement, oauth2TokenUrl, oauth2ClientId, oauth2ClientSecret, oauth2Scope, httpVersion, followRedirects, sendNoCacheHeader, timeoutSeconds, preRequestScript, testScript")
    public String createRequest(
            @P(name = "collectionId", value = "Target collection id") String collectionId,
            @P(name = "requestJson", value = "JSON object containing the request fields. name is required") String requestJson) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        ShellHttpCollection collection = this.findCollection(collectionId);
        if (collection == null) {
            return "Collection not found: " + safe(collectionId);
        }
        try {
            JSONObject json = parseJson(requestJson, "requestJson");
            ShellHttpRequest request = new ShellHttpRequest();
            request.setIid(workspace.getIid());
            request.setCollectionId(collection.getId());
            ShellHttpSetting settings = workspace.getSettings();
            request.setHttpVersion(settings.getHttpVersion());
            request.setFollowRedirects(settings.isFollowRedirects());
            request.setSendNoCacheHeader(settings.isSendNoCacheHeader());
            request.setTimeoutSeconds(settings.getTimeoutSeconds());
            applyRequestJson(request, json);
            request.setCollectionId(collection.getId());
            if (StringUtil.isBlank(request.getName())) {
                return "Request name is required.";
            }
            if (!ShellHttpRequestStore.INSTANCE.insert(request)) {
                return "Failed to create request.";
            }
            collection.getRequests().add(request);
            boolean scriptsSaved = this.saveScripts(request, json);
            this.notifyWorkspaceChanged("request-created", request.getId());
            String result = "Created request: id=" + request.getId()
                    + " | collection=" + collection.getId()
                    + " | name=" + request.getName()
                    + " | method=" + request.normalizedMethod();
            return scriptsSaved ? result : result + "\nWarning: request scripts could not be saved.";
        } catch (Exception ex) {
            return "Failed to create request: " + message(ex);
        }
    }

    @Tool(name = "updateRequest", value = "Partially update a saved HTTP request. Only fields present in requestJson are changed; arrays replace the whole list; null clears nullable fields. Supports name, collectionId, method, url, params, headers, formBody, bodyType, bodyContentType, body, authType, authUser, authPassword, authToken, authKeyName, authKeyValue, authKeyPlacement, oauth2TokenUrl, oauth2ClientId, oauth2ClientSecret, oauth2Scope, httpVersion, followRedirects, sendNoCacheHeader, timeoutSeconds, preRequestScript, testScript")
    public String updateRequest(
            @P(name = "requestId", value = "Saved HTTP request id") String requestId,
            @P(name = "requestJson", value = "JSON object containing fields to update") String requestJson) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        ShellHttpRequest request = workspace.findRequest(requestId);
        if (request == null) {
            return "Request not found: " + safe(requestId);
        }
        ShellHttpCollection oldCollection = this.findCollection(request.getCollectionId());
        ShellHttpRequest updated = request.copy();
        updated.setId(request.getId());
        updated.setIid(request.getIid());
        try {
            JSONObject json = parseJson(requestJson, "requestJson");
            applyRequestJson(updated, json);
            if (StringUtil.isBlank(updated.getName())) {
                return "Request name is required.";
            }
            ShellHttpCollection targetCollection = this.findCollection(updated.getCollectionId());
            if (targetCollection == null) {
                return "Collection not found: " + safe(updated.getCollectionId());
            }
            if (!ShellHttpRequestStore.INSTANCE.update(updated)) {
                return "Failed to update request.";
            }
            if (oldCollection == null) {
                targetCollection.getRequests().add(updated);
            } else if (!oldCollection.getId().equals(targetCollection.getId())) {
                oldCollection.getRequests().remove(request);
                targetCollection.getRequests().add(updated);
            } else {
                int index = oldCollection.getRequests().indexOf(request);
                if (index >= 0) {
                    oldCollection.getRequests().set(index, updated);
                }
            }
            boolean scriptsSaved = this.saveScripts(updated, json);
            this.notifyWorkspaceChanged("request-updated", updated.getId());
            String result = "Updated request: id=" + updated.getId()
                    + " | collection=" + updated.getCollectionId()
                    + " | name=" + updated.getName()
                    + " | method=" + updated.normalizedMethod();
            return scriptsSaved ? result : result + "\nWarning: request scripts could not be saved.";
        } catch (Exception ex) {
            return "Failed to update request: " + message(ex);
        }
    }

    @Tool(name = "deleteRequest", value = "Delete a saved HTTP request and its request scripts")
    public String deleteRequest(
            @P(name = "requestId", value = "Saved HTTP request id") String requestId) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        ShellHttpRequest request = workspace.findRequest(requestId);
        if (request == null) {
            return "Request not found: " + safe(requestId);
        }
        ShellHttpRequestScriptStore.INSTANCE.deleteByRequestId(request.getId());
        if (!ShellHttpRequestStore.INSTANCE.delete(request)) {
            return "Failed to delete request.";
        }
        ShellHttpCollection collection = this.findCollection(request.getCollectionId());
        if (collection != null) {
            collection.getRequests().remove(request);
        }
        this.notifyWorkspaceChanged("request-deleted", request.getId());
        return "Deleted request: id=" + request.getId() + " | name=" + request.getName();
    }

    @Tool(name = "createEnvironment", value = "Create an HTTP environment. environmentJson supports name and values; values is a JSON array of {key,value,enabled,description} objects")
    public String createEnvironment(
            @P(name = "environmentJson", value = "JSON object containing name and values") String environmentJson) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        try {
            JSONObject json = parseJson(environmentJson, "environmentJson");
            ShellHttpEnvironment environment = new ShellHttpEnvironment();
            environment.setIid(workspace.getIid());
            applyEnvironmentJson(environment, json);
            if (StringUtil.isBlank(environment.getName())) {
                return "Environment name is required.";
            }
            if (!ShellHttpEnvironmentStore.INSTANCE.insert(environment)) {
                return "Failed to create environment.";
            }
            workspace.getEnvironments().add(environment);
            this.notifyWorkspaceChanged("environment-created", environment.getId());
            return "Created environment: id=" + environment.getId() + " | name=" + environment.getName();
        } catch (Exception ex) {
            return "Failed to create environment: " + message(ex);
        }
    }

    @Tool(name = "updateEnvironment", value = "Partially update an HTTP environment. Supports name and values; values replaces the complete variable list")
    public String updateEnvironment(
            @P(name = "environmentId", value = "Environment id") String environmentId,
            @P(name = "environmentJson", value = "JSON object containing fields to update") String environmentJson) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        ShellHttpEnvironment environment = workspace.findEnvironment(environmentId);
        if (environment == null) {
            return "Environment not found: " + safe(environmentId);
        }
        ShellHttpEnvironment updated = environment.copy();
        updated.setId(environment.getId());
        updated.setIid(environment.getIid());
        try {
            JSONObject json = parseJson(environmentJson, "environmentJson");
            applyEnvironmentJson(updated, json);
            if (StringUtil.isBlank(updated.getName())) {
                return "Environment name is required.";
            }
            if (!ShellHttpEnvironmentStore.INSTANCE.update(updated)) {
                return "Failed to update environment.";
            }
            environment.setName(updated.getName());
            environment.setValues(ShellHttpRequest.copyRows(updated.getValues()));
            this.notifyWorkspaceChanged("environment-updated", environment.getId());
            return "Updated environment: id=" + environment.getId() + " | name=" + environment.getName();
        } catch (Exception ex) {
            return "Failed to update environment: " + message(ex);
        }
    }

    @Tool(name = "deleteEnvironment", value = "Delete an HTTP environment. If it is active, the workspace is switched to no environment")
    public String deleteEnvironment(
            @P(name = "environmentId", value = "Environment id") String environmentId) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        ShellHttpEnvironment environment = workspace.findEnvironment(environmentId);
        if (environment == null) {
            return "Environment not found: " + safe(environmentId);
        }
        boolean active = environment.getId() != null && environment.getId().equals(workspace.getActiveEnvironmentId());
        if (!ShellHttpEnvironmentStore.INSTANCE.delete(environment)) {
            return "Failed to delete environment.";
        }
        workspace.getEnvironments().remove(environment);
        if (active) {
            workspace.setActiveEnvironmentId(null);
            ShellHttpWorkspaceStore.INSTANCE.saveSetting(workspace);
        }
        this.notifyWorkspaceChanged("environment-deleted", environment.getId());
        return "Deleted environment: id=" + environment.getId() + " | name=" + environment.getName();
    }

    @Tool(name = "setActiveEnvironment", value = "Select the active HTTP environment. Pass an empty environmentId to clear the active environment")
    public String setActiveEnvironment(
            @P(name = "environmentId", value = "Environment id, or empty to clear") String environmentId) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        ShellHttpEnvironment environment = StringUtil.isBlank(environmentId)
                ? null : workspace.findEnvironment(environmentId.trim());
        if (StringUtil.isNotBlank(environmentId) && environment == null) {
            return "Environment not found: " + environmentId;
        }
        String previousId = workspace.getActiveEnvironmentId();
        workspace.setActiveEnvironmentId(environment == null ? null : environment.getId());
        if (!ShellHttpWorkspaceStore.INSTANCE.saveSetting(workspace)) {
            workspace.setActiveEnvironmentId(previousId);
            return "Failed to save active environment.";
        }
        this.notifyWorkspaceChanged("environment-active", environment == null ? null : environment.getId());
        return "Active environment: " + (environment == null ? "(none)" : environment.getName());
    }

    @Tool(name = "getSettings", value = "Get all HTTP workspace settings, including timeout, HTTP version, redirects, SSL verification, JSON formatting, no-cache header, maximum response size, and active environment")
    public String getSettings() {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        return pretty(this.settingsJson());
    }

    @Tool(name = "updateSettings", value = "Partially update HTTP workspace settings. Supports timeoutSeconds, httpVersion, followRedirects, verifySsl, prettyJson, sendNoCacheHeader, maxResponseSizeMb, activeEnvironmentId")
    public String updateSettings(
            @P(name = "settingsJson", value = "JSON object containing settings to update") String settingsJson) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        ShellHttpSetting previous = workspace.getSettings();
        String previousActiveId = workspace.getActiveEnvironmentId();
        try {
            JSONObject json = parseJson(settingsJson, "settingsJson");
            ShellHttpSetting updated = previous.copy();
            if (json.containsKey("timeoutSeconds")) {
                int timeout = json.getIntValue("timeoutSeconds");
                if (timeout <= 0) {
                    return "timeoutSeconds must be greater than 0.";
                }
                updated.setTimeoutSeconds(timeout);
            }
            if (json.containsKey("httpVersion")) {
                updated.setHttpVersion(normalizeHttpVersion(json.getString("httpVersion")));
            }
            if (json.containsKey("followRedirects") && json.get("followRedirects") != null) {
                updated.setFollowRedirects(booleanValue(json.get("followRedirects")));
            }
            if (json.containsKey("verifySsl") && json.get("verifySsl") != null) {
                updated.setVerifySsl(booleanValue(json.get("verifySsl")));
            }
            if (json.containsKey("prettyJson") && json.get("prettyJson") != null) {
                updated.setPrettyJson(booleanValue(json.get("prettyJson")));
            }
            if (json.containsKey("sendNoCacheHeader") && json.get("sendNoCacheHeader") != null) {
                updated.setSendNoCacheHeader(booleanValue(json.get("sendNoCacheHeader")));
            }
            if (json.containsKey("maxResponseSizeMb")) {
                int size = json.getIntValue("maxResponseSizeMb");
                if (size <= 0) {
                    return "maxResponseSizeMb must be greater than 0.";
                }
                updated.setMaxResponseSizeMb(size);
            }
            String activeId = previousActiveId;
            if (json.containsKey("activeEnvironmentId")) {
                activeId = StringUtil.blankToDefault(json.getString("activeEnvironmentId"), null);
                if (activeId != null && workspace.findEnvironment(activeId) == null) {
                    return "Environment not found: " + activeId;
                }
            }
            workspace.setSettings(updated);
            workspace.setActiveEnvironmentId(activeId);
            if (!ShellHttpWorkspaceStore.INSTANCE.saveSetting(workspace)) {
                workspace.setSettings(previous);
                workspace.setActiveEnvironmentId(previousActiveId);
                return "Failed to update settings.";
            }
            this.notifyWorkspaceChanged("settings-updated", null);
            return "Updated settings.\n" + pretty(this.settingsJson());
        } catch (Exception ex) {
            workspace.setSettings(previous);
            workspace.setActiveEnvironmentId(previousActiveId);
            return "Failed to update settings: " + message(ex);
        }
    }

    @Tool(name = "executeRequest", value = "Execute one saved HTTP request in the bound workspace and return the actual status, headers, and response body")
    public String executeRequest(
            @P(name = "requestId", value = "Saved HTTP request id") String requestId) {
        if (this.client == null) {
            return "HTTP client is unavailable.";
        }
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        if (StringUtil.isBlank(requestId)) {
            return "Request id is required.";
        }
        ShellHttpRequest request = workspace.findRequest(requestId.trim());
        if (request == null) {
            return "Request not found: " + requestId;
        }
        try {
            ShellHttpResponse response = this.client.execute(request.copy(), workspace);
            StringBuilder output = new StringBuilder();
            output.append("requestId: ").append(safe(request.getId())).append('\n');
            output.append("request: ").append(safe(request.getName())).append('\n');
            appendResponse(output, response);
            return output.toString();
        } catch (Exception ex) {
            return "Execution failed: " + message(ex);
        }
    }

    @Tool(name = "executeHttpRequest", value = "Send a temporary HTTP or HTTPS request without saving it. Any request headers may be supplied, including authentication headers")
    public String executeHttpRequest(
            @P(name = "method", value = "HTTP method: GET, POST, PUT, PATCH, DELETE, HEAD, or OPTIONS") String method,
            @P(name = "url", value = "HTTP or HTTPS URL. Workspace environment variables may be used") String url,
            @P(name = "headersJson", value = "Optional JSON object of request headers") String headersJson,
            @P(name = "body", value = "Optional request body") String body,
            @P(name = "contentType", value = "Optional request body content type") String contentType) {
        if (this.client == null) {
            return "HTTP client is unavailable.";
        }
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null) {
            return "HTTP workspace is unavailable.";
        }
        if (StringUtil.isBlank(url)) {
            return "URL is required.";
        }
        String normalizedMethod = StringUtil.blankToDefault(method, ShellHttpRequest.METHOD_GET)
                .trim().toUpperCase(Locale.ROOT);
        switch (normalizedMethod) {
            case ShellHttpRequest.METHOD_GET, ShellHttpRequest.METHOD_POST, ShellHttpRequest.METHOD_PUT,
                    ShellHttpRequest.METHOD_PATCH, ShellHttpRequest.METHOD_DELETE,
                    ShellHttpRequest.METHOD_HEAD, ShellHttpRequest.METHOD_OPTIONS -> {
            }
            default -> {
                return "Unsupported HTTP method: " + method;
            }
        }
        ShellHttpRequest request = new ShellHttpRequest();
        request.setMethod(normalizedMethod);
        request.setUrl(url.trim());
        if (StringUtil.isNotBlank(body)) {
            request.setBodyType(ShellHttpRequest.BODY_RAW);
            request.setBody(body);
            if (StringUtil.isNotBlank(contentType)) {
                request.setBodyContentType(contentType.trim());
            }
        }
        String headerError = this.applyHeaders(request, headersJson);
        if (headerError != null) {
            return headerError;
        }
        try {
            ShellHttpResponse response = this.client.execute(request, workspace);
            StringBuilder output = new StringBuilder();
            output.append("request: ").append(normalizedMethod).append(' ')
                    .append(safe(url)).append('\n');
            appendResponse(output, response);
            return output.toString();
        } catch (Exception ex) {
            return "Execution failed: " + message(ex);
        }
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("http_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }

    private void notifyWorkspaceChanged(String type, String id) {
        if (this.workspaceChangeListener != null) {
            this.workspaceChangeListener.accept(type, id);
        }
    }

    private ShellHttpCollection findCollection(String collectionId) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null || StringUtil.isBlank(collectionId)) {
            return null;
        }
        for (ShellHttpCollection collection : workspace.getCollections()) {
            if (collection != null && collectionId.trim().equals(collection.getId())) {
                return collection;
            }
        }
        return null;
    }

    private JSONObject requestJson(ShellHttpRequest request) {
        JSONObject json = new JSONObject();
        json.put("id", request.getId());
        json.put("collectionId", request.getCollectionId());
        json.put("collectionName", this.collectionName(request.getCollectionId()));
        json.put("name", request.getName());
        json.put("method", request.normalizedMethod());
        json.put("url", request.getUrl());
        json.put("params", rowsJson(request.getParams()));
        json.put("headers", rowsJson(request.getHeaders()));
        json.put("formBody", rowsJson(request.getFormBody()));
        json.put("bodyType", request.getBodyType());
        json.put("bodyContentType", request.getBodyContentType());
        json.put("body", request.getBody());
        json.put("authType", request.getAuthType());
        json.put("authUser", request.getAuthUser());
        json.put("authPassword", request.getAuthPassword());
        json.put("authToken", request.getAuthToken());
        json.put("authKeyName", request.getAuthKeyName());
        json.put("authKeyValue", request.getAuthKeyValue());
        json.put("authKeyPlacement", request.getAuthKeyPlacement());
        json.put("oauth2TokenUrl", request.getOauth2TokenUrl());
        json.put("oauth2ClientId", request.getOauth2ClientId());
        json.put("oauth2ClientSecret", request.getOauth2ClientSecret());
        json.put("oauth2Scope", request.getOauth2Scope());
        json.put("httpVersion", request.getHttpVersion());
        json.put("followRedirects", request.getFollowRedirects());
        json.put("sendNoCacheHeader", request.getSendNoCacheHeader());
        json.put("timeoutSeconds", request.getTimeoutSeconds());
        try {
            ShellHttpRequestScript script =
                    ShellHttpRequestScriptStore.INSTANCE.getByRequestId(request.getId());
            json.put("preRequestScript", script == null ? "" : script.getPreRequestScript());
            json.put("testScript", script == null ? "" : script.getTestScript());
        } catch (Exception ex) {
            json.put("preRequestScript", "");
            json.put("testScript", "");
        }
        return json;
    }

    private JSONObject environmentJson(ShellHttpEnvironment environment) {
        JSONObject json = new JSONObject();
        json.put("id", environment.getId());
        json.put("name", environment.getName());
        json.put("active", environment.getId() != null
                && environment.getId().equals(this.workspace().getActiveEnvironmentId()));
        json.put("values", rowsJson(environment.getValues()));
        return json;
    }

    private JSONObject settingsJson() {
        ShellHttpWorkspace workspace = this.workspace();
        ShellHttpSetting settings = workspace.getSettings();
        ShellHttpEnvironment active = workspace.activeEnvironment();
        JSONObject json = new JSONObject();
        json.put("timeoutSeconds", settings.getTimeoutSeconds());
        json.put("httpVersion", settings.getHttpVersion());
        json.put("followRedirects", settings.isFollowRedirects());
        json.put("verifySsl", settings.isVerifySsl());
        json.put("prettyJson", settings.isPrettyJson());
        json.put("sendNoCacheHeader", settings.isSendNoCacheHeader());
        json.put("maxResponseSizeMb", settings.getMaxResponseSizeMb());
        json.put("activeEnvironmentId", workspace.getActiveEnvironmentId());
        json.put("activeEnvironmentName", active == null ? null : active.getName());
        return json;
    }

    private static void applyRequestJson(ShellHttpRequest request, JSONObject json) {
        if (json.containsKey("collectionId")) {
            String collectionId = json.getString("collectionId");
            if (StringUtil.isBlank(collectionId)) {
                throw new IllegalArgumentException("collectionId cannot be blank.");
            }
            request.setCollectionId(collectionId.trim());
        }
        if (json.containsKey("name")) {
            request.setName(StringUtil.blankToDefault(json.getString("name"), ""));
        }
        if (json.containsKey("method")) {
            request.setMethod(normalizeChoice("method", json.getString("method"),
                    HTTP_METHODS, ShellHttpRequest.METHOD_GET));
        }
        if (json.containsKey("url")) {
            request.setUrl(StringUtil.blankToDefault(json.getString("url"), ""));
        }
        if (json.containsKey("params")) {
            request.setParams(parseRows(json.get("params"), "params"));
        }
        if (json.containsKey("headers")) {
            request.setHeaders(parseRows(json.get("headers"), "headers"));
        }
        if (json.containsKey("formBody")) {
            request.setFormBody(parseRows(json.get("formBody"), "formBody"));
        }
        if (json.containsKey("bodyType")) {
            request.setBodyType(normalizeChoice("bodyType", json.getString("bodyType"),
                    BODY_TYPES, ShellHttpRequest.BODY_NONE));
        }
        if (json.containsKey("bodyContentType")) {
            request.setBodyContentType(
                    StringUtil.blankToDefault(json.getString("bodyContentType"), "application/json"));
        }
        if (json.containsKey("body")) {
            request.setBody(StringUtil.blankToDefault(json.getString("body"), ""));
        }
        if (json.containsKey("authType")) {
            request.setAuthType(normalizeChoice("authType", json.getString("authType"),
                    AUTH_TYPES, ShellHttpRequest.AUTH_NONE));
        }
        if (json.containsKey("authUser")) {
            request.setAuthUser(StringUtil.blankToDefault(json.getString("authUser"), ""));
        }
        if (json.containsKey("authPassword")) {
            request.setAuthPassword(StringUtil.blankToDefault(json.getString("authPassword"), ""));
        }
        if (json.containsKey("authToken")) {
            request.setAuthToken(StringUtil.blankToDefault(json.getString("authToken"), ""));
        }
        if (json.containsKey("authKeyName")) {
            request.setAuthKeyName(StringUtil.blankToDefault(json.getString("authKeyName"), "X-API-Key"));
        }
        if (json.containsKey("authKeyValue")) {
            request.setAuthKeyValue(StringUtil.blankToDefault(json.getString("authKeyValue"), ""));
        }
        if (json.containsKey("authKeyPlacement")) {
            request.setAuthKeyPlacement(normalizeChoice("authKeyPlacement",
                    json.getString("authKeyPlacement"), AUTH_PLACEMENTS,
                    ShellHttpRequest.PLACEMENT_HEADER));
        }
        if (json.containsKey("oauth2TokenUrl")) {
            request.setOauth2TokenUrl(StringUtil.blankToDefault(json.getString("oauth2TokenUrl"), ""));
        }
        if (json.containsKey("oauth2ClientId")) {
            request.setOauth2ClientId(StringUtil.blankToDefault(json.getString("oauth2ClientId"), ""));
        }
        if (json.containsKey("oauth2ClientSecret")) {
            request.setOauth2ClientSecret(
                    StringUtil.blankToDefault(json.getString("oauth2ClientSecret"), ""));
        }
        if (json.containsKey("oauth2Scope")) {
            request.setOauth2Scope(StringUtil.blankToDefault(json.getString("oauth2Scope"), ""));
        }
        if (json.containsKey("httpVersion")) {
            request.setHttpVersion(normalizeHttpVersion(json.getString("httpVersion")));
        }
        if (json.containsKey("followRedirects")) {
            request.setFollowRedirects(optionalBoolean(json.get("followRedirects")));
        }
        if (json.containsKey("sendNoCacheHeader")) {
            request.setSendNoCacheHeader(optionalBoolean(json.get("sendNoCacheHeader")));
        }
        if (json.containsKey("timeoutSeconds")) {
            Object value = json.get("timeoutSeconds");
            if (value == null) {
                request.setTimeoutSeconds(null);
            } else {
                int timeout = json.getIntValue("timeoutSeconds");
                if (timeout <= 0) {
                    throw new IllegalArgumentException("timeoutSeconds must be greater than 0.");
                }
                request.setTimeoutSeconds(timeout);
            }
        }
    }

    private static void applyEnvironmentJson(ShellHttpEnvironment environment, JSONObject json) {
        if (json.containsKey("name")) {
            environment.setName(StringUtil.blankToDefault(json.getString("name"), ""));
        }
        if (json.containsKey("values")) {
            environment.setValues(parseRows(json.get("values"), "values"));
        }
    }

    private boolean saveScripts(ShellHttpRequest request, JSONObject json) {
        if (!json.containsKey("preRequestScript") && !json.containsKey("testScript")) {
            return true;
        }
        try {
            ShellHttpRequestScript script =
                    ShellHttpRequestScriptStore.INSTANCE.getByRequestId(request.getId());
            if (script == null) {
                script = new ShellHttpRequestScript();
                script.setIid(request.getIid());
                script.setRequestId(request.getId());
            }
            if (json.containsKey("preRequestScript")) {
                script.setPreRequestScript(json.getString("preRequestScript"));
            }
            if (json.containsKey("testScript")) {
                script.setTestScript(json.getString("testScript"));
            }
            return ShellHttpRequestScriptStore.INSTANCE.replace(script);
        } catch (Exception ex) {
            return false;
        }
    }

    private static List<ShellHttpKeyValue> parseRows(Object value, String field) {
        List<ShellHttpKeyValue> rows = new ArrayList<>();
        if (value == null) {
            return rows;
        }
        if (value instanceof JSONObject object) {
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                rows.add(new ShellHttpKeyValue(entry.getKey(),
                        entry.getValue() == null ? "" : String.valueOf(entry.getValue())));
            }
            return rows;
        }
        if (!(value instanceof JSONArray array)) {
            throw new IllegalArgumentException(field + " must be a JSON array or object.");
        }
        for (Object item : array) {
            if (item == null) {
                continue;
            }
            if (!(item instanceof JSONObject row)) {
                throw new IllegalArgumentException(field + " entries must be JSON objects.");
            }
            ShellHttpKeyValue valueRow = new ShellHttpKeyValue(
                    StringUtil.blankToDefault(row.getString("key"), ""),
                    StringUtil.blankToDefault(row.getString("value"), ""));
            valueRow.setEnabled(!row.containsKey("enabled") || booleanValue(row.get("enabled")));
            valueRow.setFile(booleanValue(row.get("file")));
            valueRow.setDescription(StringUtil.blankToDefault(row.getString("description"), ""));
            rows.add(valueRow);
        }
        return rows;
    }

    private static JSONArray rowsJson(List<ShellHttpKeyValue> rows) {
        JSONArray array = new JSONArray();
        if (rows != null) {
            for (ShellHttpKeyValue row : rows) {
                if (row == null) {
                    continue;
                }
                JSONObject json = new JSONObject();
                json.put("key", row.getKey());
                json.put("value", row.getValue());
                json.put("enabled", row.isEnabled());
                json.put("file", row.isFile());
                json.put("description", row.getDescription());
                array.add(json);
            }
        }
        return array;
    }

    private static JSONObject parseJson(String value, String field) {
        if (StringUtil.isBlank(value)) {
            throw new IllegalArgumentException(field + " is required.");
        }
        try {
            JSONObject json = JSONUtil.parseObject(value);
            if (json == null) {
                throw new IllegalArgumentException("Invalid " + field + ": JSON object is required.");
            }
            return json;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid " + field + ": " + message(ex));
        }
    }

    private static String normalizeChoice(String field, String value, Set<String> allowed,
                                          String defaultValue) {
        String normalized = StringUtil.blankToDefault(value, defaultValue).trim();
        for (String item : allowed) {
            if (item.equalsIgnoreCase(normalized)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unsupported " + field + ": " + value);
    }

    private static String normalizeHttpVersion(String value) {
        String normalized = StringUtil.blankToDefault(value, ShellHttpSetting.VERSION_HTTP_1_1).trim();
        if ("HTTP/2".equalsIgnoreCase(normalized)
                || "HTTP_2".equalsIgnoreCase(normalized)
                || "2".equals(normalized)) {
            return ShellHttpSetting.VERSION_HTTP_2;
        }
        if ("HTTP/1.1".equalsIgnoreCase(normalized)
                || "HTTP_1_1".equalsIgnoreCase(normalized)
                || "1.1".equals(normalized)) {
            return ShellHttpSetting.VERSION_HTTP_1_1;
        }
        throw new IllegalArgumentException("Unsupported httpVersion: " + value);
    }

    private static Boolean optionalBoolean(Object value) {
        return value == null ? null : booleanValue(value);
    }

    private static boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value == null ? "" : String.valueOf(value).trim();
        return "1".equals(text) || Boolean.parseBoolean(text);
    }

    private static String pretty(Object value) {
        return JSONObject.toJSONString(value, JSONWriter.Feature.PrettyFormat);
    }

    private void appendResponse(StringBuilder output, ShellHttpResponse response) {
        output.append("statusCode: ").append(response.getStatusCode()).append('\n');
        output.append("statusText: ").append(safe(response.getStatusText())).append('\n');
        output.append("successful: ").append(response.isSuccessful()).append('\n');
        output.append("uri: ").append(response.getUri() == null ? "" : response.getUri().toString()).append('\n');
        output.append("durationMs: ").append(response.getDurationMs()).append('\n');
        output.append("size: ").append(response.getSize()).append(" (")
                .append(ShellHttpUtil.humanSize(response.getSize())).append(")\n");
        output.append("contentType: ").append(safe(response.getContentType())).append('\n');
        output.append("binary: ").append(response.isBinary()).append('\n');
        output.append("bodyTooLarge: ").append(response.isBodyTooLarge()).append('\n');
        appendHeaders(output, response.getHeaders());
        if (response.isBodyTooLarge()) {
            output.append("body: [omitted: response exceeds the configured maximum size]\n");
        } else if (response.isBinary()) {
            output.append("body: [binary response omitted; bytes=").append(response.getSize());
            if (StringUtil.isNotBlank(response.getTempFile())) {
                output.append("; tempFile=").append(response.getTempFile());
            }
            output.append("]\n");
        } else {
            String body = response.getBody();
            if (body == null || body.isEmpty()) {
                output.append("body: (empty)\n");
            } else {
                String display = body.length() <= MAX_RESPONSE_CHARS
                        ? ShellHttpUtil.formatResponseBody(body, response.getContentType(), true)
                        : body;
                output.append("body:\n").append(truncate(display, MAX_RESPONSE_CHARS)).append('\n');
            }
        }
    }

    private static void appendHeaders(StringBuilder output, Map<String, List<String>> headers) {
        TreeMap<String, List<String>> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (headers != null) {
            sorted.putAll(headers);
        }
        output.append("headers:\n");
        if (sorted.isEmpty()) {
            output.append("  (none)\n");
            return;
        }
        for (Map.Entry<String, List<String>> entry : sorted.entrySet()) {
            String value = String.join(" | ", entry.getValue() == null ? List.of() : entry.getValue());
            output.append("  ").append(entry.getKey()).append(": ").append(value).append('\n');
        }
    }

    private String applyHeaders(ShellHttpRequest request, String headersJson) {
        if (StringUtil.isBlank(headersJson)) {
            return null;
        }
        JSONObject headers;
        try {
            headers = JSONUtil.parseObject(headersJson);
        } catch (Exception ex) {
            return "Invalid headers JSON: " + message(ex);
        }
        if (headers == null) {
            return "Invalid headers JSON.";
        }
        for (String key : headers.keySet()) {
            if (StringUtil.isBlank(key)) {
                continue;
            }
            request.getHeaders().add(new ShellHttpKeyValue(key, headers.getString(key)));
        }
        return null;
    }

    private String collectionName(String collectionId) {
        ShellHttpWorkspace workspace = this.workspace();
        if (workspace == null || StringUtil.isBlank(collectionId)) {
            return "";
        }
        for (ShellHttpCollection collection : workspace.getCollections()) {
            if (collection != null && collectionId.equals(collection.getId())) {
                return safe(collection.getName());
            }
        }
        return "";
    }

    private ShellHttpWorkspace workspace() {
        if (this.workspace != null) {
            return this.workspace;
        }
        return this.workspaceSupplier == null ? null : this.workspaceSupplier.get();
    }

    private static String formatList(String label, List<String> values, int maxResults) {
        int limit = maxResults <= 0 ? DEFAULT_MAX_RESULTS : Math.min(maxResults, MAX_RESULTS);
        int shown = Math.min(values.size(), limit);
        StringBuilder output = new StringBuilder();
        output.append("count: ").append(values.size()).append('\n');
        output.append("shown: ").append(shown).append('\n');
        for (int index = 0; index < shown; index++) {
            output.append(label).append(": ").append(values.get(index)).append('\n');
        }
        if (values.size() > shown) {
            output.append("... ").append(values.size() - shown).append(" more omitted\n");
        }
        return output.toString();
    }

    private static String normalizeFilter(String filter) {
        return filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean matches(String query, String value) {
        return query.isBlank() || value.toLowerCase(Locale.ROOT).contains(query);
    }

    private static String truncate(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + "\n...[truncated " + (value.length() - maxChars) + " chars]";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String message(Exception ex) {
        String message = ex.getMessage();
        return StringUtil.isBlank(message) ? ex.getClass().getSimpleName() : message;
    }
}
