import React from 'react';
import FileUpload from '../components/FileUpload';
import FileList from '../components/FileList';
import { useFiles } from '../hooks/useFiles';

const FileManagementPage: React.FC = () => {
  // 当前用户名（暂时硬编码）
  const currentUser = 'yyiy';
  
  // 使用自定义Hooks管理文件
  const { 
    files, 
    loading, 
    uploading, 
    uploadProgress, 
    uploadStatus,
    currentFileName,
    handleFileUpload, 
    handleDeleteFile 
  } = useFiles(currentUser)

  return (
    <div className="app-container">
      {/* 主内容区域 */}
      <main className="main-content">
        {/* 文件上传区域 */}
        <section className="upload-section">
          <div className="card-container">
            <FileUpload 
              onFileUpload={handleFileUpload} 
              uploading={uploading} 
              uploadProgress={uploadProgress}
              uploadStatus={uploadStatus}
              currentFileName={currentFileName}
            />
          </div>
        </section>

        {/* 文件列表区域 */}
        <section className="file-list-section">
          <div className="card-container">
            <FileList 
              files={files} 
              onDelete={handleDeleteFile} 
              loading={loading} 
            />
          </div>
        </section>
      </main>
    </div>
  );
};

export default FileManagementPage;
