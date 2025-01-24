package net.qilla.destructible.menugeneral.input;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftSign;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SignInput extends PlayerInput {

    private static final int BLOCK_Y_OFFSET = 7;
    private final List<String> signText;
    private final BlockPos blockPos;

    public SignInput(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull List<String> signText) {
        super(plugin, dPlayer);
        Preconditions.checkNotNull(signText, "List cannot be null");
        this.signText = signText;
        this.blockPos = calcBlockPos(dPlayer);
    }

    @Override
    public void init(Consumer<String> onComplete) {
        this.openMenu();
        CompletableFuture.supplyAsync(this::awaitResponse, getExecutor())
                .thenAccept(onComplete).thenRun(this::resetBlockState);
    }

    public void resetBlockState() {
        Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
            getDPlayer().sendPacket(new ClientboundBlockUpdatePacket(blockPos, getDPlayer().getServerLevel().getBlockState(blockPos)));
        });
    }

    public void openMenu() {
        CraftSign<SignBlockEntity> sign = createSign(signText);

        Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
            getDPlayer().sendPacket(new ClientboundBlockUpdatePacket(blockPos, sign.getHandle()));
            getDPlayer().sendPacket(new ClientboundBlockEntityDataPacket(blockPos, BlockEntityType.SIGN, sign.getUpdateNBT()));
            getDPlayer().sendPacket(new ClientboundOpenSignEditorPacket(blockPos, true));
        });
    }

    public @NotNull  CraftSign<SignBlockEntity> createSign(@NotNull List<String> text) {
        Preconditions.checkNotNull(text, "Text cannot be null");

        CraftSign<SignBlockEntity> sign = new CraftSign<>(getDPlayer().getCraftPlayer().getWorld(), new SignBlockEntity(blockPos, Blocks.OAK_SIGN.defaultBlockState()));
        for(int i = 0; i <= 3 && i < text.size(); i++) {
            sign.setLine(i + 1, text.get(i));
        }
        sign.update();
        return sign;
    }

    private @NotNull  BlockPos calcBlockPos(@NotNull DPlayer dPlayer) {
        Preconditions.checkNotNull(dPlayer, "DPlayer cannot be null");

        return CoordUtil.getBlockPos(dPlayer.getPlayer().getLocation()).offset(0, BLOCK_Y_OFFSET, 0);
    }
}