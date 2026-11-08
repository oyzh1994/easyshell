package cn.oyzh.easyshell.tabs.http.request;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.easyshell.fx.http.ShellHttpFormDataTableView;
import cn.oyzh.easyshell.fx.http.ShellHttpKeyValueTableView;
import cn.oyzh.easyshell.fx.http.ShellHttpResponseHeadersTableView;
import cn.oyzh.easyshell.http.ShellHttpClient;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.domain.http.ShellHttpRequestScript;
import cn.oyzh.easyshell.domain.http.ShellHttpSetting;
import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.easyshell.http.ShellHttpResponse;
import cn.oyzh.easyshell.http.ShellHttpUtil;
import cn.oyzh.easyshell.http.ShellHttpWorkspace;
import cn.oyzh.easyshell.http.script.ShellHttpScriptContext;
import cn.oyzh.easyshell.http.script.ShellHttpScriptEngine;
import cn.oyzh.easyshell.store.http.ShellHttpRequestScriptStore;
import cn.oyzh.easyshell.store.http.ShellHttpRequestStore;
import cn.oyzh.easyshell.tabs.http.ShellHttpTabPane;
import cn.oyzh.easyshell.trees.http.request.ShellHttpRequestTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.svg.glyph.llm.SendSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.hex.HexView;
import cn.oyzh.fx.plus.controls.tab.FixedTab;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyHandler;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.function.Function;

