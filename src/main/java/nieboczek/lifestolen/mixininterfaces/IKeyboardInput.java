package nieboczek.lifestolen.mixininterfaces;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;

public interface IKeyboardInput {
    Input lifestolen$getUnmodified();

    static Input getUnmodified(LocalPlayer player) {
        return ((IKeyboardInput) player.input).lifestolen$getUnmodified();
    }
}
