import React, { useState } from 'react';
import { Upload, Button, message, Progress } from 'antd';
import { InboxOutlined, UploadOutlined } from '@ant-design/icons';
import supabase from '../utils/supabase';

const { Dragger } = Upload;

interface FileUploadProps {
  onFileUploaded: (fileUrl: string, fileName: string, fileType: string, fileSize: number) => void;
}

const FileUpload: React.FC<FileUploadProps> = ({ onFileUploaded }) => {
  const [loading, setLoading] = useState(false);
  const [progress, setProgress] = useState(0);

  const props = {
    name: 'file',
    multiple: false,
    beforeUpload: (file: any) => {
      const isPDF = file.type === 'application/pdf';
      const isWord = file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
      const isText = file.type === 'text/plain';
      const isLt10M = file.size / 1024 / 1024 < 10;

      if (!isPDF && !isWord && !isText) {
        message.error('只能上传PDF、Word或TXT文件!');
        return false;
      }
      if (!isLt10M) {
        message.error('文件大小不能超过10MB!');
        return false;
      }
      return true;
    },
    customRequest: async (options: any) => {
      const { file, onSuccess, onError } = options;
      setLoading(true);
      setProgress(0);

      try {
        // Generate a unique filename
        const fileName = `${Date.now()}_${file.name}`;
        
        // Upload file to Supabase Storage
        const { data, error } = await supabase.storage
          .from('files')
          .upload(fileName, file, {
            cacheControl: '3600',
            upsert: false,
            onProgress: (event) => {
              const percent = Math.round((event.loaded / event.total) * 100);
              setProgress(percent);
            }
          });

        if (error) {
          throw error;
        }

        // Get public URL
        const { data: urlData } = supabase.storage
          .from('files')
          .getPublicUrl(fileName);

        if (urlData.publicUrl) {
          onFileUploaded(urlData.publicUrl, file.name, file.type, file.size);
          onSuccess(null, file);
          message.success('文件上传成功!');
        } else {
          throw new Error('无法获取文件URL');
        }
      } catch (error) {
        console.error('上传失败:', error);
        onError(error);
        message.error('文件上传失败，请重试!');
      } finally {
        setLoading(false);
        setProgress(0);
      }
    },
    onChange(info: any) {
      // This function is called when the upload state changes
      // We're handling the upload manually in customRequest, so this is just for reference
    },
  };

  return (
    <div>
      <Dragger {...props}>
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
        <p className="ant-upload-hint">
          支持上传 PDF、Word (.docx) 和 TXT 文件，单个文件大小不超过 10MB
        </p>
      </Dragger>
      {loading && (
        <div style={{ marginTop: 16 }}>
          <Progress percent={progress} status="active" />
        </div>
      )}
    </div>
  );
};

export default FileUpload;
