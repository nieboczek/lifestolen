package nieboczek.lifestolen.module;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nieboczek.lifestolen.Renderer3d;
import nieboczek.lifestolen.serializer.base.BooleanSerializer;
import nieboczek.lifestolen.serializer.base.DoubleSerializer;
import nieboczek.lifestolen.serializer.base.ObjectSerializer;
import nieboczek.lifestolen.serializer.base.Serializer;

import java.util.List;
import java.util.Random;

public final class KillAuraModule extends Module<KillAuraModule.Config> {
    private static final AABB BIG_AABB = new AABB(-65_535.0, -65_535.0, -65_535.0, 65_535.0, 65_535.0, 65_535.0);

    private final Random random = new Random();

    @Override
    public void tick() {
        Entity target = getNearestEnemy();
        if (target == null || !mc.player.gameMode().isSurvival()) return;
        if (mc.player.isBlocking()) return; // don't attack if shield blocking

        if (mc.player.getAttackStrengthScale(0.5f) >= 0.95) {
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public String getId() {
        return "killAura";
    }

    @Override
    public String getDisplayName() {
        return "KillAura";
    }

    @Override
    public Serializer<Config> getSerializer() {
        return ObjectSerializer.of(Config::new)
                .field("range", DoubleSerializer.of(), c -> c.range, (c, v) -> c.range = v)
                .field("attackOnlyPlayers", BooleanSerializer.of(), c -> c.attackOnlyPlayers, (c, v) -> c.attackOnlyPlayers = v);
    }

    @Override
    public void render3d() {
        Vec3 partialTickPos = Renderer3d.computePartialTickPos(
                mc.player.oldPosition(),
                mc.player.position(),
                mc.getEntityRenderDispatcher().camera.position()
        ).add(0.0, 1.0, 0.0);

        Renderer3d.renderCircleOutline(64, 0xFFFFFFFF, (float) cfg.range, partialTickPos);
    }

    private Entity getNearestEnemy() {
        Entity best = null;
        double bestDistSq = cfg.range * cfg.range;

        List<? extends Entity> entities = mc.player.level().getEntities(mc.player, BIG_AABB, $ -> true);

        for (Entity entity : entities) {
            boolean attackPlayer = entity instanceof Player;
            boolean attackLivingEntity = !cfg.attackOnlyPlayers && entity instanceof LivingEntity;

            if ((attackPlayer || attackLivingEntity) && entity != mc.player && entity.isAlive()) {
                double distSq = mc.player.distanceToSqr(entity);
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    best = entity;
                }
            }
        }

        return best;
    }

    public static final class Config {
        public double range;
        public boolean attackOnlyPlayers;
    }
}
