package cn.oyzh.easyshell.query;

import cn.oyzh.common.thread.TaskManager;
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
 * 查询提示弹窗
 *
 * @author oyzh
 * @since 2024/02/21
 */
public abstract class ShellQueryPromptPopup<E extends ShellQueryPromptItem, T extends ShellQueryToken> extends FXPopup {

    /**
     * token
     */
    protected T token;

    /**
     * 选中事件
     */
    protected Consumer<E> onItemSelected;

    /**
     * 提示词标志位
     */
    protected final AtomicInteger promptFlag = new AtomicInteger();

    /**
     * 初始化内容组件
     */
    protected void initContent() {
        ShellQueryPromptListView<E> listView = this.listView();
        if (listView == null) {
            listView = this.initListView();
            this.getContent().setAll(listView);
            listView.setCursor(Cursor.HAND);
            listView.setOnItemPicked(() -> {
                this.pickItem();
                this.hide();
            });
        }
    }

    /**
     * 初始化列表组件
     *
     * @return 列表组件
     */
    protected abstract ShellQueryPromptListView<E> initListView();

    /**
     * 获取列表组件
     *
     * @return 列表组件
     */
    public ShellQueryPromptListView<E> listView() {
        return (ShellQueryPromptListView<E>) super.content();
    }

    /**
     * 初始化提示词
     *
     * @param token 提示词
     * @return 结果
     */
    protected boolean initPrompts(T token) {
        // 提示词列表
        List<E> items = this.tokenAnalyzer().initPrompts(token, 0.5f);
        // 初始化数据
        this.listView().init(items);
        // 判断是否为空
        return !this.listView().isItemEmpty();
    }

    /**
     * 执行提示
     *
     * @param editor 编辑器
     * @param event  键盘按键事件
     */
    public void prompt(ShellQueryEditor editor, KeyEvent event) {
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
        // 获取token
        this.token = this.currentToken(editor);
        // 处理token
        if (!this.handlToken(editor)) {
            // 隐藏组件
            this.hide();
        }
    }

    /**
     * 处理token
     *
     * @param editor 编辑器
     * @return 结果
     */
    protected boolean handlToken(ShellQueryEditor editor) {
        // 处理token
        if (this.tokenAvailable()) {
            // 生成标志位
            int promptFlagVal = this.promptFlag.incrementAndGet();
            // 延迟显示提示词
            TaskManager.startDelay(() -> {
                // 初始化提示词
                if (this.promptFlag.get() == promptFlagVal) {
                    if (this.initPrompts(this.token)) {
                        this.show(editor);
                    } else {
                        this.hide();
                    }
                }
            }, 30);
            return true;
        }
        return false;
    }

    /**
     * token是否可用
     *
     * @return 结果
     */
    protected boolean tokenAvailable() {
        return this.token != null && this.token.isNotEmpty();
    }

    /**
     * 获取token解析器
     *
     * @return 结果
     */
    protected abstract ShellQueryTokenAnalyzer<E, T> tokenAnalyzer();

    /**
     * 获取当前token
     *
     * @param editor 编辑器
     * @return 当前token
     */
    protected T currentToken(ShellQueryEditor editor) {
        // 光标位置
        int cartPos = editor.caretPosition();
        // 文本内容
        String content = editor.getText();
        // 获取当前token
        return this.tokenAnalyzer().currentToken(content, cartPos);
    }

    /**
     * 执行自动完成
     *
     * @param editor 编辑器
     * @param item   提示内容
     */
    public void autoComplete(ShellQueryEditor editor, E item) {
        if (this.token != null) {
            try {
                this.replaceText(editor, item.getContent());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 替换文本
     *
     * @param editor  编辑器
     * @param content 文本
     */
    protected void replaceText(ShellQueryEditor editor, String content) {
        editor.replaceText(this.token.getStartIndex(), this.token.getEndIndex(), content);
        int caretPos = editor.caretPosition();
        int targetPos = this.token.getEndIndex() + content.length();
        if (caretPos != targetPos) {
            editor.positionCaret(targetPos);
        }
    }

    /**
     * 显示提示词组件
     *
     * @param editor 编辑器
     */
    protected void show(ShellQueryEditor editor) {
        RenderService.submitFXLater(() -> {
            try {
                Optional<Bounds> optional = editor.getCaretBounds();
                // 显示提示词
                optional.ifPresent(bounds -> this.show(editor, bounds.getCenterX() - 15, bounds.getCenterY() + 10));
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
     * 选中提示词
     */
    protected void pickItem() {
        if (this.onItemSelected != null && this.isShowing()) {
            E pickedItem = this.listView().getPickedItem();
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
    protected boolean isGeneralKeyEvent(KeyEvent event) {
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

    public Consumer<E> getOnItemSelected() {
        return onItemSelected;
    }

    public void setOnItemSelected(Consumer<E> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    @Override
    public void initNode() {
        this.initContent();
        this.changeTheme(ThemeManager.currentTheme());
        super.initNode();
    }
}
