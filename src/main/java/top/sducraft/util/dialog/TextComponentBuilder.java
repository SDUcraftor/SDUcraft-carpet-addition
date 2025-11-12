package top.sducraft.util.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * A builder for constructing complex Minecraft JSON text components.
 * Generates the correctly formatted flattened style structure required for dialog plain_message elements.
 */
public class TextComponentBuilder {

    private final JsonArray parts = new JsonArray();
    private JsonObject currentPart;
    private int width = -1;

    /**
     * Creates an empty TextComponentBuilder.
     */
    public TextComponentBuilder() {

    }

    /**
     * Creates a TextComponentBuilder with initial text.
     *
     * @param initialText The initial text fragment.
     */
    public TextComponentBuilder(String initialText) {
        this.append(initialText);
    }

    /**
     * Appends a new text fragment. Subsequent style methods will apply to this fragment.
     *
     * @param text The text to append.
     * @return This builder for chaining.
     */
    public TextComponentBuilder append(String text) {
        this.currentPart = new JsonObject();
        this.currentPart.addProperty("text", text);
        this.parts.add(this.currentPart);
        return this;
    }

    /**
     * Sets the color for the current text fragment.
     *
     * @param color The color name (e.g., "red", "gold") or hex code (e.g., "#FF55FF").
     * @return This builder for chaining.
     */
    public TextComponentBuilder color(String color) {
        ensureCurrentPartExists();
        this.currentPart.addProperty("color", color);
        return this;
    }

    /**
     * Sets the current text fragment to bold.
     *
     * @return This builder for chaining.
     */
    public TextComponentBuilder bold() {
        ensureCurrentPartExists();
        this.currentPart.addProperty("bold", true);
        return this;
    }

    /**
     * Sets a click event for the current text fragment to execute a command.
     *
     * @param command The command to execute.
     * @return This builder for chaining.
     */
    public TextComponentBuilder onClickRunCommand(String command) {
        ensureCurrentPartExists();
        JsonObject clickEvent = new JsonObject();
        clickEvent.addProperty("action", "run_command");
        clickEvent.addProperty("command", command);
        this.currentPart.add("click_event", clickEvent);
        return this;
    }

    /**
     * Sets a click event for the current text fragment to open a URL.
     *
     * @param url The URL to open.
     * @return This builder for chaining.
     */
    public TextComponentBuilder onClickOpenUrl(String url) {
        ensureCurrentPartExists();
        JsonObject clickEvent = new JsonObject();
        clickEvent.addProperty("action", "open_url");
        clickEvent.addProperty("value", url);
        this.currentPart.add("click_event", clickEvent);
        return this;
    }

    /**
     * Ensures that a text fragment exists before applying styles or events.
     *
     * @throws IllegalStateException if no text fragment has been created yet.
     */
    private void ensureCurrentPartExists() {
        if (this.currentPart == null) {
            throw new IllegalStateException("You must call .append(text) before applying any styles or click events.");
        }
    }

    /**
     * Sets the width of the text component.
     *
     * @param width The width value (must be >= 1).
     * @return This builder for chaining.
     */
    public TextComponentBuilder withWidth(int width) {
        if (width >= 1) {
            this.width = width;
        }
        return this;
    }

    /**
     * Builds the complete JsonObject for a 'plain_message' body element.
     *
     * @return A JsonObject representing the complete 'plain_message' body element.
     */
    public JsonObject build() {
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", "plain_message");

        if (this.width > 0) {
            wrapper.addProperty("width", this.width);
        }

        wrapper.add("contents", this.buildContents());
        return wrapper;
    }

    /**
     * Builds the text component contents that can be used in various contexts,
     * such as button labels or body messages.
     *
     * @return A JsonElement representing the text component (JsonObject or JsonArray).
     */
    public com.google.gson.JsonElement buildContents() {
        if (this.parts.isEmpty()) {
            return new JsonObject();
        } else if (this.parts.size() == 1) {
            return this.parts.get(0);
        } else {
            return this.parts;
        }
    }
}

