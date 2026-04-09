package nieboczek.lifestolen.module;

import nieboczek.lifestolen.serializer.base.IntegerSerializer;
import nieboczek.lifestolen.serializer.base.ObjectSerializer;
import nieboczek.lifestolen.serializer.base.Serializer;

public final class FakeLagModule extends Module<FakeLagModule.Config> {
    public static FakeLagModule INSTANCE;

    public FakeLagModule() {
        INSTANCE = this;
    }

    @Override
    public String getId() {
        return "fakeLag";
    }

    @Override
    public String getDisplayName() {
        return "FakeLag";
    }

    @Override
    public Category getCategory() {
        return Category.MOVEMENT;
    }

    @Override
    public Serializer<Config> getSerializer() {
        return ObjectSerializer.of(Config::new)
                .field("delayMsMin", IntegerSerializer.of(), c -> c.delayMsMin, (c, v) -> c.delayMsMin = v)
                .field("delayMsMax", IntegerSerializer.of(), c -> c.delayMsMax, (c, v) -> c.delayMsMax = v)
                .field("recoilTimeMs", IntegerSerializer.of(), c -> c.recoilTimeMs, (c, v) -> c.recoilTimeMs = v);
    }

    public static final class Config {
        public int delayMsMin;
        public int delayMsMax;
        public int recoilTimeMs;
    }
}
