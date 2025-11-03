package top.sducraft.util.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * [已修正] 一个用于构建复杂 Minecraft JSON 文本组件的构建器。
 * 此版本生成对话框 'plain_message' 所需的、正确的扁平化样式结构。
 */
public class TextComponentBuilder {

    private final JsonArray parts = new JsonArray();
    private JsonObject currentPart;
    private int width = -1;

    /**
     * 创建一个空的 TextComponentBuilder。
     */
    public TextComponentBuilder() {

    }

    /**
     * 创建一个包含初始文本的 TextComponentBuilder。
     *
     * @param initialText 初始的文本片段。
     */
    public TextComponentBuilder(String initialText) {
        this.append(initialText);
    }

    /**
     * 添加一个新的文本片段。后续的样式方法将应用于此片段。
     *
     * @param text 要添加的文本。
     */
    public TextComponentBuilder append(String text) {

        this.currentPart = new JsonObject();
        this.currentPart.addProperty("text", text);
        this.parts.add(this.currentPart);
        return this;
    }

    /**
     * 为当前文本片段设置颜色。
     *
     * @param color 颜色的名称 (例如 "red", "gold") 或十六进制代码 (例如 "#FF55FF")。
     */
    public TextComponentBuilder color(String color) {
        ensureCurrentPartExists();
        this.currentPart.addProperty("color", color);
        return this;
    }

    /**
     * 为当前文本片段设置为粗体。
     */
    public TextComponentBuilder bold() {
        // [修正] 直接将属性添加到 currentPart
        ensureCurrentPartExists();
        this.currentPart.addProperty("bold", true);
        return this;
    }

    /**
     * 为当前文本片段设置点击事件，用于执行一条命令。
     *
     * @param command 要执行的命令。
     */
    public TextComponentBuilder onClickRunCommand(String command) {
        // [修正] 直接将 clickEvent 添加到 currentPart
        ensureCurrentPartExists();
        JsonObject clickEvent = new JsonObject();
        clickEvent.addProperty("action", "run_command");
        // 修正: 文本组件的点击事件使用 'value' 字段
        clickEvent.addProperty("command", command);
        this.currentPart.add("click_event", clickEvent);
        return this;
    }

    /**
     * 为当前文本片段设置点击事件，用于打开一个URL。
     *
     * @param url 要打开的链接。
     */
    public TextComponentBuilder onClickOpenUrl(String url) {
        // [修正] 直接将 clickEvent 添加到 currentPart
        ensureCurrentPartExists();
        JsonObject clickEvent = new JsonObject();
        clickEvent.addProperty("action", "open_url");
        clickEvent.addProperty("value", url);
        this.currentPart.add("click_event", clickEvent);
        return this;
    }

    /**
     * [新增] 内部辅助方法，确保在应用样式前已经有文本片段存在。
     */
    private void ensureCurrentPartExists() {
        if (this.currentPart == null) {
            throw new IllegalStateException("You must call .append(text) before applying any styles or click events.");
        }
    }

    public TextComponentBuilder withWidth(int width) {
        // 根据文档，宽度值 >= 1
        if (width >= 1) {
            this.width = width;
        }
        return this;
    }

    /**
     * 构建用于 'plain_message' 主体元素的完整 JsonObject。
     *
     * @return 代表完整 'plain_message' 主体元素的 JsonObject。
     */
    public JsonObject build() {
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", "plain_message");

        if (this.width > 0) {
            wrapper.addProperty("width", this.width);
        }

        // 使用新的 buildContents() 方法来获取核心内容
        wrapper.add("contents", this.buildContents());
        return wrapper;
    }

    /**
     * 这部分可以被用于任何需要文本组件的地方，例如按钮的 label。
     *
     * @return 代表文本组件的 JsonObject 或 JsonArray。
     */
    public com.google.gson.JsonElement buildContents() {
        if (this.parts.isEmpty()) {
            // 返回一个空的文本对象，而不是 null
            return new JsonObject();
        } else if (this.parts.size() == 1) {
            // 如果只有一个部分，直接返回该部分
            return this.parts.get(0);
        } else {
            // 如果有多个部分，返回包含所有部分的数组
            return this.parts;
        }
    }
}