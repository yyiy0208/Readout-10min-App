import React from 'react';
import { Button, Popconfirm, message } from 'antd';
import { DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import supabase from '../utils/supabase';

interface File {
  id: string;
  name: string;
  size: number;
  url: string;
  uploadTime: string;
  status: string;
}

interface FileItemProps {
  file: File;
  onDelete: (fileId: string) => void;
}

const FileItem: React.FC<FileItemProps> = ({ file, onDelete }) => {
  // 格式化文件大小
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  // 格式化上传时间
  const formatUploadTime = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleString();
  };

  // 处理文件预览
  const handlePreview = () => {
    window.open(file.url, '_blank');
  };

  // 处理文件删除
  const handleDelete = async () => {
    try {
      // 从Supabase Storage删除文件
      const filePath = `public/${file.id}`;
      const { error } = await supabase.storage
        .from('readout-10min-files')
        .remove([filePath]);

      if (error) {
        throw error;
      }

      message.success('文件删除成功');
      onDelete(file.id);
    } catch (error) {
      console.error('文件删除失败:', error);
      message.error('文件删除失败，请重试');
    }
  };

  return (
    <div className="file-item">
      <div className="file-info">
        <div className="file-name">{file.name}</div>
        <div className="file-meta">
          <span className="file-size">{formatFileSize(file.size)}</span>
          <span className="file-time">{formatUploadTime(file.uploadTime)}</span>
          <span className={`file-status ${file.status}`}>
            {file.status === 'completed' ? '已完成' : file.status}
          </span>
        </div>
      </div>
      <div className="file-actions">
        <Button
          type="text"
          icon={<EyeOutlined />}
          onClick={handlePreview}
          className="file-action-btn"
        >
          预览
        </Button>
        <Popconfirm
          title="确定要删除这个文件吗？"
          onConfirm={handleDelete}
          okText="确定"
          cancelText="取消"
        >
          <Button
            type="text"
            danger
            icon={<DeleteOutlined />}
            className="file-action-btn"
          >
            删除
          </Button>
        </Popconfirm>
      </div>
    </div>
  );
};

export default FileItem;