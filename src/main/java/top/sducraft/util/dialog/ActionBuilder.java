package top.sducraft.util.dialog;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

/**
 * A builder for constructing dialog actions (buttons).
 * Provides a fluent API to define labels, tooltips, width, and click behavior.
 */
public class ActionBuilder {

    private final JsonObject container;
    private JsonObject actionPayload;

    /**
     * Private constructor for creating instances via the static factory method.
     *
     * @param labelElement The pre-built JsonElement representing the label.
     */
    private ActionBuilder(com.google.gson.JsonElement labelElement) {
        this.container = new JsonObject();
        this.container.add("label", labelElement);
    }

    /**
     * Creates a new ActionBuilder with a simple text label.
     *
     * @param labelText The text to display on the button.
     */
    public ActionBuilder(String labelText) {
        this.container = new JsonObject();
        this.container.add("label", ComponentFactory.createText(labelText));
    }

    /**
     * Creates an ActionBuilder with a complex styled label using a TextComponentBuilder.
     *
     * @param labelBuilder A configured TextComponentBuilder instance.
     * @return A new ActionBuilder with the complex label.
     */
    public static ActionBuilder withComplexLabel(TextComponentBuilder labelBuilder) {
        return new ActionBuilder(labelBuilder.buildContents());
    }

    /**
     * Sets a tooltip to display when hovering over the button.
     *
     * @param tooltip The tooltip text.
     * @return This builder for chaining.
     */
    public ActionBuilder withTooltip(String tooltip) {
        if (tooltip != null && !tooltip.isEmpty()) {
            this.container.add("tooltip", ComponentFactory.createText(tooltip));
        }
        return this;
    }

    /**
     * Sets the width of the button.
     *
     * @param width The width in pixels.
     * @return This builder for chaining.
     */
    public ActionBuilder withWidth(int width) {
        this.container.addProperty("width", width);
        return this;
    }

    /**
     * Configures this action to run a command when clicked.
     *
     * @param command The command to execute.
     * @return This builder for chaining.
     */
    public ActionBuilder asRunCommand(String command) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "run_command");
        this.actionPayload.addProperty("command", command);
        return this;
    }

    /**
     * Configures this action to suggest a command in the chat when clicked.
     *
     * @param command The command to suggest.
     * @return This builder for chaining.
     */
    public ActionBuilder asSuggestCommand(String command) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "suggest_command");
        this.actionPayload.addProperty("command", command);
        return this;
    }

    /**
     * Configures this action to open a URL when clicked.
     *
     * @param url The URL to open.
     * @return This builder for chaining.
     */
    public ActionBuilder asOpenUrl(String url) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "open_url");
        this.actionPayload.addProperty("url", url);
        return this;
    }

    /**
     * Configures this action to run a dynamic command generated from a template.
     *
     * @param template The command template.
     * @return This builder for chaining.
     */
    public ActionBuilder asDynamicRunCommand(String template) {
        this.actionPayload = new JsonObject();
        this.actionPayload.addProperty("type", "dynamic/run_command");
        this.actionPayload.addProperty("template", template);
        return this;
    }

    /**
     * Configures this action with a custom dynamic behavior.
     *
     * @param id The custom action identifier.
     * @param additions Additional data for the custom action (nullable).
     * @return This builder for chaining.
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
     * Builds and returns the final JsonObject representing this action.
     *
     * @return The complete action JsonObject.
     * @throws IllegalStateException if no action type has been set.
     */
    public JsonObject build() {
        if (this.actionPayload == null) {
            throw new IllegalStateException("Action type was not set. You must call one of the 'as...' methods before building.");
        }
        this.container.add("action", this.actionPayload);
        return this.container;
    }
}