/**
 * HTTP 请求标签页控制器。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpRequestTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * 请求方法
     */
    @FXML
    private FXComboBox<String> method;

    /**
     * 请求地址
     */
    @FXML
    private ClearableTextField requestUrl;

    /**
     * 发送按钮
     */
    @FXML
    private SendSVGGlyph send;

    /**
     * 查询参数表格
     */
    @FXML
    private ShellHttpKeyValueTableView paramsTable;

    /**
     * 请求头表格
     */
    @FXML
    private ShellHttpKeyValueTableView headersTable;

    /**
     * 表单请求体表格
     */
    @FXML
    private ShellHttpKeyValueTableView formBodyTable;

    /**
     * form-data 请求体容器
     */
    @FXML
    private FXVBox formDataBodyBox;

    /**
     * form-data 表格
     */
    @FXML
    private ShellHttpFormDataTableView formDataTable;

    /**
     * 请求体类型
     */
    @FXML
    private FXComboBox<String> bodyType;

    /**
     * 原始请求体内容类型容器
     */
    @FXML
    private FXHBox rawContentTypeBox;

    /**
     * 原始请求体内容类型
     */
    @FXML
    private FXComboBox<String> rawContentType;

    /**
     * 二进制请求体容器
     */
    @FXML
    private FXHBox binaryBodyBox;

    /**
     * 二进制文件
     */
    @FXML
    private ChooseFileTextField bodyFile;

    /**
     * 请求体编辑器
     */
    @FXML
    private ShellDataEditor requestBody;

    /**
     * 原始请求体容器
     */
    @FXML
    private FXHBox rawBodyBox;

    /**
     * 表单请求体容器
     */
    @FXML
    private FXVBox formBodyBox;

    /**
     * 认证类型
     */
    @FXML
    private FXComboBox<String> authType;

    /**
     * Basic 认证容器
     */
    @FXML
    private FXVBox basicAuthBox;

    /**
     * Bearer 认证容器
     */
    @FXML
    private FXHBox bearerAuthBox;

    /**
     * API Key 认证容器
     */
    @FXML
    private FXVBox apiKeyBox;

    /**
     * Basic 用户名
     */
    @FXML
    private ClearableTextField authUser;

    /**
     * Basic 密码
     */
    @FXML
    private PasswordTextField authPassword;

    /**
     * Bearer Token
     */
    @FXML
    private PasswordTextField authToken;

    /**
     * API Key 名称
     */
    @FXML
    private ClearableTextField authKeyName;

    /**
     * API Key 值
     */
    @FXML
    private PasswordTextField authKeyValue;

    /**
     * API Key 位置
     */
    @FXML
    private FXComboBox<String> authKeyPlacement;

    /**
     * 请求 HTTP 版本
     */
    @FXML
    private FXComboBox<String> requestHttpVersion;

    /**
     * 请求是否跟随重定向
     */
    @FXML
    private FXCheckBox requestFollowRedirects;

    /**
     * 请求是否发送 no-cache 请求头
     */
    @FXML
    private FXCheckBox requestNoCacheHeader;

    /**
     * 请求超时秒数
     */
    @FXML
    private NumberTextField requestTimeout;

    /**
     * 响应状态
     */
    @FXML
    private FXLabel responseStatus;

    /**
     * 响应耗时
     */
    @FXML
    private FXLabel responseTime;

    /**
     * 响应大小
     */
    @FXML
    private FXLabel responseSize;

    /**
     * 响应体显示格式
     */
    @FXML
    private FXComboBox<String> responseFormat;

    /**
     * 响应头表格模式
     */
    @FXML
    private FXToggleSwitch responseHeadersTableMode;

    /**
     * 响应头表格
     */
    @FXML
    private ShellHttpResponseHeadersTableView responseHeadersTable;

    /**
     * 响应正文编辑器
     */
    @FXML
    private ShellDataEditor responseBody;

    /**
     * 响应头编辑器
     */
    @FXML
    private ShellDataEditor responseHeaders;

    /**
     * 响应标签页面板
     */
    @FXML
    private RichTabPane responseTabPane;

    /**
     * Base64 标签页
     */
    @FXML
    private FixedTab responseBase64Tab;

    /**
     * HEX 标签页
     */
    @FXML
    private FixedTab responseHexTab;

    /**
     * Base64 正文
     */
    @FXML
    private ShellDataEditor responseBase64;

    /**
     * HEX 正文
     */
    @FXML
    private HexView responseHex;

    /**
     * Base64 是否已加载
     */
    private boolean base64Loaded;

    /**
     * HEX 是否已加载
     */
    private boolean hexLoaded;

    /**
     * 预览按钮
     */
    @FXML
    private FXButton preview;

    /**
     * 保存响应正文
     */
    @FXML
    private FXButton saveResponseBody;

    /**
     * 打开响应文件
     */
    @FXML
    private FXButton openResponseFile;

    /**
     * 请求前脚本
     */
    @FXML
    private ShellDataEditor preRequestScript;

    /**
     * 请求脚本数据
     */
    private ShellHttpRequestScript requestScript;

    /**
     * 响应原始正文
     */
    private String responseBodyRaw = "";

    /**
     * 请求节点
     */
    private ShellHttpRequestTreeItem item;

    /**
     * 请求
     */
    private ShellHttpRequest request;

    /**
     * 标签页面板
     */
    private ShellHttpTabPane tabPane;

    /**
     * 工作区
     */
    private ShellHttpWorkspace workspace;

    /**
     * 当前响应
     */
    private ShellHttpResponse response;

    /**
     * 是否正在加载
     */
    private boolean loading;

    /**
     * 是否有未保存修改
     */
    private boolean unsaved;

    /**
     * 正在由参数同步地址
     */
    private boolean syncingUrl;

    /**
     * 正在由地址同步参数
     */
    private boolean syncingParams;

    /**
     * 已监听变更的参数行
     */
    private final Set<ShellHttpKeyValue> paramRows =
            Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * 初始化
     *
     * @param item    请求节点
     * @param tabPane 标签页面板
     */
    public void init(ShellHttpRequestTreeItem item, ShellHttpTabPane tabPane) {
        this.item = item;
        this.request = item.request();
        this.tabPane = tabPane;
        this.workspace = tabPane.getWorkspace();
        this.requestScript = ShellHttpRequestScriptStore.INSTANCE.getByRequestId(this.request.getId());
        if (this.requestScript == null) {
            this.requestScript = new ShellHttpRequestScript();
            this.requestScript.setIid(this.request.getIid());
            this.requestScript.setRequestId(this.request.getId());
        }
        this.loading = true;
        this.initCombos();
        this.initTables();
        this.loadRequest();
        this.initListeners();
        this.bindKeys();
        this.loading = false;
    }

    /**
     * 初始化下拉框
     */
    private void initCombos() {
        this.method.getItems().setAll(
                ShellHttpRequest.METHOD_GET, ShellHttpRequest.METHOD_POST,
                ShellHttpRequest.METHOD_PUT, ShellHttpRequest.METHOD_PATCH,
                ShellHttpRequest.METHOD_DELETE, ShellHttpRequest.METHOD_HEAD,
                ShellHttpRequest.METHOD_OPTIONS);
        this.rawContentType.getItems().setAll(
                "application/json", "application/xml", "text/plain", "text/html",
                "application/javascript", "application/x-www-form-urlencoded");
        this.responseFormat.getItems().setAll(
                "Text", "JSON", "XML", "HTML", "CSS", "JavaScript", "Markdown");
        this.responseFormat.setValue("Text");
        this.displayCombo(this.bodyType,
                List.of(ShellHttpRequest.BODY_NONE, ShellHttpRequest.BODY_FORM_DATA,
                        ShellHttpRequest.BODY_FORM, ShellHttpRequest.BODY_RAW,
                        ShellHttpRequest.BODY_BINARY),
                this::bodyTypeLabel);
        this.displayCombo(this.authType,
                List.of(ShellHttpRequest.AUTH_NONE, ShellHttpRequest.AUTH_BASIC,
                        ShellHttpRequest.AUTH_BEARER, ShellHttpRequest.AUTH_API_KEY),
                this::authTypeLabel);
        this.displayCombo(this.authKeyPlacement,
                List.of(ShellHttpRequest.PLACEMENT_HEADER, ShellHttpRequest.PLACEMENT_QUERY),
                this::placementLabel);
        this.requestHttpVersion.getItems().setAll(
                ShellHttpSetting.VERSION_HTTP_1_1, ShellHttpSetting.VERSION_HTTP_2);
    }

    /**
     * 初始化表格
     */
    private void initTables() {
        this.headersTable.setChangeAction(this::scheduleSave);
        this.formBodyTable.setChangeAction(this::scheduleSave);
        this.formDataTable.setChangeAction(this::scheduleSave);
    }

    /**
     * 加载请求
     */
    private void loadRequest() {
        this.method.setValue(StringUtil.blankToDefault(this.request.getMethod(), ShellHttpRequest.METHOD_GET));
        this.requestUrl.setText(this.request.getUrl());
        this.paramsTable.setItems(FXCollections.observableArrayList(this.request.getParams()));
        this.bindParamsChange();
        this.syncInitialUrlAndParams();
        this.headersTable.setItems(FXCollections.observableArrayList(this.request.getHeaders()));
        ObservableList<ShellHttpKeyValue> formItems =
                FXCollections.observableArrayList(this.request.getFormBody());
        this.formBodyTable.setItems(formItems);
        this.formDataTable.setItems(formItems);
        this.bodyType.setValue(StringUtil.blankToDefault(this.request.getBodyType(), ShellHttpRequest.BODY_NONE));
        this.rawContentType.setValue(StringUtil.blankToDefault(
                this.request.getBodyContentType(), "application/json"));
        boolean binary = ShellHttpRequest.BODY_BINARY.equalsIgnoreCase(this.request.getBodyType());
        this.bodyFile.setText(binary
                ? StringUtil.blankToDefault(this.request.getBody(), "") : "");
        this.requestBody.text(binary ? "" : StringUtil.blankToDefault(this.request.getBody(), ""));
        this.preRequestScript.setFormatType(EditorFormatType.JAVASCRIPT);
        this.preRequestScript.text(this.requestScript.getPreRequestScript());
        this.authType.setValue(StringUtil.blankToDefault(this.request.getAuthType(), ShellHttpRequest.AUTH_NONE));
        this.authUser.setText(StringUtil.blankToDefault(this.request.getAuthUser(), ""));
        this.authPassword.setText(StringUtil.blankToDefault(this.request.getAuthPassword(), ""));
        this.authToken.setText(StringUtil.blankToDefault(this.request.getAuthToken(), ""));
        this.authKeyName.setText(StringUtil.blankToDefault(this.request.getAuthKeyName(), "X-API-Key"));
        this.authKeyValue.setText(StringUtil.blankToDefault(this.request.getAuthKeyValue(), ""));
        this.authKeyPlacement.setValue(StringUtil.blankToDefault(
                this.request.getAuthKeyPlacement(), ShellHttpRequest.PLACEMENT_HEADER));
        ShellHttpSetting global = this.workspace.getSettings();
        this.requestHttpVersion.setValue(StringUtil.blankToDefault(
                this.request.getHttpVersion(), global.getHttpVersion()));
        this.requestFollowRedirects.setSelected(this.request.getFollowRedirects() == null
                ? global.isFollowRedirects() : this.request.getFollowRedirects());
        this.requestNoCacheHeader.setSelected(this.request.getSendNoCacheHeader() == null
                ? global.isSendNoCacheHeader() : this.request.getSendNoCacheHeader());
        this.requestTimeout.setText(String.valueOf(this.request.getTimeoutSeconds() == null
                ? global.getTimeoutSeconds() : this.request.getTimeoutSeconds()));
        this.responseStatus.text("-");
        this.responseTime.text("-");
        this.responseSize.text("-");
        this.responseFormat.setDisable(true);
        this.responseBodyRaw = "";
        this.responseBody.text("");
        this.responseHeaders.text("");
        this.responseHeadersTable.getItems().clear();
        this.updateResponseHeadersMode();
        this.preview.setDisable(true);
        this.saveResponseBody.setDisable(true);
        this.openResponseFile.setDisable(true);
        this.resetResponseExtraTabs();
        this.updateBodyEditor();
        this.updateAuthEditor();
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
        this.requestUrl.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.loading && !this.syncingUrl) {
                this.request.setUrl(newValue);
                this.syncParamsFromUrl(newValue);
                this.scheduleSave();
            }
        });
        this.method.selectedItemChanged((observable, oldValue, newValue) -> {
            if (this.loading) {
                return;
            }
            this.request.setMethod(StringUtil.blankToDefault(newValue, ShellHttpRequest.METHOD_GET));
            if (this.item != null) {
                this.item.refreshRequest();
            }
            this.scheduleSave();
        });
        this.requestHttpVersion.selectedItemChanged(
                (observable, oldValue, newValue) -> this.onRequestSettingChanged());
        this.requestFollowRedirects.selectedProperty().addListener(
                (observable, oldValue, newValue) -> this.onRequestSettingChanged());
        this.requestNoCacheHeader.selectedProperty().addListener(
                (observable, oldValue, newValue) -> this.onRequestSettingChanged());
        this.requestTimeout.textProperty().addListener(
                (observable, oldValue, newValue) -> this.onRequestSettingChanged());
        this.bodyType.selectedItemChanged((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setBodyType(newValue);
                this.updateBodyEditor();
                this.scheduleSave();
            }
        });
        this.rawContentType.selectedItemChanged((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setBodyContentType(newValue);
                this.scheduleSave();
            }
        });
        this.authType.selectedItemChanged((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setAuthType(newValue);
                this.updateAuthEditor();
                this.scheduleSave();
            }
        });
        this.authKeyPlacement.selectedItemChanged((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setAuthKeyPlacement(newValue);
                this.scheduleSave();
            }
        });
        this.authUser.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setAuthUser(newValue);
                this.scheduleSave();
            }
        });
        this.authPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setAuthPassword(newValue);
                this.scheduleSave();
            }
        });
        this.authToken.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setAuthToken(newValue);
                this.scheduleSave();
            }
        });
        this.authKeyName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setAuthKeyName(newValue);
                this.scheduleSave();
            }
        });
        this.authKeyValue.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setAuthKeyValue(newValue);
                this.scheduleSave();
            }
        });
        this.requestBody.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setBody(newValue);
                this.scheduleSave();
            }
        });
        this.bodyFile.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.request.setBody(newValue);
                this.scheduleSave();
            }
        });
        this.preRequestScript.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.loading) {
                this.requestScript.setPreRequestScript(newValue);
                this.scheduleSave();
            }
        });
        this.responseFormat.selectedItemChanged((observable, oldValue, newValue) -> this.renderResponseBody());
        this.responseTabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTab, newTab) -> this.loadExtraResponseTab(newTab));
        this.responseHeadersTableMode.selectedProperty().addListener(
                (observable, oldValue, newValue) -> this.updateResponseHeadersMode());
    }

    /**
     * 绑定快捷键
     */
    private void bindKeys() {
        KeyHandler saveHandler = new KeyHandler();
        saveHandler.setHandler(e -> this.saveRequest());
        saveHandler.setKeyCode(KeyCode.S);
        saveHandler.setMainModifierDown(true);
        saveHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.root, saveHandler);

        KeyHandler sendHandler = new KeyHandler();
        sendHandler.setHandler(e -> this.sendRequest());
        sendHandler.setKeyCode(KeyCode.ENTER);
        sendHandler.setMainModifierDown(true);
        sendHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.root, sendHandler);
    }

    /**
     * 刷新请求体编辑器
     */
    private void updateBodyEditor() {
        String type = this.bodyType.getValue();
        boolean raw = ShellHttpRequest.BODY_RAW.equalsIgnoreCase(type);
        boolean form = ShellHttpRequest.BODY_FORM.equalsIgnoreCase(type);
        boolean formData = ShellHttpRequest.BODY_FORM_DATA.equalsIgnoreCase(type);
        boolean binary = ShellHttpRequest.BODY_BINARY.equalsIgnoreCase(type);
        this.rawBodyBox.setVisible(raw);
        this.rawBodyBox.setManaged(raw);
        this.rawContentTypeBox.setVisible(raw);
        this.rawContentTypeBox.setManaged(raw);
        this.formBodyBox.setVisible(form);
        this.formBodyBox.setManaged(form);
        this.formDataBodyBox.setVisible(formData);
        this.formDataBodyBox.setManaged(formData);
        this.binaryBodyBox.setVisible(binary);
        this.binaryBodyBox.setManaged(binary);
    }

    /**
     * 刷新认证编辑器
     */
    private void updateAuthEditor() {
        String type = this.authType.getValue();
        boolean basic = ShellHttpRequest.AUTH_BASIC.equalsIgnoreCase(type);
        boolean bearer = ShellHttpRequest.AUTH_BEARER.equalsIgnoreCase(type);
        boolean apiKey = ShellHttpRequest.AUTH_API_KEY.equalsIgnoreCase(type);
        this.basicAuthBox.setVisible(basic);
        this.basicAuthBox.setManaged(basic);
        this.bearerAuthBox.setVisible(bearer);
        this.bearerAuthBox.setManaged(bearer);
        this.apiKeyBox.setVisible(apiKey);
        this.apiKeyBox.setManaged(apiKey);
    }

    /**
     * 监听参数表格的增删和内容变化。
     */
    private void bindParamsChange() {
        for (ShellHttpKeyValue row : this.paramsTable.getItems()) {
            this.observeParamRow(row);
        }
        this.paramsTable.getItems().addListener((ListChangeListener<ShellHttpKeyValue>) change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(this::observeParamRow);
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(this.paramRows::remove);
                }
            }
            if (!this.loading && !this.syncingParams) {
                this.onParamsChanged();
            }
        });
    }

    /**
     * 监听单个参数行。
     *
     * @param row 参数行
     */
    private void observeParamRow(ShellHttpKeyValue row) {
        if (row == null || !this.paramRows.add(row)) {
            return;
        }
        row.enabledProperty().addListener((observable, oldValue, newValue) -> this.onParamsChanged());
        row.keyProperty().addListener((observable, oldValue, newValue) -> this.onParamsChanged());
        row.valueProperty().addListener((observable, oldValue, newValue) -> this.onParamsChanged());
    }

    /**
     * 初始化地址和参数的联动。
     */
    private void syncInitialUrlAndParams() {
        if (this.paramsTable.getItems().isEmpty()) {
            this.syncParamsFromUrl(this.requestUrl.getText());
        } else {
            this.syncUrlFromParams();
        }
        this.request.setUrl(this.requestUrl.getText());
        this.request.setParams(new ArrayList<>(this.paramsTable.getItems()));
    }

    /**
     * 参数变更，同步到地址。
     */
    private void onParamsChanged() {
        if (this.loading || this.syncingParams) {
            return;
        }
        this.syncUrlFromParams();
        this.request.setUrl(this.requestUrl.getText());
        this.scheduleSave();
    }

    /**
     * 用参数刷新地址。
     */
    private void syncUrlFromParams() {
        String base = ShellHttpUtil.removeQuery(this.requestUrl.getText());
        String query = ShellHttpUtil.encodeRowsForDisplay(this.paramsTable.getItems());
        String url = StringUtil.isBlank(query) ? base : ShellHttpUtil.appendQuery(base, query);
        this.syncingUrl = true;
        try {
            this.requestUrl.setText(url);
        } finally {
            this.syncingUrl = false;
        }
    }

    /**
     * 用地址刷新参数。
     *
     * @param url 地址
     */
    private void syncParamsFromUrl(String url) {
        List<ShellHttpKeyValue> rows = ShellHttpUtil.parseQuery(url);
        for (ShellHttpKeyValue row : this.paramsTable.getItems()) {
            if (row != null && !row.isEnabled()) {
                rows.add(row);
            }
        }
        this.syncingParams = true;
        try {
            this.paramsTable.getItems().setAll(rows);
        } finally {
            this.syncingParams = false;
        }
    }

    /**
     * 请求级设置变更。
     */
    private void onRequestSettingChanged() {
        if (this.loading || this.request == null) {
            return;
        }
        this.applyRequestSettings();
        this.scheduleSave();
    }

    /**
     * 应用请求级设置。
     */
    private void applyRequestSettings() {
        if (this.request == null) {
            return;
        }
        ShellHttpSetting global = this.workspace.getSettings();
        this.request.setHttpVersion(StringUtil.blankToDefault(
                this.requestHttpVersion.getValue(), global.getHttpVersion()));
        this.request.setFollowRedirects(this.requestFollowRedirects.isSelected());
        this.request.setSendNoCacheHeader(this.requestNoCacheHeader.isSelected());
        int value = this.requestTimeout.getIntValue();
        this.request.setTimeoutSeconds(value > 0 ? value : global.getTimeoutSeconds());
    }

    /**
     * 延迟保存
     */
    private void scheduleSave() {
        if (this.loading || this.tabPane == null || this.request == null) {
            return;
        }
        // 表格增删行不会自动回写到请求对象，这里先同步列表再保存
        this.request.setParams(new ArrayList<>(this.paramsTable.getItems()));
        this.request.setHeaders(new ArrayList<>(this.headersTable.getItems()));
        this.request.setFormBody(new ArrayList<>(this.formBodyTable.getItems()));
        if (this.requestScript != null) {
            this.requestScript.setPreRequestScript(this.preRequestScript.getText());
        }
        this.markUnsaved();
    }

    /**
     * 标记为未保存
     */
    private void markUnsaved() {
        if (this.loading || this.request == null) {
            return;
        }
        if (!this.unsaved) {
            this.unsaved = true;
            this.flushTab();
        }
    }

    /**
     * 是否有未保存修改
     *
     * @return 结果
     */
    public boolean isUnsaved() {
        return unsaved;
    }

    /**
     * 重命名请求
     *
     * @param name 名称
     */
    public void rename(String name) {
        if (this.request == null) {
            return;
        }
        this.request.setName(name);
        if (this.item != null) {
            this.item.refreshRequest();
        }
    }

    /**
     * 保存请求
     */
    public void flush() {
        if (this.request == null) {
            return;
        }
        this.request.setMethod(StringUtil.blankToDefault(
                this.method.getValue(), ShellHttpRequest.METHOD_GET));
        // 发送前再同步一次，确保 POST 等请求的查询参数也进入实际请求地址
        if (!this.paramsTable.getItems().isEmpty()) {
            this.syncUrlFromParams();
        }
        this.request.setUrl(this.requestUrl.getText());
        this.request.setParams(new ArrayList<>(this.paramsTable.getItems()));
        this.request.setHeaders(new ArrayList<>(this.headersTable.getItems()));
        this.request.setBodyType(StringUtil.blankToDefault(
                this.bodyType.getValue(), ShellHttpRequest.BODY_NONE));
        this.request.setBodyContentType(StringUtil.blankToDefault(
                this.rawContentType.getValue(), "application/json"));
        if (ShellHttpRequest.BODY_BINARY.equalsIgnoreCase(this.request.getBodyType())) {
            this.request.setBody(this.bodyFile.getTextTrim());
        } else {
            this.request.setBody(StringUtil.blankToDefault(this.requestBody.getText(), ""));
        }
        this.request.setFormBody(new ArrayList<>(this.formBodyTable.getItems()));
        this.request.setAuthType(StringUtil.blankToDefault(
                this.authType.getValue(), ShellHttpRequest.AUTH_NONE));
        this.request.setAuthUser(this.authUser.getTextTrim());
        this.request.setAuthPassword(this.authPassword.getPassword());
        this.request.setAuthToken(this.authToken.getPassword());
        this.request.setAuthKeyName(this.authKeyName.getTextTrim());
        this.request.setAuthKeyValue(this.authKeyValue.getPassword());
        this.request.setAuthKeyPlacement(StringUtil.blankToDefault(
                this.authKeyPlacement.getValue(), ShellHttpRequest.PLACEMENT_HEADER));
        this.applyRequestSettings();
        if (this.item != null) {
            this.item.refreshRequest();
        }
        boolean requestSaved = ShellHttpRequestStore.INSTANCE.update(this.request);
        this.requestScript.setIid(this.request.getIid());
        this.requestScript.setRequestId(this.request.getId());
        this.requestScript.setPreRequestScript(this.preRequestScript.getText());
        boolean scriptSaved = ShellHttpRequestScriptStore.INSTANCE.replace(this.requestScript);
        this.unsaved = !(requestSaved && scriptSaved);
        this.flushTab();
    }

    /**
     * 保存
     */
    @FXML
    private void saveRequest() {
        this.flush();
        MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
    }

    /**
     * 发送请求
     */
    @FXML
    private void sendRequest() {
        if (this.request == null) {
            return;
        }
        this.flush();
        if (StringUtil.isBlank(this.requestUrl.getTextTrim())) {
            MessageBox.warn(ShellI18nHelper.httpUrlEmpty(), this.getWindow());
            return;
        }
        ShellHttpClient client = this.tabPane.getClient();
        if (client == null) {
            return;
        }
        this.response = null;
        this.responseStatus.text(ShellI18nHelper.httpSending());
        this.responseStatus.setTextFill(Color.ORANGE);
        this.responseTime.text("-");
        this.responseSize.text("-");
        this.responseFormat.setDisable(true);
        this.responseBodyRaw = "";
        this.responseBody.text("");
        this.responseHeaders.text("");
        this.responseHeadersTable.getItems().clear();
        this.preview.setDisable(true);
        this.saveResponseBody.setDisable(true);
        this.openResponseFile.setDisable(true);
        this.resetResponseExtraTabs();
        this.send.setDisable(true);
        ShellHttpRequest snapshot = this.request.copy();
        String preScript = this.requestScript.getPreRequestScript();
        String testsScript = this.requestScript.getTestScript();
        ShellHttpScriptContext scriptContext = new ShellHttpScriptContext(
                this.workspace, snapshot, this.workspace.activeEnvironment());
        ShellHttpScriptEngine scriptEngine = new ShellHttpScriptEngine(scriptContext);
        ThreadUtil.start(() -> {
            try {
                scriptEngine.evalPreRequest(preScript);
                ShellHttpResponse result = client.execute(snapshot, this.workspace);
                scriptContext.setResponse(result);
                try {
                    scriptEngine.evalTests(testsScript);
                } catch (Throwable testEx) {
                    testEx.printStackTrace();
                    String message = testEx.getMessage() == null ? testEx.toString() : testEx.getMessage();
                    System.out.println("[HTTP Test Error] " + message);
                }
                FXUtil.runLater(() -> {
                    this.showResponse(result);
                    this.showScriptTestResults(scriptContext);
                });
            } catch (Throwable ex) {
                ex.printStackTrace();
                FXUtil.runLater(() -> {
                    this.send.setDisable(false);
                    this.responseStatus.text(ShellI18nHelper.httpRequestFailed());
                    this.responseStatus.setTextFill(Color.RED);
                    this.responseBodyRaw = "";
                    this.responseHeadersTable.getItems().clear();
                    this.saveResponseBody.setDisable(true);
                    this.openResponseFile.setDisable(true);
                    this.responseBody.text(ex.getMessage() == null
                            ? ex.toString() : ex.getMessage());
                });
            }
        });
    }

    /**
     * 显示脚本测试结果
     *
     * @param context 脚本上下文
     */
    private void showScriptTestResults(ShellHttpScriptContext context) {
        for (ShellHttpScriptContext.TestResult result : context.getTestResults()) {
            System.out.println("[HTTP Test] " + (result.passed() ? "PASS " : "FAIL ")
                    + result.name() + (result.passed() || StringUtil.isBlank(result.message())
                    ? "" : " - " + result.message()));
        }
    }

    /**
     * 显示响应
     *
     * @param result 响应
     */
    private void showResponse(ShellHttpResponse result) {
        this.response = result;
        this.resetResponseExtraTabs();
        this.send.setDisable(false);
        String status = result.getStatusCode()
                + (StringUtil.isBlank(result.getStatusText()) ? "" : " " + result.getStatusText());
        this.responseStatus.text(status);
        if (result.isSuccessful()) {
            this.responseStatus.setTextFill(Color.GREEN);
        } else if (result.getStatusCode() >= 300 && result.getStatusCode() < 400) {
            this.responseStatus.setTextFill(Color.ORANGE);
        } else {
            this.responseStatus.setTextFill(Color.RED);
        }
        this.responseTime.text(result.getDurationMs() + " ms");
        this.responseSize.text(ShellHttpUtil.humanSize(result.getSize()));
        this.responseBodyRaw = StringUtil.blankToDefault(result.getBody(), "");
        this.responseFormat.setDisable(result.isBinary());
        this.responseFormat.setValue(this.detectResponseFormat(result));
        this.renderResponseBody();
        this.responseHeaders.text(ShellHttpClient.headersText(result));
        this.responseHeadersTable.getItems().setAll(this.responseHeaderRows(result));
        this.updateResponseHeadersMode();
        this.preview.setDisable(!result.isHtml());
        this.saveResponseBody.setDisable(result.isBinary());
        this.openResponseFile.setDisable(StringUtil.isBlank(result.getTempFile()));
        this.loadExtraResponseTab(this.responseTabPane.getSelectionModel().getSelectedItem());
    }

    /**
     * 重置 Base64/HEX 响应页。
     */
    private void resetResponseExtraTabs() {
        this.base64Loaded = false;
        this.hexLoaded = false;
        if (this.responseBase64 != null) {
            this.responseBase64.text("");
        }
        if (this.responseHex != null) {
            this.responseHex.openBytes(new byte[0]);
        }
    }

    /**
     * 按需加载扩展响应页。
     *
     * @param tab 当前标签页
     */
    private void loadExtraResponseTab(Tab tab) {
        if (tab == this.responseBase64Tab) {
            this.loadBase64Response();
        } else if (tab == this.responseHexTab) {
            this.loadHexResponse();
        }
    }

    /**
     * 加载 Base64 响应。
     */
    private void loadBase64Response() {
        if (this.base64Loaded || this.response == null) {
            return;
        }
        this.base64Loaded = true;
        byte[] data = this.responseData();
        this.responseBase64.text(data.length == 0 ? "" : Base64.getEncoder().encodeToString(data));
    }

    /**
     * 加载 HEX 响应。
     */
    private void loadHexResponse() {
        if (this.hexLoaded || this.response == null) {
            return;
        }
        this.hexLoaded = true;
        this.responseHex.openBytes(this.responseData());
    }

    /**
     * 获取响应原始字节。
     *
     * @return 字节
     */
    private byte[] responseData() {
        if (this.response == null) {
            return new byte[0];
        }
        if (this.response.getData() != null) {
            return this.response.getData();
        }
        if (StringUtil.isNotBlank(this.response.getTempFile())) {
            try {
                return Files.readAllBytes(Path.of(this.response.getTempFile()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (this.response.getBody() == null) {
            return new byte[0];
        }
        return this.response.getBody().getBytes(this.response.getCharset() == null
                ? StandardCharsets.UTF_8 : this.response.getCharset());
    }

    /**
     * 渲染响应正文
     */
    private void renderResponseBody() {
        if (this.response == null) {
            return;
        }
        if (this.response.isBinary()) {
            this.responseBody.text(ShellI18nHelper.httpBinaryResponse(
                    this.response.getSize(), this.response.getContentType()));
            return;
        }
        String format = this.responseFormat.getValue();
        String contentType;
        EditorFormatType formatType;
        switch (format == null ? "" : format) {
            case "JSON" -> {
                contentType = "application/json";
                formatType = EditorFormatType.JSON;
            }
            case "XML" -> {
                contentType = "application/xml";
                formatType = EditorFormatType.XML;
            }
            case "HTML" -> {
                contentType = "text/html";
                formatType = EditorFormatType.HTML;
            }
            case "CSS" -> {
                contentType = "text/css";
                formatType = EditorFormatType.CSS;
            }
            case "JavaScript" -> {
                contentType = "text/javascript";
                formatType = EditorFormatType.JAVASCRIPT;
            }
            case "Markdown" -> {
                contentType = "text/markdown";
                formatType = EditorFormatType.MARKDOWN;
            }
            default -> {
                contentType = "text/plain";
                formatType = EditorFormatType.RAW;
            }
        }
        this.responseBody.setFormatType(formatType);
        boolean pretty = this.workspace != null
                && this.workspace.getSettings().isPrettyJson()
                && "JSON".equals(format);
        this.responseBody.text(ShellHttpUtil.formatResponseBody(
                this.responseBodyRaw, contentType, pretty));
    }

    /**
     * 获取响应体默认显示格式
     *
     * @param response 响应
     * @return 格式
     */
    private String detectResponseFormat(ShellHttpResponse response) {
        if (response.isJson()) {
            return "JSON";
        }
        if (response.isHtml()) {
            return "HTML";
        }
        if (response.isCss()) {
            return "CSS";
        }
        if (response.isJavaScript()) {
            return "JavaScript";
        }
        if (response.isMarkdown()) {
            return "Markdown";
        }
        if (response.getContentType() != null
                && response.getContentType().toLowerCase().contains("xml")) {
            return "XML";
        }
        return "Text";
    }

    /**
     * 响应头表格行
     *
     * @param response 响应
     * @return 行
     */
    private List<ShellHttpKeyValue> responseHeaderRows(ShellHttpResponse response) {
        List<ShellHttpKeyValue> rows = new ArrayList<>();
        if (response.getHeaders() != null) {
            response.getHeaders().forEach((name, values) ->
                    values.forEach(value -> rows.add(new ShellHttpKeyValue(name, value))));
        }
        return rows;
    }

    /**
     * 更新响应头显示模式
     */
    private void updateResponseHeadersMode() {
        boolean table = this.responseHeadersTableMode.isSelected();
        this.responseHeadersTable.setVisible(table);
        this.responseHeadersTable.setManaged(table);
        this.responseHeaders.setVisible(!table);
        this.responseHeaders.setManaged(!table);
    }

    /**
     * 保存响应正文到文件
     */
    @FXML
    private void saveResponseBody() {
        if (this.response == null || this.response.isBinary()) {
            return;
        }
        try {
            String extension = this.responseBodyExtension();
            FileExtensionFilter filter = new FileExtensionFilter(
                    extension.toUpperCase(Locale.ROOT), "*." + extension);
            File file = FileChooserHelper.save(I18nHelper.saveFile(), "response." + extension,
                    filter, this.getWindow());
            if (file != null) {
                file = this.ensureResponseBodyExtension(file, extension);
                FileUtil.writeUtf8String(this.responseBodyRaw, file);
                MessageBox.info(I18nHelper.operationSuccess(), this.getWindow());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 确保响应正文文件使用实际格式后缀
     *
     * @param file      文件
     * @param extension 扩展名
     * @return 文件
     */
    private File ensureResponseBodyExtension(File file, String extension) {
        String suffix = "." + extension.toLowerCase(Locale.ROOT);
        String name = file.getName();
        if (name.toLowerCase(Locale.ROOT).endsWith(suffix)) {
            return file;
        }
        int index = name.lastIndexOf('.');
        String baseName = index > 0 ? name.substring(0, index) : name;
        return new File(file.getParentFile(), baseName + suffix);
    }

    /**
     * 打开响应文件
     */
    @FXML
    private void openResponseFile() {
        if (this.response == null || StringUtil.isBlank(this.response.getTempFile())) {
            return;
        }
        try {
            Path file = Path.of(this.response.getTempFile());
            FXUtil.open(file.toUri().toString());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 响应正文文件扩展名
     *
     * @return 扩展名
     */
    private String responseBodyExtension() {
        // 文件后缀使用响应内容的实际格式，而不是当前显示格式
        String format = this.response == null ? null : this.detectResponseFormat(this.response);
        return switch (format == null ? "" : format) {
            case "JSON" -> "json";
            case "XML" -> "xml";
            case "HTML" -> "html";
            case "CSS" -> "css";
            case "JavaScript" -> "js";
            case "Markdown" -> "md";
            default -> "txt";
        };
    }

    /**
     * 预览 HTML
     */
    @FXML
    private void previewHtml() {
        ShellHttpResponse current = this.response;
        if (current == null || !current.isHtml()) {
            MessageBox.warn(ShellI18nHelper.httpNotHtml(), this.getWindow());
            return;
        }
        ThreadUtil.start(() -> {
            try {
                Path file = ShellHttpUtil.createHtmlPreview(current.getBody(), current.getUri());
                FXUtil.runLater(() -> {
                    try {
                        FXUtil.showDocument(file.toUri().toString());
                    } catch (Exception ex) {
                        MessageBox.exception(ex, this.getWindow());
                    }
                });
            } catch (Exception ex) {
                FXUtil.runLater(() -> MessageBox.exception(ex, this.getWindow()));
            }
        });
    }

    @FXML
    private void addParam() {
        this.paramsTable.addRow();
    }

    @FXML
    private void deleteParam() {
        this.paramsTable.deleteSelectedRow();
    }

    @FXML
    private void addHeader() {
        this.headersTable.addRow();
    }

    @FXML
    private void deleteHeader() {
        this.headersTable.deleteSelectedRow();
    }

    @FXML
    private void addFormData() {
        this.formDataTable.addRow();
    }

    @FXML
    private void deleteFormData() {
        this.formDataTable.deleteSelectedRow();
    }

    @FXML
    private void addFormBody() {
        this.formBodyTable.addRow();
    }

    @FXML
    private void deleteFormBody() {
        this.formBodyTable.deleteSelectedRow();
    }

    /**
     * 获取请求
     *
     * @return 请求
     */
    public ShellHttpRequest request() {
        return request;
    }

    /**
     * 设置下拉框显示
     *
     * @param combo   下拉框
     * @param values  值
     * @param display 显示转换
     */
    private void displayCombo(FXComboBox<String> combo, List<String> values,
                              Function<String, String> display) {
        combo.getItems().setAll(values);
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(String value) {
                return value == null ? "" : display.apply(value);
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        });
    }

    /**
     * 请求体类型显示
     *
     * @param value 值
     * @return 显示文本
     */
    private String bodyTypeLabel(String value) {
        return switch (value) {
            case ShellHttpRequest.BODY_FORM_DATA -> "form-data";
            case ShellHttpRequest.BODY_RAW -> ShellI18nHelper.httpBodyRaw();
            case ShellHttpRequest.BODY_FORM -> ShellI18nHelper.httpBodyUrlencoded();
            case ShellHttpRequest.BODY_BINARY -> ShellI18nHelper.httpBodyBinary();
            default -> I18nHelper.none();
        };
    }

    /**
     * 认证类型显示
     *
     * @param value 值
     * @return 显示文本
     */
    private String authTypeLabel(String value) {
        return switch (value) {
            case ShellHttpRequest.AUTH_BASIC -> ShellI18nHelper.httpBasicAuth();
            case ShellHttpRequest.AUTH_BEARER -> ShellI18nHelper.httpBearerAuth();
            case ShellHttpRequest.AUTH_API_KEY -> ShellI18nHelper.httpApiKeyAuth();
            default -> I18nHelper.none();
        };
    }

    /**
     * API Key 位置显示
     *
     * @param value 值
     * @return 显示文本
     */
    private String placementLabel(String value) {
        return ShellHttpRequest.PLACEMENT_QUERY.equalsIgnoreCase(value)
                ? I18nHelper.query() : ShellI18nHelper.httpHeader();
    }
}
