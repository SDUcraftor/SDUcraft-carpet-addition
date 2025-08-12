package top.sducraft.util.dialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个用于创建 "notice" 类型对话框的具体构建器。
 * notice 对话框用于显示信息，并带有一个确认按钮。
 */
public class NoticeDialogBuilder extends DialogBuilder<NoticeDialogBuilder> {

    // 这个构建器自己管理 body 元素，与 MultiActionDialogBuilder 的设计保持一致
    private final List<JsonObject> bodyElements = new ArrayList<>();

    public NoticeDialogBuilder() {
        super("notice");
        // 为确保对话框始终有效，在创建时就设置一个默认的确认按钮
        this.setConfirmationButton("好的");
    }

    /**
     * [实现] 实现父类的抽象方法，添加一个复杂的文本主体。
     * @param builder 一个配置好的 TextComponentBuilder。
     */
    @Override
    public NoticeDialogBuilder addBody(TextComponentBuilder builder) {
        // 将构建好的 body 元素添加到自己的列表中
        this.bodyElements.add(builder.build());
        return this;
    }

    /**
     * 设置底部确认按钮的文本。
     *
     * @param label 按钮上显示的文本。
     */
    public void setConfirmationButton(String label) {
        JsonObject action = new JsonObject();
        action.add("label", ComponentFactory.createText(label));
        // notice 对话框的 action 是单个对象，直接添加到根对象
        this.root.add("action", action);
    }

    /**
     * [重写] 构建最终的 JsonObject。
     */
    @Override
    public JsonObject build() {
        // 首先调用父类的 build 获取带有 title 等信息的 root 对象
        super.build();

        // 然后，将自己列表中的 body 元素添加到 root
        if (!this.bodyElements.isEmpty()) {
            JsonArray bodyArray = new JsonArray();
            this.bodyElements.forEach(bodyArray::add);
            this.root.add("body", bodyArray);
        }

        return this.root;
    }
}
