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
     * 私有构造函数，强制使用静态工厂方法来创建实例。
     *
     * @param labelElement 已经构建好的、代表标签的 JsonElement。
     */
    private ActionBuilder(com.google.gson.JsonElement labelElement) {
        this.container = new JsonObject();
        this.container.add("label", labelElement);
    }

    public ActionBuilder(String labelText) {
        this.container = new JsonObject();
        this.container.add("label", ComponentFactory.createText(labelText));
    }

    /**
     * [新] 使用一个 TextComponentBuilder 来创建一个带有复杂样式的 ActionBuilder。
     *
     * @param labelBuilder 一个已经配置好的 TextComponentBuilder 实例。
     */
    public static ActionBuilder withComplexLabel(TextComponentBuilder labelBuilder) {
        // 使用我们新增的 buildContents() 方法
        return new ActionBuilder(labelBuilder.buildContents());
    }

    public ActionBuilder withTooltip(String tooltip) {
        if (tooltip != null && !tooltip.isEmpty()) {
            this.container.add("tooltip", ComponentFactory.createText(tooltip));
        }
        return this;
    }

    public ActionBuilder withWidth(int width) {
        this.container.addProperty("width", width);
        return this;
    }

    public ActionBuilder asRunCommand(String command) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "run_command");
        this.actionPayload.addProperty("command", command);
        return this;
    }

    public ActionBuilder asSuggestCommand(String command) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "suggest_command");
        this.actionPayload.addProperty("command", command);
        return this;
    }

    public ActionBuilder asOpenUrl(String url) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "open_url");
        this.actionPayload.addProperty("url", url);
        return this;
    }

    public ActionBuilder asDynamicRunCommand(String template) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "dynamic/run_command");
        this.actionPayload.addProperty("template", template);
        return this;
    }

    public ActionBuilder asDynamicCustom(String id, @Nullable JsonObject additions) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "dynamic/custom");
        this.actionPayload.addProperty("id", id);
        if (additions != null) {
            this.actionPayload.add("additions", additions);
        }
        return this;
    }

    public JsonObject build() {
        if (this.actionPayload == null) {
            throw new IllegalStateException("Action type was not set. You must call one of the 'as...' methods before building.");
        }
        this.container.add("action", this.actionPayload);
        return this.container;
    }
}