package me.gamercoder215.socketmc.instruction.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a text element to be displayed on the client's screen, built by a {@link Component}.
 */
public final class PaperText extends Text {

    private Component component = Component.empty();
    private int alpha;

    /**
     * Constructs a new, empty text element.
     */
    public PaperText() {}

    /**
     * Constructs a new text element.
     * @param component Component for Text
     */
    public PaperText(@NotNull Component component) {
        this(component, 0xFF);
    }

    /**
     * Constructs a new text element.
     * @param component Component for Text
     * @param alpha Color Alpha Value
     */
    public PaperText(@NotNull Component component, int alpha) {
        this.component = component;
        this.alpha = alpha;
    }

    /**
     * Gets the text content for this text element.
     * @return Text Content
     */
    @NotNull
    public Component getComponent() {
        return component;
    }

    /**
     * Sets the text content for this text element.
     * @param component Text Component
     * @throws IllegalArgumentException if the text is null
     */
    public void setComponent(@NotNull Component component) throws IllegalArgumentException {
        if (component == null) throw new IllegalArgumentException("Component cannot be null");
        this.component = component;
    }

    /**
     * Gets the alpha value for the color in this text element.
     * @return Alpha Value
     */
    public int getAlpha() {
        return alpha >>> 24;
    }

    /**
     * Sets the alpha value for the color in this text element.
     * @param alpha Alpha Value
     * @throws IllegalArgumentException if the alpha value is not between 0 and 255
     */
    public void setAlpha(int alpha) throws IllegalArgumentException {
        if (alpha < 0 || alpha > 255) throw new IllegalArgumentException("Alpha must be between 0 and 255");
        this.alpha = alpha;
    }

    @Override
    public int getColor() {
        return component.color().value() | (alpha << 24);
    }

    @Override
    public String toJSON() {
        return JSONComponentSerializer.json().serialize(component);
    }
}
