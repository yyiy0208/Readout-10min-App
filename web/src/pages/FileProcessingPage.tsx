import React, { useState } from 'react';
import { Card, Button, Space, message, Progress } from 'antd';
import { FileTextOutlined, LoadingOutlined } from '@ant-design/icons';

const FileProcessingPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [isProcessing, setIsProcessing] = useState(false);

  const handleProcessFile = () => {
    setIsProcessing(true);
    setLoading(true);
    setProgress(0);

    // Simulate file processing
    const interval = setInterval(() => {
      setProgress(prev => {
        const next = prev + 10;
        if (next >= 100) {
          clearInterval(interval);
          setLoading(false);
          setIsProcessing(false);
          message.success('文件处理成功!');
          return 100;
        }
        return next;
      });
    }, 300);
  };

  return (
    <div style={{ padding: '24px 0' }}>
      <Card title="文件处理" style={{ marginBottom: 24 }}>
        <div style={{ marginBottom: 24 }}>
          <h3>处理选项</h3>
          <p>选择文件处理的相关选项，然后点击"开始处理"按钮。</p>
        </div>

        <Space style={{ marginBottom: 24 }} direction="vertical" size="large">
          <div>
            <span>智能段落拆分: </span>
            <span>启用</span>
          </div>
          <div>
            <span>语言检测: </span>
            <span>自动</span>
          </div>
          <div>
            <span>难度评估: </span>
            <span>启用</span>
          </div>
        </Space>

        <Button 
          type="primary" 
          icon={<FileTextOutlined />} 
          onClick={handleProcessFile}
          loading={loading}
          disabled={isProcessing}
        >
          开始处理
        </Button>

        {isProcessing && (
          <div style={{ marginTop: 16 }}>
            <Progress percent={progress} status="active" />
          </div>
        )}
      </Card>

      <Card title="处理结果">
        <div style={{ textAlign: 'center', padding: '48px 0' }}>
          <p>请先上传文件并点击"开始处理"按钮</p>
        </div>
      </Card>
    </div>
  );
};

export default FileProcessingPage;
