// 文件类型定义
export interface FileItem {
  id: string;
  name: string;
  size: number;
  url: string;
  uploadTime: string;
  status: string;
  userId: string;
  parsedContent?: string;
  paragraphs?: string[];
}

// 解析结果类型
export interface ParseResult {
  content: string;
  paragraphs: string[];
}

// 段落类型
export interface Paragraph {
  id?: string;
  contentId: string;
  paragraphNumber: number;
  text: string;
  wordCount: number;
  estimatedDuration: number;
}

// 内容类型
export interface Content {
  id: string;
  title: string;
  fileUrl: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  createdAt: string;
  createdBy: string;
  rawText: string;
  totalParagraphs: number;
  totalWords: number;
  estimatedDuration: number;
  paragraphs?: Paragraph[];
}

// 文件上传状态
export type UploadStatus = 'idle' | 'uploading' | 'success' | 'error';

// 文件上传结果
export interface UploadResult {
  file: FileItem;
  originalFile: File;
}

// API响应类型
export interface ApiResponse<T> {
  data: T | null;
  error: Error | null;
}

// 解析选项
export interface ParseOptions {
  fileType: string;
  arrayBuffer: ArrayBuffer;
}
