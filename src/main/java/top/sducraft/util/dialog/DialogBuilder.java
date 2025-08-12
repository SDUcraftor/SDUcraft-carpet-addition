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
    // 我们将让子类完全负责元素的管理，父类不再需要这个列表。
    // 为了避免混淆，可以将其注释或删除。
    // protected final List<JsonObject> bodyElements = new ArrayList<>();

    protected DialogBuilder(String type) {
        this.root = new JsonObject();
        this.root.addProperty("type", type);
        this.setAfterAction("close");
        this.setCanCloseWithEscape(true);
        this.setPauseGame(false);
    }

    public T setTitle(String title) {
        this.root.add("title", ComponentFactory.createText(title));
        return self();
    }

    /**
     * [为了方便保留] 添加一行简单的文本。
     * 这是一个便捷方法，内部调用 addBody。
     * @deprecated 推荐使用 addBody(TextComponentBuilder) 以获得更多控制。
     */
    @Deprecated
    public T addBodyText(String text) {
        return this.addBody(new TextComponentBuilder(text));
    }

    /**
     * [抽象方法] 添加一个复杂的文本主体。
     * 这个方法必须由子类来实现，因为只有子类知道如何存储元素。
     * @param builder 一个配置好的 TextComponentBuilder。
     */
    public abstract T addBody(TextComponentBuilder builder);


    public JsonObject build() {
        // 父类的 build 方法现在只负责返回 root，因为所有构建逻辑都在子类中。
        return root;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
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
}