package nieboczek.lifestolen.config.serializer

class SerializerException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}