package top.sducraft.util.dialog;

import com.google.gson.JsonObject;

/**
 * Abstract base class for dialog builders.
 * Uses the Curiously Recurring Template Pattern (CRTP) to provide fluent method chaining in subclasses.
 *
 * @param <T> The concrete builder subclass type.
 */
public abstract class DialogBuilder<T extends DialogBuilder<T>> {
    protected final JsonObject root;

    /**
     * Creates a new dialog builder with the specified type.
     *
     * @param type The dialog type identifier.
     */
    protected DialogBuilder(String type) {
        this.root = new JsonObject();
        this.root.addProperty("type", type);
        this.setAfterAction("close");
        this.setCanCloseWithEscape(true);
        this.setPauseGame(false);
    }

    /**
     * Sets the title of the dialog.
     *
     * @param title The title text.
     * @return This builder for chaining.
     */
    public T setTitle(String title) {
        this.root.add("title", ComponentFactory.createText(title));
        return self();
    }

    /**
     * Adds a simple text line to the dialog body.
     * This is a convenience method that internally calls addBody.
     *
     * @param text The text to add.
     * @return This builder for chaining.
     * @deprecated Use {@link #addBody(TextComponentBuilder)} for better control over text styling.
     */
    @Deprecated
    public T addBodyText(String text) {
        return this.addBody(new TextComponentBuilder(text));
    }

    /**
     * Adds a complex text component to the dialog body.
     * This method must be implemented by subclasses.
     *
     * @param builder A configured TextComponentBuilder.
     * @return This builder for chaining.
     */
    public abstract T addBody(TextComponentBuilder builder);

    /**
     * Builds and returns the final JsonObject representing this dialog.
     *
     * @return The complete dialog JsonObject.
     */
    public JsonObject build() {
        return root;
    }

    /**
     * Returns this builder instance with the proper generic type.
     * Used internally to support method chaining in the CRTP pattern.
     *
     * @return This builder instance.
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    /**
     * Sets the behavior after an action is executed.
     *
     * @param afterAction "close", "none", or "wait_for_response".
     * @return This builder for chaining.
     */
    public T setAfterAction(String afterAction) {
        this.root.addProperty("after_action", afterAction);
        return self();
    }

    /**
     * Sets whether the dialog can be closed with the Escape key.
     *
     * @param canClose true if the dialog can be closed with Escape, false otherwise.
     * @return This builder for chaining.
     */
    public T setCanCloseWithEscape(boolean canClose) {
        this.root.addProperty("can_close_with_escape", canClose);
        return self();
    }

    /**
     * Sets whether the game should be paused while the dialog is open.
     *
     * @param pause true to pause the game, false otherwise.
     * @return This builder for chaining.
     */
    public T setPauseGame(boolean pause) {
        this.root.addProperty("pause", pause);
        return self();
    }
}

