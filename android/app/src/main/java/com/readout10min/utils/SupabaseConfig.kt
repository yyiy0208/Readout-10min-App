
package com.readout10min.utils

import io.github.jan-tennert.supabase.SupabaseClient
import io.github.jan-tennert.supabase.createSupabaseClient
import io.github.jan-tennert.supabase.gotrue.GoTrue
import io.github.jan-tennert.supabase.postgrest.Postgrest
import io.github.jan-tennert.supabase.storage.Storage

object SupabaseConfig {
    private const val SUPABASE_URL = "YOUR_SUPABASE_URL"
    private const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"

    val supabaseClient: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
        install(Storage)
    }
}
