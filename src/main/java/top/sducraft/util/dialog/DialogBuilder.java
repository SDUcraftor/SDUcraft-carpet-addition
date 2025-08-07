package top.sducraft.util.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个抽象的对话框构建器基类。
 * 它使用“奇异的递归模板模式” (CRTP) 来在继承体系中实现流畅的链式调用。
 * @param <T> 具体的构建器子类类型。
 */
public abstract class DialogBuilder<T extends DialogBuilder<T>> {
    protected final JsonObject root;
    protected final List<JsonObject> bodyElements = new ArrayList<>();

    protected DialogBuilder(String type) {
        this.root = new JsonObject();
        this.root.addProperty("type", type);
        this.setAfterAction("close");
        this.setCanCloseWithEscape(true);
        this.setPauseGame(false);
    }

    /**
     * 设置对话框的标题。
     * @param title 标题文本。
     */
    public T setTitle(String title) {
        this.root.add("title", ComponentFactory.createText(title));
        return self();
    }

    /**
     * 在对话框主体部分添加一行简单的文本。
     * @param text 要添加的文本。
     */
    public T addBodyText(String text) {
        JsonObject plainMessage = new JsonObject();
        plainMessage.addProperty("type", "plain_message");
        plainMessage.add("contents", ComponentFactory.createText(text));
        this.bodyElements.add(plainMessage);
        return self();
    }

    /**
     * 设置操作执行后的行为。
     *
     * @param afterAction "close", "none", 或 "wait_for_response"。
     */
    public void setAfterAction(String afterAction) {
        this.root.addProperty("after_action", afterAction);
    }

    public void setCanCloseWithEscape(boolean canClose) {
        this.root.addProperty("can_close_with_escape", canClose);
    }

    public void setPauseGame(boolean pause) {
        this.root.addProperty("pause", pause);
    }

    /**
     * 构建最终的 JsonObject。
     * @return 代表完整对话框的 JsonObject。
     */
    public JsonObject build() {
        if (!bodyElements.isEmpty()) {
            JsonArray bodyArray = new JsonArray();
            bodyElements.forEach(bodyArray::add);
            root.add("body", bodyArray);
        }
        return root;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
}