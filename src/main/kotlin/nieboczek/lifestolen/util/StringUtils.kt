package nieboczek.lifestolen.util

object StringUtils {
    /** "Attack Only Players" -> "AttackOnlyPlayers" */
    fun titleCaseToPascalCase(string: String): String {
        val parts = string.split(" ")
        return parts.joinToString("")
    }
}
