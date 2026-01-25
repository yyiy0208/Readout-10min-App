import supabase from '../utils/supabase';
import { FileItem, ParseResult } from '../types';

// 内容服务类
class ContentService {
  // 从数据库获取用户的文件列表
  async getFilesByUserId(userId: string): Promise<FileItem[]> {
    try {
      const { data, error } = await supabase
        .from('content')
        .select('*')
        .eq('created_by', userId)
        .order('created_at', { ascending: false });

      if (error) {
        console.error('从数据库获取文件列表失败:', error);
        return [];
      }

      // 转换数据格式，适配FileItem接口
      return (data || []).map((content: any) => ({
        id: content.id,
        name: content.file_name,
        size: content.file_size,
        url: content.file_url,
        uploadTime: content.created_at,
        status: 'completed',
        userId: content.created_by
      }));
    } catch (error) {
      console.error('获取文件列表失败:', error);
      return [];
    }
  }
  
  // 保存文件内容到数据库
  async saveContent(
    file: FileItem,
    parseResult: ParseResult,
    userId: string
  ): Promise<{ success: boolean; error: Error | null }> {
    try {
      // 获取文件扩展名，用于file_type
      const fileExtension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'));
      const fileType = `application/${fileExtension.substring(1)}`; // 移除点号

      // 计算段落数和总字数
      const paragraphs = parseResult.paragraphs;
      const totalParagraphs = paragraphs.length;
      const totalWords = paragraphs.reduce((sum: number, para: string) => 
        sum + para.split(/\s+/).filter(word => word.length > 0).length, 0);
      
      // 简单估算朗读时间（假设平均语速为每分钟150词）
      const estimatedDuration = Math.round((totalWords / 150) * 60);

      // 保存文件信息到content表
      const { data: savedContent, error: saveError } = await supabase
        .from('content')
        .insert({
          title: file.name, // 使用文件名作为标题
          file_url: file.url,
          file_name: file.name,
          file_type: fileType,
          file_size: file.size,
          created_by: userId,
          raw_text: parseResult.content,
          total_paragraphs: totalParagraphs,
          total_words: totalWords,
          estimated_duration: estimatedDuration
        })
        .select('*')
        .single();

      if (saveError) {
        throw saveError;
      }

      // 如果有段落，保存到paragraphs表
      if (paragraphs.length > 0 && savedContent) {
        const paragraphData = paragraphs.map((text: string, index: number) => ({
          content_id: savedContent.id,
          paragraph_number: index + 1,
          text: text,
          word_count: text.split(/\s+/).filter(word => word.length > 0).length,
          estimated_duration: Math.round((text.split(/\s+/).filter(word => word.length > 0).length / 150) * 60)
        }));

        const { error: insertError } = await supabase
          .from('paragraphs')
          .insert(paragraphData);

        if (insertError) {
          console.error('保存段落失败:', insertError);
          // 这里不抛出错误，因为文件已经保存成功，只是段落保存失败
        }
      }

      return {
        success: true,
        error: null
      };
    } catch (error) {
      console.error('保存文件内容失败:', error);
      return {
        success: false,
        error: error instanceof Error ? error : new Error('保存文件内容失败')
      };
    }
  }
  
  // 从数据库删除文件
  async deleteFile(fileId: string): Promise<{ success: boolean; error: Error | null }> {
    try {
      // 从content表删除文件记录
      const { error: contentError } = await supabase
        .from('content')
        .delete()
        .eq('id', fileId);

      if (contentError) {
        throw contentError;
      }

      return {
        success: true,
        error: null
      };
    } catch (error) {
      console.error('删除文件失败:', error);
      return {
        success: false,
        error: error instanceof Error ? error : new Error('删除文件失败')
      };
    }
  }
  
  // 更新段落拆分结果
  async updateParagraphs(
    contentId: string,
    paragraphs: Array<{text: string, wordCount: number, estimatedDuration: number}>
  ): Promise<{ success: boolean; error: Error | null }> {
    try {
      // 1. 删除现有段落
      const { error: deleteError } = await supabase
        .from('paragraphs')
        .delete()
        .eq('content_id', contentId);

      if (deleteError) {
        throw deleteError;
      }

      // 2. 插入新段落
      if (paragraphs.length > 0) {
        const paragraphData = paragraphs.map((para, index) => ({
          content_id: contentId,
          paragraph_number: index + 1,
          text: para.text,
          word_count: para.wordCount,
          estimated_duration: Math.round(para.estimatedDuration * 60) // 转换为秒
        }));

        const { error: insertError } = await supabase
          .from('paragraphs')
          .insert(paragraphData);

        if (insertError) {
          throw insertError;
        }
      }

      // 3. 更新content表中的统计信息
      const totalParagraphs = paragraphs.length;
      const totalWords = paragraphs.reduce((sum, para) => sum + para.wordCount, 0);
      const estimatedDuration = Math.round(paragraphs.reduce((sum, para) => sum + para.estimatedDuration, 0) * 60);

      const { error: updateError } = await supabase
        .from('content')
        .update({
          total_paragraphs: totalParagraphs,
          total_words: totalWords,
          estimated_duration: estimatedDuration
        })
        .eq('id', contentId);

      if (updateError) {
        throw updateError;
      }

      return {
        success: true,
        error: null
      };
    } catch (error) {
      console.error('更新段落失败:', error);
      return {
        success: false,
        error: error instanceof Error ? error : new Error('更新段落失败')
      };
    }
  }
  
  // 获取历史分段结果
  async getParagraphsByContentId(contentId: string): Promise<Array<{
    text: string;
    wordCount: number;
    estimatedDuration: number;
  }>> {
    try {
      const { data, error } = await supabase
        .from('paragraphs')
        .select('text, word_count, estimated_duration')
        .eq('content_id', contentId)
        .order('paragraph_number', { ascending: true });
      
      if (error) {
        throw error;
      }
      
      return (data || []).map(paragraph => ({
        text: paragraph.text,
        wordCount: paragraph.word_count,
        estimatedDuration: paragraph.estimated_duration / 60 // 转换为分钟
      }));
    } catch (error) {
      console.error('获取历史分段结果失败:', error);
      return [];
    }
  }
  
  // 计算段落统计信息
  calculateParagraphStats(paragraphs: string[]): {
    totalParagraphs: number;
    totalWords: number;
    estimatedDuration: number;
  } {
    const totalParagraphs = paragraphs.length;
    const totalWords = paragraphs.reduce((sum: number, para: string) => 
      sum + para.split(/\s+/).filter(word => word.length > 0).length, 0);
    const estimatedDuration = Math.round((totalWords / 150) * 60);
    
    return {
      totalParagraphs,
      totalWords,
      estimatedDuration
    };
  }
}

// 导出单例实例
export default new ContentService();
