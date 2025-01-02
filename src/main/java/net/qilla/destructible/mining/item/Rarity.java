package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum Rarity {
    NONE(Component.empty(), NamedTextColor.GRAY),
    COMMON(MiniMessage.miniMessage().deserialize("<!italic><white><bold>COMMON"), NamedTextColor.WHITE),
    UNIQUE(MiniMessage.miniMessage().deserialize("<!italic><yellow><bold>UNIQUE"), NamedTextColor.YELLOW),
    RARE(MiniMessage.miniMessage().deserialize("<!italic><blue><bold>RARE"), NamedTextColor.BLUE),
    LEGENDARY(MiniMessage.miniMessage().deserialize("<!italic><gold><bold>LEGENDARY"), NamedTextColor.GOLD),;

    private final Component component;
    private final TextColor color;

    Rarity(Component component, TextColor color) {
        this.component = component;
        this.color = color;
    }

    public Component getComponent() {
        return this.component;
    }

    public TextColor getTextColor() {
        return this.color;
    }
}
