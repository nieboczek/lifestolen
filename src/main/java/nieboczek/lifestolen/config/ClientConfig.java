package nieboczek.lifestolen.config;

import nieboczek.lifestolen.serializer.base.*;

import java.util.Map;

/// Access instance at {@link nieboczek.lifestolen.Lifestolen#cfg}
public final class ClientConfig {
    public static final String ID = "client";

    public boolean renderClientBrandText;
    public float textScale = 1f;
    public Map<String, Boolean> enabledModules;

    public static Serializer<ClientConfig> getSerializer() {
        return ClassSerializer.of(ClientConfig::new)
                .field("renderClientBrandText", BooleanSerializer.of(), c -> c.renderClientBrandText, (c, v) -> c.renderClientBrandText = v)
                .field("textScale", FloatSerializer.of(), c -> c.textScale, (c, v) -> c.textScale = v)
                .field("enabledModules", MapSerializer.of(IdentifierSerializer.of(), BooleanSerializer.of()), c -> c.enabledModules, (c, v) -> c.enabledModules = v);
    }
}
