import { useState, useEffect } from 'react';
import { message } from 'antd';
import { FileItem } from '../types';
import contentService from '../services/contentService';
import fileService from '../services/fileService';
import parseService from '../services/parseService';

// 自定义Hooks，用于管理文件相关操作
export const useFiles = (userId: string) => {
  const [files, setFiles] = useState<FileItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadStatus, setUploadStatus] = useState<'idle' | 'parsing' | 'uploading' | 'completed'>('idle');
  const [currentFileName, setCurrentFileName] = useState('');
  
  // 从数据库获取文件列表
  const fetchFiles = async () => {
    try {
      setLoading(true);
      const fetchedFiles = await contentService.getFilesByUserId(userId);
      setFiles(fetchedFiles);
    } catch (error) {
      console.error('获取文件列表失败:', error);
      message.error('获取文件列表失败');
    } finally {
      setLoading(false);
    }
  };
  
  // 初始加载文件列表
  useEffect(() => {
    fetchFiles();
  }, [userId]);
  
  // 处理文件上传
  const handleFileUpload = async (file: File) => {
    try {
      setUploading(true);
      setUploadStatus('idle');
      setUploadProgress(0);
      setCurrentFileName(file.name);
      
      // 验证文件
      if (!fileService.validateFileType(file)) {
        message.error('不支持的文件类型，仅支持 PDF、Word、TXT 格式');
        return;
      }
      
      if (!fileService.validateFileSize(file)) {
        message.error('文件大小超过限制，单个文件不超过10MB');
        return;
      }
      
      // 生成唯一文件名
      const filePath = fileService.generateUniqueFileName(file);
      
      // 1. 解析文件内容
      setUploadStatus('parsing');
      setUploadProgress(30);
      const parseResult = await parseService.parseFile(file);
      
      // 2. 上传文件到Storage
      setUploadStatus('uploading');
      setUploadProgress(60);
      const { url, error: uploadError } = await fileService.uploadFile(file, filePath);
      
      if (uploadError) {
        throw uploadError;
      }
      
      // 3. 构造文件对象
      const fileItem = fileService.constructFileItem(file, filePath, url);
      fileItem.userId = userId;
      
      // 4. 保存文件内容到数据库
      setUploadProgress(80);
      const { error: saveError } = await contentService.saveContent(fileItem, parseResult, userId);
      
      if (saveError) {
        throw saveError;
      }
      
      // 5. 更新本地文件列表
      await fetchFiles();
      
      setUploadProgress(100);
      setUploadStatus('completed');
      message.success(`文件上传成功: ${file.name}`);
      
      // 重置进度状态
      setTimeout(() => {
        setUploading(false);
        setUploadProgress(0);
        setUploadStatus('idle');
        setCurrentFileName('');
      }, 1000);
    } catch (error) {
      console.error('文件上传失败:', error);
      message.error(`文件上传失败: ${error instanceof Error ? error.message : '请重试'}`);
      
      // 重置进度状态
      setTimeout(() => {
        setUploading(false);
        setUploadProgress(0);
        setUploadStatus('idle');
        setCurrentFileName('');
      }, 1000);
    }
  };
  
  // 处理文件删除
  const handleDeleteFile = async (fileId: string) => {
    try {
      // 1. 从数据库删除文件记录
      const { error: dbError } = await contentService.deleteFile(fileId);
      
      if (dbError) {
        throw dbError;
      }
      
      // 2. 从Storage删除文件
      const { error: storageError } = await fileService.deleteFile(fileId);
      
      if (storageError) {
        console.warn('从Storage删除文件失败，但数据库记录已删除:', storageError);
        // 继续执行，不中断流程
      }
      
      // 3. 更新本地文件列表
      await fetchFiles();
      
      message.success('文件删除成功');
    } catch (error) {
      console.error('文件删除失败:', error);
      message.error(`文件删除失败: ${error instanceof Error ? error.message : '请重试'}`);
    }
  };
  
  return {
    files,
    loading,
    uploading,
    uploadProgress,
    uploadStatus,
    currentFileName,
    fetchFiles,
    handleFileUpload,
    handleDeleteFile
  };
};
