package nieboczek.lifestolen.config;

import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.module.Module;
import nieboczek.lifestolen.serializer.base.Serializer;
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MainConfigSerializer extends Serializer<List<Module<?>>> {
    private final Map<String, Serializer<Object>> moduleSerializers = new HashMap<>();
    private final Map<String, Integer> moduleIndices = new HashMap<>();

    @SuppressWarnings("unchecked")
    public MainConfigSerializer(List<Module<?>> modules) {
        for (int i = 0; i < modules.size(); i++) {
            Module<?> module = modules.get(i);
            moduleSerializers.put(module.getId(), (Serializer<Object>) module.getSerializer());
            moduleIndices.put(module.getId(), i);
        }
        moduleSerializers.put(ClientConfig.ID, (Serializer<Object>) (Object) ClientConfig.getSerializer());
    }

    @Override
    public void serialize(List<Module<?>> value, SerializedStringBuilder builder) {
        builder.text('{').newLine();
        builder.indent();

        // Client config
        builder.indented().text(ClientConfig.ID).text(" = ");
        moduleSerializers.get(ClientConfig.ID).serialize(Lifestolen.cfg, builder);
        builder.text(';').newLine();

        for (Module<?> module : value) {
            Serializer<Object> serializer = moduleSerializers.get(module.getId());
            if (serializer == null) continue;
            builder.indented().text(module.getId()).text(" = ");
            serializer.serialize(module.cfg, builder);
            builder.text(';').newLine();
        }
        builder.unindent();
        builder.indented().text("};");
    }

    @Override
    public List<Module<?>> deserialize(TokenStream stream) {
        throw new UnsupportedOperationException("Use deserialize(String, ArrayList<Module<?>>) instead");
    }

    public void deserialize(String source, List<Module<?>> modules) {
        TokenStream stream = new TokenStream(source);
        stream.expect(TokenType.L_BRACE);
        while (stream.peek().type() != TokenType.R_BRACE) {
            String moduleId = stream.nextTokenText(TokenType.IDENTIFIER);
            stream.expect(TokenType.EQUAL);
            Serializer<Object> serializer = moduleSerializers.get(moduleId);
            if (serializer != null) {
                Object config = serializer.deserialize(stream);
                Lifestolen.LOG.info("Deserialized {}", moduleId);
                if (moduleId.equals(ClientConfig.ID)) {
                    Lifestolen.cfg = (ClientConfig) config;
                } else {
                    int index = moduleIndices.get(moduleId);
                    @SuppressWarnings("unchecked")
                    Module<Object> module = (Module<Object>) modules.get(index);
                    module.cfg = config;
                }
            }
            stream.expect(TokenType.SEMICOLON);
        }
        stream.expect(TokenType.R_BRACE);
        stream.expect(TokenType.SEMICOLON);
        stream.expect(TokenType.EOF);
    }
}
