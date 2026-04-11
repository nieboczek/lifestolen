package nieboczek.lifestolen.module

object FakeLagModule : Module("FakeLag", Category.MOVEMENT) {
    val delay by intRange("Delay", 300..600, 0..1000, "ms")
    val recoilTime by int("Recoil Time", 200, 0..1000, "ms")
}
