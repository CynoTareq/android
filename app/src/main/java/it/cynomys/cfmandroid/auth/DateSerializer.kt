package it.cynomys.cfmandroid.auth// Custom serializer for Date
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.UUID


// Custom KSerializer for java.util.Date to handle its serialization to and from ISO 8601 String.
// Used for ProfileDto (GET response) which may contain full date-time.
object DateSerializer : KSerializer<Date> {

    // Define the ISO 8601 format used by the backend for full date-time fields
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").apply {
        // Enforce UTC timezone for 'Z' suffix compatibility
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    // Serializes Date to ISO 8601 String
    override fun serialize(encoder: Encoder, value: Date) {
        val dateString = synchronized(dateFormat) {
            dateFormat.format(value)
        }
        encoder.encodeString(dateString)
    }

    // Deserializes ISO 8601 String back to Date
    override fun deserialize(decoder: Decoder): Date {
        val dateString = decoder.decodeString()
        return synchronized(dateFormat) {
            dateFormat.parse(dateString)
        }
    }
}

// FIX: Utility object for manual Date <-> String conversion for date-only fields ("yyyy-MM-dd").
object DateOnlySerializer {
    // Define the date-only format supported by the backend
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd").apply {
        // Use UTC to ensure date parsing is consistent
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // Utility function to format a Date into the required "yyyy-MM-dd" string for the request body.
    fun formatDate(date: Date): String {
        return synchronized(dateFormat) {
            dateFormat.format(date)
        }
    }

    // Utility function to parse a "yyyy-MM-dd" string from the response back into a Date for the model.
    fun parseDate(dateString: String): Date {
        return synchronized(dateFormat) {
            dateFormat.parse(dateString)
        }
    }
}

// Custom KSerializer for java.util.UUID
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}