package nieboczek.lifestolen;

import nieboczek.lifestolen.serializer.base.BooleanSerializer;
import nieboczek.lifestolen.serializer.base.ObjectSerializer;
import nieboczek.lifestolen.serializer.base.Serializer;

public final class ClientConfig {
    public static final String ID = "client";

    public boolean renderClientBrandText;

    public static Serializer<ClientConfig> getSerializer() {
        return ObjectSerializer.of(ClientConfig::new)
                .field("renderClientBrandText", BooleanSerializer.of(), c -> c.renderClientBrandText, (c, v) -> c.renderClientBrandText = v);
    }
}
