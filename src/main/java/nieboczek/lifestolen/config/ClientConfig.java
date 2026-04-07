package nieboczek.lifestolen.config;

import nieboczek.lifestolen.serializer.base.*;

import java.util.Map;

public final class ClientConfig {
    public static final String ID = "client";

    public boolean renderClientBrandText;
    public Map<String, Boolean> enabledModules;

    public static Serializer<ClientConfig> getSerializer() {
        return ObjectSerializer.of(ClientConfig::new)
                .field("renderClientBrandText", BooleanSerializer.of(), c -> c.renderClientBrandText, (c, v) -> c.renderClientBrandText = v)
                .field("enabledModules", MapSerializer.of(IdentifierSerializer.of(), BooleanSerializer.of()), c -> c.enabledModules, (c, v) -> c.enabledModules = v);
    }
}
