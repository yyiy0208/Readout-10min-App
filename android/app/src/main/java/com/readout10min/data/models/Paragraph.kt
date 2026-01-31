package com.readout10min.data.models

import java.time.Instant
import java.util.*
import kotlinx.serialization.Contextual

@kotlinx.serialization.Serializable
data class Paragraph(
    @Contextual val id: UUID = UUID.randomUUID(),
    @Contextual val content_id: UUID,
    val paragraph_number: Int,
    val text: String,
    val word_count: Int,
    val estimated_duration: Int,
    val audio_url: String? = null,
    @Contextual val created_at: Instant = Instant.now(),
    @Contextual val updated_at: Instant = Instant.now()
)
