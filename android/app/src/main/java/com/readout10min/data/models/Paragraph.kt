package com.readout10min.data.models

import java.time.Instant
import java.util.*
import kotlinx.serialization.Serializable
import com.readout10min.data.serializers.UUIDSerializer
import com.readout10min.data.serializers.InstantSerializer

@Serializable
data class Paragraph(
    @Serializable(with = UUIDSerializer::class) val id: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class) val content_id: UUID,
    val paragraph_number: Int,
    val text: String,
    val word_count: Int,
    val estimated_duration: Int,
    val audio_url: String? = null,
    @Serializable(with = InstantSerializer::class) val created_at: Instant = Instant.now(),
    @Serializable(with = InstantSerializer::class) val updated_at: Instant = Instant.now()
)
