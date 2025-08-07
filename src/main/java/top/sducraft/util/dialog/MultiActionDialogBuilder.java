package top.sducraft.util.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 一个用于创建 "multi_action" 类型对话框的具体构建器。
 */
public class MultiActionDialogBuilder extends DialogBuilder<MultiActionDialogBuilder> {

    private final JsonArray actions = new JsonArray();
    private final JsonArray inputs = new JsonArray();

    public MultiActionDialogBuilder() {
        super("multi_action");
    }

    /**
     * 设置操作按钮的布局列数。
     * @param columns 列数。
     */
    public MultiActionDialogBuilder setColumns(int columns) {
        this.root.addProperty("columns", columns);
        return this;
    }

    /**
     * 添加一个文本输入框。
     * @param key   用于在命令模板中引用的唯一键。
     * @param label 输入框上方显示的标签文本。
     */
    public MultiActionDialogBuilder addTextInput(String key, String label) {
        JsonObject input = new JsonObject();
        input.addProperty("key", key);
        input.addProperty("type", "text");
        input.add("label", ComponentFactory.createText(label));
        this.inputs.add(input);
        return this;
    }

    /**
     * 添加一个通过 ActionBuilder 构建的、完全自定义的操作按钮。
     * 这是添加按钮的推荐方式。
     * @param actionBuilder 一个已经配置好的 ActionBuilder 实例。
     */
    public MultiActionDialogBuilder addAction(ActionBuilder actionBuilder) {
        this.actions.add(actionBuilder.build());
        return this;
    }

    /**
     * 在对话框底部添加一个退出/取消按钮。
     * @param label 按钮上显示的文本。
     */
    public MultiActionDialogBuilder addExitButton(String label) {
        JsonObject exitAction = new JsonObject();
        exitAction.add("label", ComponentFactory.createText(label));
        // 默认行为是关闭对话框，由父类 DialogBuilder 设置，这里只需定义标签
        this.root.add("exit_action", exitAction);
        return this;
    }

    @Override
    public JsonObject build() {
        if (actions.size() > 0) {
            this.root.add("actions", actions);
        }
        if (inputs.size() > 0) {
            this.root.add("inputs", inputs);
        }
        return super.build();
    }
}