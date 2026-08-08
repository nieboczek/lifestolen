package nieboczek.lifestolen.config.serializer.base

import nieboczek.lifestolen.config.serializer.SerializerException
import nieboczek.lifestolen.config.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.config.serializer.lang.TokenStream
import nieboczek.lifestolen.config.serializer.lang.TokenType

class IntRangeSerializer : Serializer<IntRange>() {
    override fun serialize(value: IntRange, builder: SerializedStringBuilder) {
        builder.text(value.first.toString()).text("..").text(value.last.toString())
    }

    override fun deserialize(stream: TokenStream): IntRange {
        val startText = stream.nextTokenText(TokenType.NUMBER)
        val start: Int
        try {
            start = startText.toInt()
        } catch (e: NumberFormatException) {
            throw SerializerException("Invalid int value for start: \"$startText\"", e)
        }

        stream.expect(TokenType.RANGE_INFIX)

        val endText = stream.nextTokenText(TokenType.NUMBER)
        val end: Int
        try {
            end = endText.toInt()
        } catch (e: NumberFormatException) {
            throw SerializerException("Invalid int value for end: \"$endText\"", e)
        }

        return start..end
    }
}