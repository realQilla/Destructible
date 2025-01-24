package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menugeneral.MaterialRepresentation;
import org.bukkit.Material;

public enum Rarity implements MaterialRepresentation {
    NONE(MiniMessage.miniMessage().deserialize("<red>None"), NamedTextColor.DARK_GRAY, Material.BARRIER),
    COMMON(MiniMessage.miniMessage().deserialize("<!italic><white><bold>COMMON"), NamedTextColor.WHITE, Material.QUARTZ),
    UNIQUE(MiniMessage.miniMessage().deserialize("<!italic><yellow><bold>UNIQUE"), NamedTextColor.YELLOW, Material.IRON_INGOT),
    RARE(MiniMessage.miniMessage().deserialize("<!italic><blue><bold>RARE"), NamedTextColor.BLUE, Material.GOLD_INGOT),
    LEGENDARY(MiniMessage.miniMessage().deserialize("<!italic><gold><bold>LEGENDARY"), NamedTextColor.GOLD, Material.RESIN_BRICK),
    FABLED(MiniMessage.miniMessage().deserialize("<!italic><light_purple><bold>FABLED"), NamedTextColor.LIGHT_PURPLE, Material.NETHERITE_INGOT);

    private final Component component;
    private final TextColor color;
    private final Material representation;

    Rarity(Component component, TextColor color, Material representation) {
        this.component = component;
        this.color = color;
        this.representation = representation;
    }

    public Component getComponent() {
        return this.component;
    }

    public TextColor getTextColor() {
        return this.color;
    }

    public Material getRepresentation() {
        return this.representation;
    }
}
