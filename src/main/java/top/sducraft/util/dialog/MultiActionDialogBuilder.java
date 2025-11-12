package top.sducraft.util.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A concrete builder for creating "multi_action" type dialogs.
 * This type of dialog can contain multiple body elements, inputs, and action buttons.
 */
public class MultiActionDialogBuilder extends DialogBuilder<MultiActionDialogBuilder> {

    /**
     * Internal interface for dialog elements.
     */
    private interface IDialogElement {
        void addToParent(Consumer<JsonObject> bodyConsumer, Consumer<JsonObject> inputConsumer, Consumer<JsonObject> actionConsumer);
    }

    /**
     * List of dialog elements added to this builder.
     */
    private final List<IDialogElement> elements = new ArrayList<>();

    /**
     * Creates a new multi-action dialog builder.
     */
    public MultiActionDialogBuilder() {
        super("multi_action");
    }

    /**
     * Adds a text component to the dialog body.
     * Implements the abstract method from the parent class.
     *
     * @param builder A configured TextComponentBuilder.
     * @return This builder for chaining.
     */
    @Override
    public MultiActionDialogBuilder addBody(TextComponentBuilder builder) {
        JsonObject bodyJson = builder.build();
        this.elements.add((body, input, action) -> body.accept(bodyJson));
        return this;
    }

    /**
     * Sets the number of columns for the action button layout.
     *
     * @param columns The number of columns.
     * @return This builder for chaining.
     */
    public MultiActionDialogBuilder setColumns(int columns) {
        this.root.addProperty("columns", columns);
        return this;
    }

    /**
     * Adds a text input field to the dialog.
     *
     * @param key The key to identify the input value.
     * @param label The label text to display.
     * @return This builder for chaining.
     */
    public MultiActionDialogBuilder addTextInput(String key, String label) {
        JsonObject inputJson = new JsonObject();
        inputJson.addProperty("key", key);
        inputJson.addProperty("type", "text");
        inputJson.add("label", ComponentFactory.createText(label));
        this.elements.add((body, input, action) -> input.accept(inputJson));
        return this;
    }

    /**
     * Adds an action button to the dialog.
     *
     * @param actionBuilder A configured ActionBuilder.
     * @return This builder for chaining.
     */
    public MultiActionDialogBuilder addAction(ActionBuilder actionBuilder) {
        JsonObject actionJson = actionBuilder.build();
        this.elements.add((body, input, action) -> action.accept(actionJson));
        return this;
    }

    /**
     * Adds an exit button to close the dialog.
     *
     * @param label The text to display on the exit button.
     * @return This builder for chaining.
     */
    public MultiActionDialogBuilder addExitButton(String label) {
        JsonObject exitAction = new JsonObject();
        exitAction.add("label", ComponentFactory.createText(label));
        this.root.add("exit_action", exitAction);
        return this;
    }

    /**
     * Builds and returns the final JsonObject representing this dialog.
     *
     * @return The complete dialog JsonObject.
     */
    @Override
    public JsonObject build() {
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

