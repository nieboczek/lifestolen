package nieboczek.lifestolen.module;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.serializer.base.ISerializable;

public abstract class Module<C> implements ISerializable<C> {
    public Minecraft mc;
    public C cfg;

    private boolean enabled;

    public static void sendChat(CommandContext<FabricClientCommandSource> ctx, MutableComponent msg) {
        ctx.getSource().sendFeedback(Lifestolen.MSG_PREFIX.copy().append(msg.withColor(0xFFFFFF)));
    }

    public static void sendChat(CommandContext<FabricClientCommandSource> ctx, String msg) {
        ctx.getSource().sendFeedback(Lifestolen.MSG_PREFIX.copy().append(Component.literal(msg).withColor(0xFFFFFF)));
    }

    // TODO: Remove and use custom widgets instead
    public static void sendStatus(Component msg, Minecraft mc) {
        mc.player.displayClientMessage(msg, true);
    }

    public static void sendChat(Component msg, Minecraft mc) {
        mc.player.displayClientMessage(msg, false);
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
        Lifestolen.cfg.enabledModules.put(getId(), enabled);
    }

    public final boolean isEnabled() {
        return enabled;
    }


    public abstract String getId();

    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
    }

    public void tick() {
    }

    public void render2d(GuiGraphics context) {
    }

    public void render3d() {
    }
}
