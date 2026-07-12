package nieboczek.lifestolen.config.setting

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import nieboczek.lifestolen.serializer.base.ListSerializer
import nieboczek.lifestolen.serializer.minecraft.ResourceSerializer

class BlockListSetting(name: String, default: MutableList<Block>) :
    Setting<MutableList<Block>>(name, default, ListSerializer(ResourceSerializer(BuiltInRegistries.BLOCK)))
