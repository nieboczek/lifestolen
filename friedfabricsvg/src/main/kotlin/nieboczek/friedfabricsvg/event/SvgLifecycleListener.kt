package nieboczek.friedfabricsvg.event

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import nieboczek.friedfabricsvg.api.FriedFabricsvgApi
import nieboczek.friedfabricsvg.parse.SvgDocumentCache

object SvgLifecycleListener {
    fun register() {
        ClientLifecycleEvents.CLIENT_STARTED.register { _ ->
            val mc = Minecraft.getInstance()
            val resourceManager = mc.resourceManager
            if (resourceManager is net.minecraft.server.packs.resources.ReloadableResourceManager) {
                resourceManager.registerReloadListener(
                    ResourceManagerReloadListener { _ ->
                        FriedFabricsvgApi.invalidateAll()
                        SvgDocumentCache.clear()
                    }
                )
            }
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            FriedFabricsvgApi.invalidateAll()
            SvgDocumentCache.clear()
        }
    }
}
