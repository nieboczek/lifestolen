package nieboczek.friedfabricsvg

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import nieboczek.friedfabricsvg.api.FriedFabricsvgApi
import nieboczek.friedfabricsvg.event.SvgLifecycleListener

@Environment(EnvType.CLIENT)
object FriedFabricSvg : ClientModInitializer {
    override fun onInitializeClient() {
        SvgLifecycleListener.register()
        ClientLifecycleEvents.CLIENT_STARTED.register { _ ->
            FriedFabricsvgApi.initialize()
        }
    }
}
