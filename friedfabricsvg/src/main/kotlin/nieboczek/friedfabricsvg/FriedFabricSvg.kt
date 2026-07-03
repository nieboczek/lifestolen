@file:JvmName("FriedFabricSvg")

package nieboczek.friedfabricsvg

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import nieboczek.friedfabricsvg.api.FriedFabricsvgApi
import nieboczek.friedfabricsvg.event.SvgLifecycleListener

@Environment(EnvType.CLIENT)
object FriedFabricSvg : ClientModInitializer {
    override fun onInitializeClient() {
        FriedFabricsvgApi.initialize()
        SvgLifecycleListener.register()
    }
}
