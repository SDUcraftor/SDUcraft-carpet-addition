package top.sducraft.util.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 一个用于创建 "multi_action" 类型对话框的具体构建器。
 */
public class MultiActionDialogBuilder extends DialogBuilder<MultiActionDialogBuilder> {

    // --- 内部结构 (保持不变) ---
    private interface IDialogElement {
        void addToParent(Consumer<JsonObject> bodyConsumer, Consumer<JsonObject> inputConsumer, Consumer<JsonObject> actionConsumer);
    }

    /**
     * [关键] 这个列表是定义在子类中的，所有添加操作都围绕它进行。
     */
    private final List<IDialogElement> elements = new ArrayList<>();

    public MultiActionDialogBuilder() {
        super("multi_action");
    }

    // --- 实现父类的抽象方法 ---

    /**
     * [实现] 实现父类定义的抽象方法 addBody。
     * 它将一个构建好的文本组件添加到 elements 列表中。
     */
    @Override
    public MultiActionDialogBuilder addBody(TextComponentBuilder builder) {
        JsonObject bodyJson = builder.build();
        this.elements.add((body, input, action) -> body.accept(bodyJson));
        return this;
    }

    // --- 其他添加方法 (保持不变) ---

    public MultiActionDialogBuilder setColumns(int columns) {
        this.root.addProperty("columns", columns);
        return this;
    }

    public MultiActionDialogBuilder addTextInput(String key, String label) {
        JsonObject inputJson = new JsonObject();
        inputJson.addProperty("key", key);
        inputJson.addProperty("type", "text");
        inputJson.add("label", ComponentFactory.createText(label));
        this.elements.add((body, input, action) -> input.accept(inputJson));
        return this;
    }

    public MultiActionDialogBuilder addAction(ActionBuilder actionBuilder) {
        JsonObject actionJson = actionBuilder.build();
        this.elements.add((body, input, action) -> action.accept(actionJson));
        return this;
    }

    public MultiActionDialogBuilder addExitButton(String label) {
        JsonObject exitAction = new JsonObject();
        exitAction.add("label", ComponentFactory.createText(label));
        this.root.add("exit_action", exitAction);
        return this;
    }

    // --- Build 方法 (保持不变) ---
    @Override
    public JsonObject build() {
        // 首先调用父类的 build 来获取带有 title 等信息的 root 对象
        super.build();

        JsonArray bodyArray = new JsonArray();
        JsonArray inputsArray = new JsonArray();
        JsonArray actionsArray = new JsonArray();

        for (IDialogElement element : this.elements) {
            element.addToParent(bodyArray::add, inputsArray::add, actionsArray::add);
        }

        if (!bodyArray.isEmpty()) {
            this.root.add("body", bodyArray);
        }
        if (!inputsArray.isEmpty()) {
            this.root.add("inputs", inputsArray);
        }
        if (!actionsArray.isEmpty()) {
            this.root.add("actions", actionsArray);
        }

        return this.root;
    }
}