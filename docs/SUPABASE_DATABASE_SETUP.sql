-- Readout-10min-App 数据库初始化脚本

-- 创建 content 表：存储文章基本信息
CREATE TABLE IF NOT EXISTS content (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    author TEXT,
    source TEXT,
    file_url TEXT NOT NULL,
    file_name TEXT NOT NULL,
    file_type TEXT NOT NULL,
    file_size INT NOT NULL,
    total_paragraphs INT DEFAULT 0,
    total_words INT DEFAULT 0,
    estimated_duration INT DEFAULT 0, -- 预计朗读时间（秒）
    difficulty TEXT DEFAULT 'medium', -- easy, medium, hard
    language TEXT DEFAULT 'en',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 创建 paragraphs 表：存储拆分后的段落
CREATE TABLE IF NOT EXISTS paragraphs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id UUID REFERENCES content(id) ON DELETE CASCADE,
    paragraph_number INT NOT NULL,
    text TEXT NOT NULL,
    word_count INT NOT NULL,
    estimated_duration INT NOT NULL, -- 预计朗读时间（秒）
    audio_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 创建 progress 表：存储用户阅读进度
CREATE TABLE IF NOT EXISTS progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL, -- 实际项目中应与 auth.users 表关联
    content_id UUID REFERENCES content(id) ON DELETE CASCADE,
    current_paragraph INT DEFAULT 1,
    is_completed BOOLEAN DEFAULT FALSE,
    total_time_spent INT DEFAULT 0, -- 总阅读时间（秒）
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 创建 practice_records 表：存储练习记录
CREATE TABLE IF NOT EXISTS practice_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL, -- 实际项目中应与 auth.users 表关联
    paragraph_id UUID REFERENCES paragraphs(id) ON DELETE CASCADE,
    content_id UUID REFERENCES content(id) ON DELETE CASCADE,
    practice_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    duration INT NOT NULL, -- 实际朗读时间（秒）
    accuracy FLOAT DEFAULT 0.0, -- 准确率
    fluency FLOAT DEFAULT 0.0, -- 流利度
    pronunciation_score FLOAT DEFAULT 0.0, -- 发音分数
    recording_url TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_paragraphs_content_id ON paragraphs(content_id);
CREATE INDEX IF NOT EXISTS idx_paragraphs_number ON paragraphs(content_id, paragraph_number);
CREATE INDEX IF NOT EXISTS idx_progress_user_content ON progress(user_id, content_id);
CREATE INDEX IF NOT EXISTS idx_practice_records_user ON practice_records(user_id);
CREATE INDEX IF NOT EXISTS idx_practice_records_date ON practice_records(practice_date);

-- 启用 RLS（Row Level Security）
ALTER TABLE content ENABLE ROW LEVEL SECURITY;
ALTER TABLE paragraphs ENABLE ROW LEVEL SECURITY;
ALTER TABLE progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE practice_records ENABLE ROW LEVEL SECURITY;

-- 创建 RLS 策略
-- content 表策略：允许所有人读取，仅管理员修改
CREATE POLICY "Allow all to read content" ON content
    FOR SELECT
    USING (true);

CREATE POLICY "Allow admin to modify content" ON content
    FOR ALL
    USING (auth.role() = 'authenticated') -- 实际项目中应检查管理员角色
    WITH CHECK (auth.role() = 'authenticated');

-- paragraphs 表策略：允许所有人读取，仅管理员修改
CREATE POLICY "Allow all to read paragraphs" ON paragraphs
    FOR SELECT
    USING (true);

CREATE POLICY "Allow admin to modify paragraphs" ON paragraphs
    FOR ALL
    USING (auth.role() = 'authenticated') -- 实际项目中应检查管理员角色
    WITH CHECK (auth.role() = 'authenticated');

-- progress 表策略：用户只能访问自己的进度
CREATE POLICY "Allow users to access their own progress" ON progress
    FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- practice_records 表策略：用户只能访问自己的练习记录
CREATE POLICY "Allow users to access their own practice records" ON practice_records
    FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- 创建触发器：更新 updated_at 字段
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为所有表添加 updated_at 触发器
CREATE TRIGGER update_content_updated_at
    BEFORE UPDATE ON content
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_paragraphs_updated_at
    BEFORE UPDATE ON paragraphs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_progress_updated_at
    BEFORE UPDATE ON progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_practice_records_updated_at
    BEFORE UPDATE ON practice_records
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 插入示例数据（可选）
-- INSERT INTO content (
--     title, author, source, file_url, file_name, file_type, file_size
-- ) VALUES (
--     'Sample Article', 'Unknown', 'Sample Source', 
--     'https://example.com/sample.pdf', 'sample.pdf', 'application/pdf', 1024000
-- );

-- 创建存储函数：更新文章总段落数
CREATE OR REPLACE FUNCTION update_content_paragraph_count()
RETURNS TRIGGER AS $$
BEGIN
    -- 插入或删除段落后更新文章的总段落数
    UPDATE content
    SET total_paragraphs = (SELECT COUNT(*) FROM paragraphs WHERE content_id = NEW.content_id)
    WHERE id = NEW.content_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器：更新文章总段落数
CREATE TRIGGER update_content_paragraph_count_after_insert
    AFTER INSERT ON paragraphs
    FOR EACH ROW
    EXECUTE FUNCTION update_content_paragraph_count();

CREATE TRIGGER update_content_paragraph_count_after_delete
    AFTER DELETE ON paragraphs
    FOR EACH ROW
    EXECUTE FUNCTION update_content_paragraph_count();

-- 存储函数：计算文章总字数
CREATE OR REPLACE FUNCTION calculate_total_words()
RETURNS TRIGGER AS $$
BEGIN
    -- 计算并更新文章的总字数
    UPDATE content
    SET total_words = (SELECT SUM(word_count) FROM paragraphs WHERE content_id = NEW.content_id)
    WHERE id = NEW.content_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器：计算文章总字数
CREATE TRIGGER calculate_total_words_after_insert
    AFTER INSERT ON paragraphs
    FOR EACH ROW
    EXECUTE FUNCTION calculate_total_words();

CREATE TRIGGER calculate_total_words_after_update
    AFTER UPDATE ON paragraphs
    FOR EACH ROW
    EXECUTE FUNCTION calculate_total_words();

CREATE TRIGGER calculate_total_words_after_delete
    AFTER DELETE ON paragraphs
    FOR EACH ROW
    EXECUTE FUNCTION calculate_total_words();

-- 存储函数：计算文章预计朗读时间
CREATE OR REPLACE FUNCTION calculate_estimated_duration()
RETURNS TRIGGER AS $$
BEGIN
    -- 计算并更新文章的预计朗读时间（基于所有段落的预计时间总和）
    UPDATE content
    SET estimated_duration = (SELECT SUM(estimated_duration) FROM paragraphs WHERE content_id = NEW.content_id)
    WHERE id = NEW.content_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器：计算文章预计朗读时间
CREATE TRIGGER calculate_estimated_duration_after_insert
    AFTER INSERT ON paragraphs
    FOR EACH ROW
    EXECUTE FUNCTION calculate_estimated_duration();

CREATE TRIGGER calculate_estimated_duration_after_update
    AFTER UPDATE ON paragraphs
    FOR EACH ROW
    EXECUTE FUNCTION calculate_estimated_duration();

CREATE TRIGGER calculate_estimated_duration_after_delete
    AFTER DELETE ON paragraphs
    FOR EACH ROW
    EXECUTE FUNCTION calculate_estimated_duration();
