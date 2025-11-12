package top.sducraft.util.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete builder for creating "notice" type dialogs.
 * Notice dialogs are used to display information with a single confirmation button.
 */
public class NoticeDialogBuilder extends DialogBuilder<NoticeDialogBuilder> {

    /**
     * List of body elements in this dialog.
     */
    private final List<JsonObject> bodyElements = new ArrayList<>();

    /**
     * Creates a new notice dialog builder with a default confirmation button.
     */
    public NoticeDialogBuilder() {
        super("notice");
        this.setConfirmationButton("OK");
    }

    /**
     * Adds a complex text component to the dialog body.
     * Implements the abstract method from the parent class.
     *
     * @param builder A configured TextComponentBuilder.
     * @return This builder for chaining.
     */
    @Override
    public NoticeDialogBuilder addBody(TextComponentBuilder builder) {
        this.bodyElements.add(builder.build());
        return this;
    }

    /**
     * Sets the text for the confirmation button at the bottom of the dialog.
     *
     * @param label The button text.
     * @return This builder for chaining.
     */
    public NoticeDialogBuilder setConfirmationButton(String label) {
        JsonObject action = new JsonObject();
        action.add("label", ComponentFactory.createText(label));
        this.root.add("action", action);
        return this;
    }

    /**
     * Sets the confirmation button using a complex text component.
     *
     * @param labelBuilder A configured TextComponentBuilder instance.
     * @return This builder for chaining.
     */
    public NoticeDialogBuilder setConfirmationButton(TextComponentBuilder labelBuilder) {
        JsonObject action = new JsonObject();
        action.add("label", labelBuilder.buildContents());
        this.root.add("action", action);
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

        if (!this.bodyElements.isEmpty()) {
            JsonArray bodyArray = new JsonArray();
            this.bodyElements.forEach(bodyArray::add);
            this.root.add("body", bodyArray);
        }

        return this.root;
    }
}

