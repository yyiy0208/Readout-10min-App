import supabase from '../utils/supabase';
import { FileItem } from '../types';

// 文件服务类
class FileService {
  private readonly bucketName = 'readout-10min-files';
  
  // 支持的文件类型
  private readonly supportedTypes = ['.pdf', '.docx', '.txt'];
  
  // 文件大小限制（10MB）
  private readonly maxFileSize = 10 * 1024 * 1024;
  
  // 验证文件类型
  validateFileType(file: File): boolean {
    const fileExtension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'));
    return this.supportedTypes.includes(fileExtension);
  }
  
  // 验证文件大小
  validateFileSize(file: File): boolean {
    return file.size <= this.maxFileSize;
  }
  
  // 生成唯一文件名
  generateUniqueFileName(file: File): string {
    const fileExtension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'));
    const dateNow = Date.now();
    return `${dateNow}${fileExtension}`;
  }
  
  // 上传文件到Supabase Storage
  async uploadFile(file: File, filePath: string): Promise<{ url: string; error: Error | null }> {
    try {
      // 上传文件
      const { error: uploadError } = await supabase.storage
        .from(this.bucketName)
        .upload(filePath, file, {
          cacheControl: '3600',
          upsert: false
        });
      
      if (uploadError) {
        throw uploadError;
      }
      
      // 获取文件URL
      const { data: urlData } = supabase.storage
        .from(this.bucketName)
        .getPublicUrl(filePath);
      
      return {
        url: urlData.publicUrl,
        error: null
      };
    } catch (error) {
      console.error('文件上传失败:', error);
      return {
        url: '',
        error: error instanceof Error ? error : new Error('文件上传失败')
      };
    }
  }
  
  // 从Supabase Storage下载文件
  async downloadFile(filePath: string): Promise<{ data: ArrayBuffer | null; error: Error | null }> {
    try {
      const { data, error } = await supabase.storage
        .from(this.bucketName)
        .download(filePath);
      
      if (error) {
        throw error;
      }
      
      // 转换为ArrayBuffer
      const arrayBuffer = await data.arrayBuffer();
      
      return {
        data: arrayBuffer,
        error: null
      };
    } catch (error) {
      console.error('文件下载失败:', error);
      return {
        data: null,
        error: error instanceof Error ? error : new Error('文件下载失败')
      };
    }
  }
  
  // 从Supabase Storage删除文件
  async deleteFile(filePath: string): Promise<{ success: boolean; error: Error | null }> {
    try {
      const { error } = await supabase.storage
        .from(this.bucketName)
        .remove([filePath]);
      
      if (error) {
        throw error;
      }
      
      return {
        success: true,
        error: null
      };
    } catch (error) {
      console.error('文件删除失败:', error);
      return {
        success: false,
        error: error instanceof Error ? error : new Error('文件删除失败')
      };
    }
  }
  
  // 构造文件对象
  constructFileItem(file: File, fileName: string, url: string): FileItem {
    return {
      id: fileName,
      name: file.name,
      size: file.size,
      url: url,
      uploadTime: new Date().toISOString(),
      status: 'completed',
      userId: '' // 将在后续处理中设置
    };
  }
}

// 导出单例实例
export default new FileService();
