package cn.oyzh.easyshell.query.mongo;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.controls.popup.FXPopup;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.thread.RenderService;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 查询提示框
 *
 * @author oyzh
 * @since 2024/02/21
 */
public class MongoQueryPromptPopup extends FXPopup {

    /**
     * 选中事件
     */
    protected Consumer<MongoQueryPromptItem> onItemSelected;

    public MongoQueryPromptPopup() {
        this.setAutoFix(true);
        this.setAutoHide(true);
        this.initContent();
        this.changeTheme(ThemeManager.currentTheme());
    }

    /**
     * 初始化内容组件
     */
    protected void initContent() {
        MongoQueryPromptListView listView = this.listView();
        if (listView == null) {
            listView = new MongoQueryPromptListView();
            this.getContent().setAll(listView);
            listView.setFontSize(12.0);
            listView.setCursor(Cursor.HAND);
            listView.setOnItemPicked(() -> {
                this.pickItem();
                this.hide();
            });
        }
    }

    /**
     * 列表组件
     *
     * @return 列表组件
     */
    public MongoQueryPromptListView listView() {
        return (MongoQueryPromptListView) CollectionUtil.getFirst(this.getContent());
    }

    /**
     * 初始化提示词
     *
     * @param token 提示词
     * @return 结果
     */
    public synchronized boolean initPrompts(MongoQueryToken token) {
        // 提示词列表
        List<MongoQueryPromptItem> items = MongoQueryTokenAnalyzer.INSTANCE.initPrompts(token, 0.5f);
        // 初始化数据
        this.listView().init(items);
        // 判断是否为空
        return !this.listView().isItemEmpty();
    }

    /**
     * token
     */
    private MongoQueryToken token;

    /**
     * 提示词标志位
     */
    private final AtomicInteger promptFlag = new AtomicInteger();

    /**
     * 执行提示
     *
     * @param area  文本域
     * @param event 键盘按键事件
     */
    public void prompt(MongoQueryEditor area, KeyEvent event) {
        // 常规按键不处理
        if (this.isGeneralKeyEvent(event)) {
            this.hide();
            return;
        }
        KeyCode code = event.getCode();
        // 已显示
        if (this.isShowing()) {
            // 按键下
            if (code == KeyCode.DOWN) {
                this.listView().pickNext();
                return;
            }
            // 选中内容清空下才处理
            if (this.listView().hasPicked()) {
                // 按键上
                if (code == KeyCode.UP) {
                    this.listView().pickPrev();
                    return;
                }
                // 按键回车
                if (code == KeyCode.ENTER) {
                    this.pickItem();
                    this.hide();
                    return;
                }
            }
        }
        // 更新按键
        if (ShellQueryUtil.UPDATE_CODES.contains(code)) {
            this.hide();
            return;
        }
        // 判断按键特征，按需隐藏提示组件
        if (KeyboardUtil.isMainModifierDown(event) || !ShellQueryUtil.PROMPT_CODES.contains(code)) {
            this.hide();
            return;
        }
        // 光标位置
        int cartPos = area.caretPosition();
        // 文本内容
        String content = area.getText();
        // 获取token
        this.token = MongoQueryTokenAnalyzer.INSTANCE.currentToken(content, cartPos);
        // 处理token
        if (this.token != null && this.token.isNotEmpty()) {
            // 生成标志位
            int promptFlagVal = this.promptFlag.incrementAndGet();
            // 延迟显示提示词
            TaskManager.startDelay(() -> {
                // 初始化提示词
                if (this.promptFlag.get() == promptFlagVal) {
                    if (this.initPrompts(this.token)) {
                        this.show(area);
                    } else {
                        this.hide();
                    }
                }
            }, 30);
            return;
        }
        // 隐藏组件
        this.hide();
    }

    /**
     * 显示提示词组件
     *
     * @param area 文本域
     */
    private void show(MongoQueryEditor area) {
        RenderService.submitFXLater(() -> {
            try {
                Optional<Bounds> optional = area.getCaretBounds();
                // 显示提示词
                optional.ifPresent(bounds -> this.show(area, bounds.getCenterX() - 15, bounds.getCenterY() + 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void hide() {
        if (this.isShowing()) {
            FXUtil.runWait(super::hide);
        }
        this.token = null;
    }

    /**
     * 执行自动完成
     *
     * @param editor 编辑器
     * @param item   提示内容
     */
    public void autoComplete(MongoQueryEditor editor, MongoQueryPromptItem item) {
        try {
            if (this.token != null) {
                editor.replaceText(this.token.getStartIndex(), this.token.getEndIndex(), item.getContent());
                int caretPos = editor.caretPosition();
                int targetPos = this.token.getEndIndex() + item.getContent().length();
                if (caretPos != targetPos) {
                    editor.positionCaret(targetPos);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 选中提示词
     */
    private void pickItem() {
        if (this.onItemSelected != null && this.isShowing()) {
            MongoQueryPromptItem pickedItem = this.listView().getPickedItem();
            if (pickedItem != null) {
                this.onItemSelected.accept(pickedItem);
            }
        }
    }

    /**
     * 是否常规按键事件
     *
     * @param event 按键事件
     * @return 结果
     */
    private boolean isGeneralKeyEvent(KeyEvent event) {
        KeyCode code = event.getCode();
        // 保存
        if (event.isControlDown() && KeyCode.S == code) {
            return true;
        }
        // 剪切
        if (event.isControlDown() && KeyCode.X == code) {
            return true;
        }
        // 粘贴
        if (event.isControlDown() && KeyCode.V == code) {
            return true;
        }
        // 复制
        if (event.isControlDown() && KeyCode.C == code) {
            return true;
        }
        // 全选
        if (event.isControlDown() && KeyCode.A == code) {
            return true;
        }
        // 撤销
        if (event.isControlDown() && KeyCode.Z == code) {
            return true;
        }
        // 重做
        if (event.isControlDown() && KeyCode.Y == code) {
            return true;
        }
        // 注释
        if (event.isControlDown() && KeyCode.SLASH == code) {
            return true;
        }
        return false;
    }

    public Consumer<MongoQueryPromptItem> getOnItemSelected() {
        return onItemSelected;
    }

    public void setOnItemSelected(Consumer<MongoQueryPromptItem> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }
}
