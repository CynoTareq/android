package it.cynomys.cfmandroid.auth// Custom serializer for Date
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Date
import java.util.UUID

// Custom KSerializer for java.util.Date to handle its serialization to and from Long (timestamp).
object DateSerializer : KSerializer<Date> {
    // Defines the structure of the serialized form, which is a primitive Long.
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

    // Serializes a Date object to its long timestamp representation.
    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeLong(value.time)
    }

    // Deserializes a long timestamp back into a Date object.
    override fun deserialize(decoder: Decoder): Date {
        return Date(decoder.decodeLong())
    }
}

// Custom KSerializer for java.util.UUID to handle its serialization to and from String.
object UUIDSerializer : KSerializer<UUID> {
    // Defines the structure of the serialized form, which is a primitive String.
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    // Serializes a UUID object to its String representation.
    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    // Deserializes a String back into a UUID object.
    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}
