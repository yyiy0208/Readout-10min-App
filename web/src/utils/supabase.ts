
import { createClient } from '@supabase/supabase-js'

// Supabase配置
const supabaseUrl = (import.meta as any).env?.VITE_SUPABASE_URL || ''
const supabasePublishableKey = (import.meta as any).env?.VITE_SUPABASE_PUBLISHABLE_KEY || ''

// 创建Supabase客户端
const supabase = createClient(supabaseUrl, supabasePublishableKey)

export default supabase
