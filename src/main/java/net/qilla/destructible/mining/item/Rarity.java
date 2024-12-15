package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.awt.*;

public enum Rarity {
    COMMON(MiniMessage.miniMessage().deserialize("<!italic><white><bold>COMMON"), NamedTextColor.WHITE),
    UNUSUAL(MiniMessage.miniMessage().deserialize("<!italic><yellow><bold>UNUSUAL"), NamedTextColor.YELLOW),
    RARE(MiniMessage.miniMessage().deserialize("<!italic><blue><bold>RARE"), NamedTextColor.BLUE),
    LEGENDARY(MiniMessage.miniMessage().deserialize("<!italic><gold><bold>LEGENDARY"), NamedTextColor.GOLD),;

    private final Component component;
    private final TextColor color;

    Rarity(Component component, TextColor color) {
        this.component = component;
        this.color = color;
    }

    public Component getFormatted() {
        return this.component;
    }

    public TextColor getColor() {
        return this.color;
    }
}
