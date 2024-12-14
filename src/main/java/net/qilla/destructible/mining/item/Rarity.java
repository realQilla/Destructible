package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum Rarity {
    COMMON(MiniMessage.miniMessage().deserialize("<!italic><white><bold>COMMON")),
    UNUSUAL(MiniMessage.miniMessage().deserialize("<!italic><yellow><bold>UNUSUAL")),
    RARE(MiniMessage.miniMessage().deserialize("<!italic><blue><bold>RARE")),
    LEGENDARY(MiniMessage.miniMessage().deserialize("<!italic><gold><bold>LEGENDARY"));

    private final Component component;

    Rarity(Component component) {
        this.component = component;
    }

    public Component getFormatted() {
        return this.component;
    }
}
