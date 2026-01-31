package com.readout10min.data.models

import java.time.Instant
import java.util.*
import kotlinx.serialization.Contextual

@kotlinx.serialization.Serializable
data class Progress(
    @Contextual val id: UUID = UUID.randomUUID(),
    @Contextual val user_id: UUID,
    @Contextual val content_id: UUID,
    val current_paragraph: Int = 1,
    val is_completed: Boolean = false,
    val total_time_spent: Int = 0,
    @Contextual val created_at: Instant = Instant.now(),
    @Contextual val updated_at: Instant = Instant.now()
)
