package com.readout10min.data
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    private const val SUPABASE_URL = "https://rpdosngigisripruwhps.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJwZG9zbmdpZ2lzcmlwcnV3aHBzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njg5MTI3NDMsImV4cCI6MjA4NDQ4ODc0M30.Fdpq3j0x4FKGJgtoeEMoVMXZWi3xjJf-6C3gh8tFmtA"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
