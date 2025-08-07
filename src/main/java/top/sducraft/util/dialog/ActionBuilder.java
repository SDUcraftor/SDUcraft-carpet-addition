package top.sducraft.util.dialog;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;


/**
 * 一个用于构建对话框中单个操作（按钮）的构建器。
 * 它提供了一个流畅的 API 来定义标签、提示、宽度和具体的点击行为。
 */
public class ActionBuilder {

    private final JsonObject container;
    private JsonObject actionPayload;

    /**
     * 创建一个新的 ActionBuilder。
     * @param label 按钮上显示的文本（必需）。
     */
    public ActionBuilder(String label) {
        this.container = new JsonObject();
        this.container.add("label", ComponentFactory.createText(label));
    }

    /**
     * 为按钮设置悬浮提示文本。
     * @param tooltip 鼠标悬停时显示的文本。
     * @return 当前的 ActionBuilder 实例，用于链式调用。
     */
    public ActionBuilder withTooltip(String tooltip) {
        if (tooltip != null && !tooltip.isEmpty()) {
            this.container.add("tooltip", ComponentFactory.createText(tooltip));
        }
        return this;
    }

    /**
     * 设置按钮的自定义宽度。
     * @param width 按钮的宽度（以像素为单位），默认为150。
     * @return 当前的 ActionBuilder 实例，用于链式调用。
     */
    public ActionBuilder withWidth(int width) {
        this.container.addProperty("width", width);
        return this;
    }

    // --- 静态操作 ---

    /**
     * 将此操作设置为运行一条静态命令。
     * @param command 要执行的命令。
     */
    public ActionBuilder asRunCommand(String command) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "run_command");
        this.actionPayload.addProperty("command", command);
        return this;
    }

    /**
     * 将此操作设置为打开一个URL。
     * @param url 要打开的链接。
     */
    public ActionBuilder asOpenUrl(String url) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "open_url");
        this.actionPayload.addProperty("value", url);
        return this;
    }

    /**
     * 将此操作设置为复制文本到剪贴板。
     * @param text 要复制的文本。
     */
    public ActionBuilder asCopyToClipboard(String text) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "copy_to_clipboard");
        this.actionPayload.addProperty("value", text);
        return this;
    }

    /**
     * 将此操作设置为显示另一个对话框。
     * @param dialogId 要显示的对话框的资源位置ID (例如 "mymod:my_dialog")。
     */
    public ActionBuilder asShowDialog(String dialogId) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "show_dialog");
        this.actionPayload.addProperty("dialog", dialogId);
        return this;
    }

    // --- 动态操作 ---

    /**
     * 将此操作设置为使用模板动态运行命令。
     * @param template 命令模板，例如 "/register $(college) $(grade)"
     */
    public ActionBuilder asDynamicRunCommand(String template) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "dynamic/run_command");
        this.actionPayload.addProperty("template", template);
        return this;
    }

    /**
     * 将此操作设置为发送一个自定义的点击事件。
     * @param id 自定义命名空间ID。
     * @param additions (可选) 一个 JsonObject，包含要添加到负载中的静态字段。
     */
    public ActionBuilder asDynamicCustom(String id, @Nullable JsonObject additions) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "dynamic/custom");
        this.actionPayload.addProperty("id", id);
        if (additions != null) {
            this.actionPayload.add("additions", additions);
        }
        return this;
    }

    /**
     * 构建最终的 JsonObject。
     * 在调用此方法之前，必须先调用一个 'as...' 方法来设置操作类型。
     * @return 代表完整操作的 JsonObject。
     */
    public JsonObject build() {
        if (this.actionPayload == null) {
            throw new IllegalStateException("Action type was not set. You must call one of the 'as...' methods before building.");
        }
        this.container.add("action", this.actionPayload);
        return this.container;
    }
}