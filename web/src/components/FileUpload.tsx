import React, { useState, useRef } from 'react';

interface FileUploadProps {
  onFileUpload: (file: File) => void;
  uploading: boolean;
  uploadProgress: number;
  uploadStatus: 'idle' | 'parsing' | 'uploading' | 'completed';
  currentFileName: string;
}

const FileUpload: React.FC<FileUploadProps> = ({ 
  onFileUpload, 
  uploading, 
  uploadProgress, 
  uploadStatus,
  currentFileName 
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // 处理拖拽事件
  const handleDragEnter = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    const files = Array.from(e.dataTransfer.files);
    files.forEach(file => onFileUpload(file));
  };

  // 处理点击上传
  const handleClickUpload = () => {
    fileInputRef.current?.click();
  };

  // 处理文件选择
  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    files.forEach(file => onFileUpload(file));
    // 重置文件输入，以便可以重复选择同一个文件
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  // 获取状态文本
  const getStatusText = () => {
    switch (uploadStatus) {
      case 'parsing':
        return '解析中...';
      case 'uploading':
        return '上传中...';
      case 'completed':
        return '上传成功!';
      default:
        return '处理中...';
    }
  };

  return (
    <div className="upload-container">
      <input
        ref={fileInputRef}
        type="file"
        multiple
        className="file-input"
        onChange={handleFileSelect}
        style={{ display: 'none' }}
      />
      <div
        className={`ant-upload-drag ${isDragging ? 'ant-upload-drag-hover' : ''}`}
        onDragEnter={handleDragEnter}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
        onClick={handleClickUpload}
        style={{
          border: `2px dashed ${isDragging ? 'var(--md-sys-color-primary)' : 'var(--md-sys-color-outline-variant)'}`,
          borderRadius: '0.75rem',
          padding: '16px',
          textAlign: 'center',
          cursor: 'pointer',
          transition: 'all 0.25s',
          backgroundColor: 'var(--md-sys-color-surface-container-low)',
          width: '100%',
          maxWidth: 'none',
          height: 'auto',
          minHeight: '80px'
        }}
      >
        <div className="ant-upload-drag-container">
          <div className="upload-icon" style={{ fontSize: '32px', marginBottom: '8px', color: 'var(--md-sys-color-primary)' }}>📁</div>
          <p style={{ fontSize: '14px', marginBottom: '4px', color: 'var(--md-sys-color-on-surface)', fontWeight: '500' }}>Click or drag file to this area to upload</p>
          <p className="ant-upload-hint" style={{ fontSize: '12px', color: 'var(--md-sys-color-on-surface-variant)', fontWeight: 'normal', marginBottom: '0' }}>Support for a single or bulk upload.</p>
        </div>
      </div>

      {uploading && (
        <div className="upload-progress" style={{ marginTop: '24px', width: '100%', maxWidth: '500px', marginLeft: 'auto', marginRight: 'auto', padding: '16px', background: 'var(--md-sys-color-surface-container)', borderRadius: '0.5rem', backdropFilter: 'blur(10px)' }}>
          <div className="progress-info" style={{ marginBottom: '12px', fontSize: '14px', color: 'var(--md-sys-color-on-surface)', fontWeight: '500', textAlign: 'center' }}>
            正在处理: {currentFileName}
          </div>
          
          {/* 两步进度条 */}
          <div className="progress-steps" style={{ marginBottom: '16px' }}>
            {/* 步骤指示器 */}
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12px', color: 'var(--md-sys-color-on-surface-variant)' }}>
                <div style={{ width: '20px', height: '20px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: uploadStatus === 'completed' || uploadStatus === 'uploading' || uploadStatus === 'parsing' ? 'var(--md-sys-color-primary)' : 'var(--md-sys-color-surface-variant)', color: 'var(--md-sys-color-on-primary)', fontSize: '10px', fontWeight: 'bold' }}>
                  ✓
                </div>
                <span>解析中</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12px', color: 'var(--md-sys-color-on-surface-variant)' }}>
                <div style={{ width: '20px', height: '20px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: uploadStatus === 'completed' || uploadStatus === 'uploading' ? 'var(--md-sys-color-primary)' : 'var(--md-sys-color-surface-variant)', color: 'var(--md-sys-color-on-primary)', fontSize: '10px', fontWeight: 'bold' }}>
                  {uploadStatus === 'completed' ? '✓' : uploadStatus === 'uploading' ? '2' : '2'}
                </div>
                <span>上传中</span>
              </div>
            </div>
            
            {/* 进度条 */}
            <div className="progress-bar" style={{ height: '6px', background: 'var(--md-sys-color-surface-variant)', borderRadius: '9999px', overflow: 'hidden', marginBottom: '8px', position: 'relative' }}>
              <div 
                className="progress-fill"
                style={{
                  height: '100%',
                  background: 'linear-gradient(90deg, var(--md-sys-color-primary), var(--md-sys-color-secondary))',
                  borderRadius: '9999px',
                  transition: 'width 0.3s ease-in-out',
                  width: `${uploadProgress}%`,
                  boxShadow: '0 0 10px rgba(104, 84, 141, 0.3)',
                  position: 'relative',
                  overflow: 'hidden'
                }}
              >
                <div style={{
                  content: '',
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  right: 0,
                  bottom: 0,
                  background: 'linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent)',
                  animation: 'progress-shine 1.5s infinite'
                }}></div>
              </div>
            </div>
          </div>
          
          <div className="progress-text" style={{ textAlign: 'center', fontSize: '14px', color: 'var(--md-sys-color-on-surface-variant)', fontWeight: '500' }}>
            {getStatusText()}
          </div>
        </div>
      )}
    </div>
  );
};

export default FileUpload;