package nieboczek.lifestolen.util

/** "Attack Only Players" -> "AttackOnlyPlayers" */
fun String.titleCaseToPascalCase(): String = split(" ").joinToString("")
