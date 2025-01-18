package net.qilla.destructible.command;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.Rarity;
import net.qilla.destructible.data.DRegistry;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import java.util.List;

public class TestCommand {
    private static final String COMMAND = "test";
    private static final List<String> ALIAS = List.of();
    private final Destructible plugin;
    private final Commands commands;

    public TestCommand(Destructible plugin, Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands.literal(COMMAND)
                .requires(source -> source.getSender() instanceof Player)
                .executes(this::test).build(), ALIAS);
    }

    private int test(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Registry.MATERIAL.stream().filter(Material::isItem).forEach(material -> {
                DItem dItem = new DItem.Builder()
                        .id(material.toString())
                        .displayName(Component.text(FormatUtil.toName(material.toString())))
                        .material(material)
                        .stackSize(64)
                        .rarity(Rarity.COMMON)
                        .resource(!material.isBlock())
                        .build();
                DRegistry.DESTRUCTIBLE_ITEMS.put(dItem.getId(), dItem);
            });
            Registry.MATERIAL.stream().filter(material -> material.isBlock() && material.isItem()).forEach(material -> {
                ItemDrop itemDrop = new ItemDrop.Builder()
                        .dItem(DRegistry.DESTRUCTIBLE_ITEMS.get(material.toString()))
                        .chance(1)
                        .amount(1)
                        .build();
                DBlock dBlock = new DBlock.Builder()
                        .id(material.toString())
                        .material(material)
                        .strength(1)
                        .durability(240)
                        .cooldown(300000)
                        .lootpool(List.of(itemDrop))
                        .breakSound(material.createBlockData().getSoundGroup().getBreakSound())
                        .breakParticle(material)
                        .build();
                DRegistry.DESTRUCTIBLE_BLOCKS.put(dBlock.getId(), dBlock);
            });
        });
        return Command.SINGLE_SUCCESS;
    }
}