import React, { useState } from 'react';
import { Spin } from 'antd';
import { useNavigate } from 'react-router-dom';
import { FileItem } from '../types';

interface FileListProps {
  files: FileItem[];
  onDelete: (fileId: string) => void;
  loading: boolean;
}

const FileList: React.FC<FileListProps> = ({ files, onDelete, loading }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  // 过滤文件
  const filteredFiles = files.filter(file => 
    file.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // 处理搜索
  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
  };

  // 格式化文件大小
  const formatFileSize = (size: number): string => {
    if (size < 1024) return `${size} B`;
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`;
    return `${(size / (1024 * 1024)).toFixed(2)} MB`;
  };

  // 格式化上传时间
  const formatUploadTime = (time: string): string => {
    const date = new Date(time);
    return date.toLocaleString('zh-CN');
  };

  return (
    <>
      <div className="section-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <h2>已上传文件</h2>
        <div className="search-box" style={{ display: 'flex', gap: '8px', background: 'rgba(255, 255, 255, 0.7)', backdropFilter: 'blur(20px)', padding: '8px', borderRadius: '9999px', border: '1px solid var(--border-color)' }}>
          <input 
            type="text" 
            // 去除placeholder
            value={searchTerm}
            onChange={handleSearch}
            style={{
              padding: '8px 16px',
              border: 'none',
              background: 'transparent',
              outline: 'none',
              color: 'var(--text-primary)',
              fontSize: '14px',
              width: '240px',
              borderRadius: '9999px'
            }}
          />
          <button 
            className="search-btn"
            style={{
              padding: '8px 16px',
              borderRadius: '9999px',
              border: 'none', // 去除外边框
              background: 'linear-gradient(135deg, var(--primary-color), var(--primary-light))',
              color: 'white',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: '500'
            }}
          >
            🔍
          </button>
        </div>
      </div>
      
      {loading ? (
        <div style={{ textAlign: 'center', padding: '48px' }}>
          <Spin size="large" />
          <p style={{ marginTop: '16px' }}>加载中...</p>
        </div>
      ) : files.length === 0 ? (
        <div className="empty-file-list" style={{ textAlign: 'center', padding: '48px', background: 'var(--background-alt)', borderRadius: '0.75rem', border: '1px solid var(--border-color)' }}>
          <p style={{ color: 'var(--text-secondary)' }}>暂无上传文件</p>
        </div>
      ) : (
        <>
          <div className="table-container" style={{ overflowX: 'auto', borderRadius: '0.5rem', border: '1px solid var(--border-color)', marginBottom: '16px' }}>
            <table className="file-table" style={{ width: '100%', borderCollapse: 'collapse', background: 'var(--background-alt)' }}>
              <thead>
                <tr style={{ background: 'var(--primary-color)', color: 'white', position: 'sticky', top: 0, zIndex: 10 }}>
                  <th style={{ padding: '16px 24px', textAlign: 'left', fontWeight: '600', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>文件名</th>
                  <th style={{ padding: '16px 24px', textAlign: 'left', fontWeight: '600', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>类型</th>
                  <th style={{ padding: '16px 24px', textAlign: 'left', fontWeight: '600', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>大小</th>
                  <th style={{ padding: '16px 24px', textAlign: 'left', fontWeight: '600', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>上传时间</th>
                  <th style={{ padding: '16px 24px', textAlign: 'left', fontWeight: '600', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>操作</th>
                </tr>
              </thead>
              <tbody>
                {filteredFiles.map((file) => (
                  <tr key={file.id} style={{ transition: 'all 0.25s', cursor: 'pointer', borderBottom: 'none' }}>
                    <td style={{ padding: '20px 24px', display: 'flex', alignItems: 'center', gap: '12px', fontWeight: '500', color: 'var(--text-primary)', fontSize: '14px' }}>
                      <span className="file-icon" style={{ fontSize: '20px' }}>📄</span>
                      {/* 限制文件名长度，保证按钮可见 */}
                      <div style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {file.name}
                      </div>
                    </td>
                    <td style={{ padding: '20px 24px', color: 'var(--text-secondary)', fontSize: '14px' }}>{file.name.split('.').pop()?.toUpperCase()}</td>
                    <td style={{ padding: '20px 24px', color: 'var(--text-secondary)', fontSize: '14px' }}>{formatFileSize(file.size)}</td>
                    <td style={{ padding: '20px 24px', color: 'var(--text-secondary)', fontSize: '14px' }}>{formatUploadTime(file.uploadTime)}</td>
                    <td style={{ padding: '20px 24px' }}>
                      <div className="action-buttons" style={{ display: 'flex', gap: '16px' }}>
                        <button 
                          className="btn btn-primary btn-sm"
                          style={{
                            padding: '12px 16px',
                            borderRadius: '0.5rem',
                            border: 'none',
                            background: 'var(--primary-color)',
                            color: 'white',
                            cursor: 'pointer',
                            fontSize: '12px',
                            fontWeight: '600',
                            transition: 'all 0.25s'
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'translateY(-2px)';
                            e.currentTarget.style.boxShadow = '0 4px 6px -1px rgba(0, 0, 0, 0.1)';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.boxShadow = 'none';
                          }}
                          onClick={() => navigate('/file-processing', { state: { file } })}
                        >
                          分段
                        </button>
                        <button 
                          className="btn btn-danger btn-sm"
                          onClick={() => onDelete(file.id)}
                          style={{
                            padding: '12px 16px',
                            borderRadius: '0.5rem',
                            border: 'none',
                            background: 'var(--error-color)',
                            color: 'white',
                            cursor: 'pointer',
                            fontSize: '12px',
                            fontWeight: '600',
                            transition: 'all 0.25s'
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'translateY(-2px)';
                            e.currentTarget.style.boxShadow = '0 4px 6px -1px rgba(0, 0, 0, 0.1)';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.boxShadow = 'none';
                          }}
                        >
                          删除
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="pagination" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '12px', padding: '16px', background: 'rgba(255, 255, 255, 0.7)', backdropFilter: 'blur(20px)', borderRadius: '9999px', border: '1px solid var(--border-color)' }}>
            <button 
              className="btn btn-secondary btn-sm"
              style={{
                padding: '12px 16px',
                borderRadius: '0.5rem',
                border: '1px solid var(--border-color)',
                background: 'var(--background-light)',
                color: 'var(--text-primary)',
                cursor: 'pointer',
                fontSize: '12px',
                fontWeight: '600',
                transition: 'all 0.25s'
              }}
            >
              上一页
            </button>
            <span className="page-info" style={{ color: 'var(--text-secondary)', fontSize: '12px', fontWeight: '500' }}>第 1 页 / 共 1 页</span>
            <button 
              className="btn btn-secondary btn-sm"
              style={{
                padding: '12px 16px',
                borderRadius: '0.5rem',
                border: '1px solid var(--border-color)',
                background: 'var(--background-light)',
                color: 'var(--text-primary)',
                cursor: 'pointer',
                fontSize: '12px',
                fontWeight: '600',
                transition: 'all 0.25s'
              }}
            >
              下一页
            </button>
          </div>
        </>
      )}
    </>
  );
};

export default FileList;