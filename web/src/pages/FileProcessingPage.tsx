import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import supabase from '../utils/supabase';
import parseService from '../services/parseService';
import contentService from '../services/contentService';
import { Paragraph, FileItem } from '../types';

// 拆分配置接口
export interface SplitConfig {
  targetDuration: number; // 目标时长（分钟）
  readingSpeed: number; // 阅读速度（词/分钟）
}

// 拆分结果接口
export interface SplitResult {
  paragraphs: Paragraph[];
  totalParagraphs: number;
  totalWords: number;
  averageWordsPerParagraph: number;
  estimatedTotalDuration: number;
}

const FileProcessingPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  // 获取从文件管理页面传递的文件信息
  const file = location.state?.file as FileItem | undefined;
  
  // 状态管理
  const [content, setContent] = useState<string>('');
  const [targetDuration, setTargetDuration] = useState<number>(10);
  const [splitResult, setSplitResult] = useState<SplitResult | null>(null);
  const [isSplitting, setIsSplitting] = useState<boolean>(false);
  const [showResult, setShowResult] = useState<boolean>(false);
  const [isSaving, setIsSaving] = useState<boolean>(false);
  const [showFullContent, setShowFullContent] = useState<boolean>(false);
  const [selectedParagraph, setSelectedParagraph] = useState<Paragraph | null>(null);
  const [showParagraphModal, setShowParagraphModal] = useState<boolean>(false);
  
  // 示例内容，实际应用中应从文件服务获取
  const sampleContent = `Sample Article Title

This is the first paragraph of the sample article. It contains some text that will be split into readable paragraphs for the user to practice reading.

This is the second paragraph. The intelligent splitting algorithm will analyze the text and split it into appropriate segments based on the target duration setting.

This is the third paragraph. The user can adjust the target duration and other settings to customize their reading experience.

This is the fourth paragraph. Each paragraph will be displayed separately in the mobile app, allowing the user to focus on one segment at a time.

This is the fifth paragraph. The goal is to create paragraphs that are approximately 10 minutes in length, based on average reading speed.

This is the sixth paragraph. It provides additional content to demonstrate the splitting functionality. The algorithm should be able to handle longer documents and create appropriate segments.

This is the seventh paragraph. It continues the sample text, providing more content for testing the splitting algorithm.

This is the eighth paragraph. The more content we have, the better we can test the algorithm's ability to create balanced segments.

This is the ninth paragraph. Each paragraph should be roughly the same length in terms of reading time, based on the user's target duration.

This is the tenth paragraph. The algorithm should consider sentence boundaries and paragraph structure to create natural-sounding segments.`;
  
  // 初始化内容和历史分段结果
  useEffect(() => {
    const fetchData = async () => {
      if (!file) {
        // 如果没有传递文件，使用示例内容
        setContent(sampleContent);
      } else {
        try {
          // 从数据库获取文件内容
          const { data: contentData, error: contentError } = await supabase
            .from('content')
            .select('raw_text')
            .eq('id', file.id)
            .single();
          
          if (contentError) {
            console.error('获取文件内容失败:', contentError);
            setContent(sampleContent);
          } else {
            setContent(contentData.raw_text || sampleContent);
          }
          
          // 从数据库获取历史分段结果
          const historicalParagraphs = await contentService.getParagraphsByContentId(file.id);
          
          if (historicalParagraphs.length > 0) {
            // 转换为Paragraph类型
            const paragraphs: Paragraph[] = historicalParagraphs.map((para, index) => ({
              contentId: file.id,
              paragraphNumber: index + 1,
              text: para.text,
              wordCount: para.wordCount,
              estimatedDuration: para.estimatedDuration
            }));
            
            // 计算统计信息
            const totalWords = paragraphs.reduce((sum, para) => sum + para.wordCount, 0);
            const averageWordsPerParagraph = Math.round(totalWords / paragraphs.length);
            const estimatedTotalDuration = paragraphs.reduce((sum, para) => sum + para.estimatedDuration, 0);
            
            // 设置拆分结果
            const result: SplitResult = {
              paragraphs,
              totalParagraphs: paragraphs.length,
              totalWords,
              averageWordsPerParagraph,
              estimatedTotalDuration
            };
            
            setSplitResult(result);
            setShowResult(true);
          }
        } catch (error) {
          console.error('获取数据失败:', error);
          setContent(sampleContent);
        }
      }
    };
    
    fetchData();
  }, [file]);
  
  // 处理目标时长变化
  const handleDurationChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setTargetDuration(parseInt(e.target.value));
  };
  
  // 拆分段落
  const handleSplit = async () => {
    setIsSplitting(true);
    setShowResult(false);
    
    try {
      // 使用parseService的智能拆分方法
      const splitParagraphs = parseService.splitParagraphsByDuration(content, targetDuration);
      
      // 转换为Paragraph类型
      const paragraphs: Paragraph[] = splitParagraphs.map((para, index) => ({
        contentId: file?.id || 'sample-content',
        paragraphNumber: index + 1,
        text: para.text,
        wordCount: para.wordCount,
        estimatedDuration: para.estimatedDuration
      }));
      
      // 计算统计信息
      const totalWords = paragraphs.reduce((sum, para) => sum + para.wordCount, 0);
      const averageWordsPerParagraph = Math.round(totalWords / paragraphs.length);
      const estimatedTotalDuration = paragraphs.reduce((sum, para) => sum + para.estimatedDuration, 0);
      
      // 设置拆分结果
      const result: SplitResult = {
        paragraphs,
        totalParagraphs: paragraphs.length,
        totalWords,
        averageWordsPerParagraph,
        estimatedTotalDuration
      };
      
      setSplitResult(result);
      setShowResult(true);
    } catch (error) {
      console.error('段落拆分失败:', error);
      alert('段落拆分失败，请重试');
    } finally {
      setIsSplitting(false);
    }
  };
  
  // 重置拆分
  const handleReset = () => {
    setTargetDuration(10);
    setSplitResult(null);
    setShowResult(false);
  };
  
  // 保存拆分结果
  const handleSave = async () => {
    if (!splitResult || !file) return;
    
    setIsSaving(true);
    
    try {
      // 调用contentService更新段落
      const result = await contentService.updateParagraphs(
        file.id,
        splitResult.paragraphs.map(para => ({
          text: para.text,
          wordCount: para.wordCount,
          estimatedDuration: para.estimatedDuration
        }))
      );
      
      if (result.success) {
        alert('拆分结果已保存');
        navigate('/');
      } else {
        throw result.error || new Error('保存失败');
      }
    } catch (error) {
      console.error('保存拆分结果失败:', error);
      alert(`保存拆分结果失败: ${error instanceof Error ? error.message : '未知错误'}`);
    } finally {
      setIsSaving(false);
    }
  };
  
  // 如果没有文件信息，返回文件管理页面
  if (!file && !content) {
    navigate('/');
    return null;
  }
  
  return (
    <div className="main-content">
      <div className="file-processing-content" style={{ 
        display: 'flex', 
        flexDirection: 'column',
        gap: 'var(--spacing-8)', 
        padding: 'var(--spacing-8)', 
        flex: 1 
      }}>
        {/* 顶部：拆分设置 */}
        <aside className="settings-panel" style={{ 
          backgroundColor: 'var(--background-alt)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--border-radius-lg)',
          boxShadow: 'var(--shadow-sm)',
          overflow: 'hidden'
        }}>
          <div className="panel-header" style={{ 
            padding: 'var(--spacing-4)',
            borderBottom: '1px solid var(--border-color)',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            <h2 style={{ 
              fontSize: 'var(--font-size-lg)',
              fontWeight: 'var(--font-weight-semibold)',
              color: 'var(--text-primary)',
              margin: 0
            }}>段落拆分设置</h2>
            <button 
              className="btn btn-secondary"
              style={{ 
                padding: 'var(--spacing-3) var(--spacing-4)',
                borderRadius: 'var(--border-radius-md)',
                border: '1px solid var(--border-color)',
                background: 'var(--background-light)',
                color: 'var(--text-primary)',
                cursor: 'pointer',
                fontSize: 'var(--font-size-xs)',
                fontWeight: 'var(--font-weight-semibold)',
                transition: 'all var(--transition-base)'
              }}
              onClick={() => navigate('/')}
            >
              返回
            </button>
          </div>
          
          <div className="panel-content" style={{ 
            padding: 'var(--spacing-4)'
          }}>
            {/* 目标时长设置 */}
            <div className="setting-item" style={{ 
              marginBottom: 'var(--spacing-6)'
            }}>
              <label htmlFor="duration-slider" style={{ 
                display: 'block',
                fontWeight: 'var(--font-weight-medium)',
                marginBottom: 'var(--spacing-2)',
                fontSize: 'var(--font-size-sm)',
                color: 'var(--text-primary)'
              }}>目标时长</label>
              <div className="duration-display" style={{ 
                fontSize: 'var(--font-size-lg)',
                fontWeight: 'var(--font-weight-semibold)',
                margin: 'var(--spacing-2) 0',
                textAlign: 'center',
                color: 'var(--primary-color)'
              }}>
                <span id="duration-value">{targetDuration}</span> 分钟
              </div>
              <input 
                type="range" 
                id="duration-slider" 
                min="5" 
                max="20" 
                value={targetDuration} 
                step="1"
                onChange={handleDurationChange}
                style={{
                  width: '100%',
                  height: '6px',
                  background: 'var(--background-light)',
                  outline: 'none',
                  borderRadius: 'var(--border-radius-full)',
                  margin: 'var(--spacing-3) 0',
                  appearance: 'none'
                }}
              />
              <div className="slider-labels" style={{ 
                display: 'flex',
                justifyContent: 'space-between',
                fontSize: 'var(--font-size-xs)',
                color: 'var(--text-light)'
              }}>
                <span>5分钟</span>
                <span>20分钟</span>
              </div>
            </div>

            {/* 操作按钮 */}
            <div className="action-buttons" style={{ 
              display: 'flex',
              flexDirection: 'row', 
              gap: 'var(--spacing-3)',
              justifyContent: 'space-between'
            }}>
              <button 
                className="btn btn-primary" 
                id="split-btn"
                onClick={handleSplit}
                disabled={isSplitting}
                style={{
                  padding: 'var(--spacing-4) var(--spacing-6)',
                  borderRadius: 'var(--border-radius-md)',
                  border: 'none',
                  background: 'var(--primary-color)',
                  color: 'white',
                  cursor: 'pointer',
                  fontSize: 'var(--font-size-sm)',
                  fontWeight: 'var(--font-weight-semibold)',
                  transition: 'all var(--transition-base)',
                  flex: 1
                }}
              >
                {isSplitting ? '拆分中...' : (showResult ? '重新拆分' : '拆分')}
              </button>
              <button 
                className="btn btn-secondary" 
                id="reset-btn"
                onClick={handleReset}
                style={{
                  padding: 'var(--spacing-4) var(--spacing-6)',
                  borderRadius: 'var(--border-radius-md)',
                  border: '1px solid var(--border-color)',
                  background: 'var(--background-light)',
                  color: 'var(--text-primary)',
                  cursor: 'pointer',
                  fontSize: 'var(--font-size-sm)',
                  fontWeight: 'var(--font-weight-semibold)',
                  transition: 'all var(--transition-base)',
                  flex: 1
                }}
              >
                重置
              </button>
              <button 
                className="btn btn-primary" 
                id="save-btn"
                onClick={handleSave}
                disabled={!showResult || isSaving}
                style={{
                  padding: 'var(--spacing-4) var(--spacing-6)',
                  borderRadius: 'var(--border-radius-md)',
                  border: 'none',
                  background: 'var(--primary-color)',
                  color: 'white',
                  fontSize: 'var(--font-size-sm)',
                  fontWeight: 'var(--font-weight-semibold)',
                  transition: 'all var(--transition-base)',
                  opacity: !showResult || isSaving ? 0.5 : 1,
                  flex: 1
                }}
              >
                {isSaving ? '保存中...' : '保存'}
              </button>
            </div>
          </div>
        </aside>

        {/* 底部：内容预览与结果 */}
        <section className="content-panel" style={{ 
          flex: 1,
          backgroundColor: 'var(--background-alt)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--border-radius-lg)',
          boxShadow: 'var(--shadow-sm)',
          overflow: 'hidden'
        }}>
          {/* 内容预览 */}
          <div className="preview-section" style={{ 
            padding: 'var(--spacing-8)',
            borderBottom: '1px solid var(--border-color)'
          }}>
            <div className="section-header" style={{ 
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 'var(--spacing-6)',
              flexWrap: 'wrap',
              gap: 'var(--spacing-4)'
            }}>
              <h2 style={{ 
                fontSize: 'var(--font-size-xl)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--text-primary)',
                margin: 0,
                letterSpacing: '-0.02em'
              }}>文件内容预览</h2>
              <div className="file-info" style={{ 
                fontSize: 'var(--font-size-sm)',
                color: 'var(--text-secondary)',
                display: 'flex',
                gap: 'var(--spacing-4)'
              }}>
                <span className="file-name">{file?.name || 'sample_article.txt'}</span>
                <span className="file-size">{file ? `${(file.size / 1024 / 1024).toFixed(2)} MB` : '0.1 MB'}</span>
              </div>
            </div>
            <div 
              className="content-preview" 
              style={{ 
                backgroundColor: 'var(--background-light)',
                padding: 'var(--spacing-6)',
                borderRadius: 'var(--border-radius-lg)',
                border: '1px solid var(--border-color)',
                cursor: 'pointer',
                transition: 'all var(--transition-base)'
              }}
              onClick={() => setShowFullContent(true)}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = 'var(--background-color)';
                e.currentTarget.style.boxShadow = 'var(--shadow-md)';
                e.currentTarget.style.transform = 'translateY(-2px)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = 'var(--background-light)';
                e.currentTarget.style.boxShadow = 'none';
                e.currentTarget.style.transform = 'translateY(0)';
              }}
            >
              <h3 style={{ 
                fontSize: 'var(--font-size-xl)',
                fontWeight: 'var(--font-weight-semibold)',
                color: 'var(--text-primary)',
                margin: '0 0 var(--spacing-4) 0',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap' 
              }}>{file?.name || 'Sample Article'}</h3>
              {/* 显示原始内容，默认只显示5行 */}
              <p style={{ 
                margin: '0 0 var(--spacing-4) 0',
                color: 'var(--text-secondary)',
                lineHeight: 'var(--line-height-relaxed)',
                display: '-webkit-box',
                WebkitLineClamp: 5,
                WebkitBoxOrient: 'vertical',
                overflow: 'hidden',
                textOverflow: 'ellipsis'
              }}>{content}</p>
              <div style={{ 
                fontSize: 'var(--font-size-xs)',
                color: 'var(--text-light)',
                textAlign: 'right',
                marginTop: 'var(--spacing-2)'
              }}>点击查看全部内容</div>
            </div>
            
            {/* 完整内容弹窗 */}
            {showFullContent && (
              <div style={{
                position: 'fixed',
                top: 0,
                left: 0,
                width: '100%',
                height: '100%',
                backgroundColor: 'rgba(0, 0, 0, 0.5)',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                zIndex: 1000,
                padding: 'var(--spacing-8)'
              }}>
                <div style={{
                  backgroundColor: 'var(--background-alt)',
                  borderRadius: 'var(--border-radius-lg)',
                  maxWidth: '80%',
                  maxHeight: '80%',
                  boxShadow: 'var(--shadow-lg)',
                  position: 'relative',
                  width: '100%',
                  display: 'flex',
                  flexDirection: 'column'
                }}>
                  {/* 弹窗头部 - 固定 */}
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: 'var(--spacing-8) var(--spacing-8) var(--spacing-4) var(--spacing-8)',
                    borderBottom: '1px solid var(--border-color)',
                    backgroundColor: 'var(--background-alt)',
                    borderRadius: 'var(--border-radius-lg) var(--border-radius-lg) 0 0'
                  }}>
                    <h2 style={{
                      fontSize: 'var(--font-size-xl)',
                      fontWeight: 'var(--font-weight-semibold)',
                      color: 'var(--text-primary)',
                      margin: 0
                    }}>{file?.name || 'Sample Article'}</h2>
                    <button
                      onClick={() => setShowFullContent(false)}
                      style={{
                        background: 'none',
                        border: 'none',
                        fontSize: 'var(--font-size-xl)',
                        cursor: 'pointer',
                        color: 'var(--text-secondary)',
                        borderRadius: 'var(--border-radius-full)',
                        width: '40px',
                        height: '40px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        transition: 'all var(--transition-base)'
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = 'var(--background-light)';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = 'transparent';
                      }}
                    >
                      ×
                    </button>
                  </div>
                  
                  {/* 弹窗内容 - 可滚动 */}
                  <div style={{
                    lineHeight: 'var(--line-height-relaxed)',
                    color: 'var(--text-primary)',
                    fontSize: 'var(--font-size-base)',
                    padding: 'var(--spacing-8)',
                    overflow: 'auto',
                    flex: 1
                  }}>{content}</div>
                </div>
              </div>
            )}
          </div>

          {/* 拆分结果 */}
          {showResult && splitResult && (
            <div className="result-section" id="result-section" style={{ 
              padding: 'var(--spacing-8)'
            }}>
              <div className="section-header" style={{ 
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: 'var(--spacing-6)',
                flexWrap: 'wrap',
                gap: 'var(--spacing-4)'
              }}>
                <h2 style={{ 
                  fontSize: 'var(--font-size-xl)',
                  fontWeight: 'var(--font-weight-semibold)',
                  color: 'var(--text-primary)',
                  margin: 0,
                  letterSpacing: '-0.02em'
                }}>拆分结果</h2>
                <div className="result-stats" style={{ 
                  display: 'flex',
                  gap: 'var(--spacing-8)',
                  color: 'var(--text-secondary)',
                  fontSize: 'var(--font-size-sm)'
                }}>
                  <span>总段落数: <strong style={{ color: 'var(--primary-color)' }} id="total-paragraphs">{splitResult.totalParagraphs}</strong></span>
                  <span>总词数: <strong style={{ color: 'var(--primary-color)' }}>{splitResult.totalWords}</strong></span>
                  <span>平均每段: <strong style={{ color: 'var(--primary-color)' }}>{splitResult.averageWordsPerParagraph} 词</strong></span>
                  <span>预估总时长: <strong style={{ color: 'var(--primary-color)' }}>{splitResult.estimatedTotalDuration.toFixed(1)} 分钟</strong></span>
                </div>
              </div>
              <div className="paragraphs-list" id="paragraphs-list" style={{ 
                display: 'flex',
                flexDirection: 'column',
                gap: 'var(--spacing-4)',
                marginTop: 'var(--spacing-4)'
              }}>
                {splitResult.paragraphs.map((paragraph, index) => (
                  <div 
                    key={index} 
                    className="paragraph-item" 
                    style={{ 
                      backgroundColor: 'var(--background-light)',
                      padding: 'var(--spacing-5)',
                      borderRadius: 'var(--border-radius-lg)',
                      borderLeft: '4px solid var(--primary-color)',
                      transition: 'all var(--transition-base)',
                      border: '1px solid var(--border-color)',
                      cursor: 'pointer'
                    }}
                    onClick={() => {
                      setSelectedParagraph(paragraph);
                      setShowParagraphModal(true);
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor = 'var(--background-color)';
                      e.currentTarget.style.boxShadow = 'var(--shadow-md)';
                      e.currentTarget.style.transform = 'translateY(-2px)';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = 'var(--background-light)';
                      e.currentTarget.style.boxShadow = 'none';
                      e.currentTarget.style.transform = 'translateY(0)';
                    }}
                  >
                    <div className="paragraph-header" style={{ 
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      marginBottom: 'var(--spacing-2)'
                    }}>
                      <span className="paragraph-number" style={{ 
                        fontWeight: 'var(--font-weight-semibold)',
                        color: 'var(--text-primary)',
                        fontSize: 'var(--font-size-sm)'
                      }}>段落 {paragraph.paragraphNumber}</span>
                      <span className="paragraph-meta" style={{ 
                        fontSize: 'var(--font-size-xs)',
                        color: 'var(--text-secondary)'
                      }}>
                        {paragraph.wordCount} 词 · {paragraph.estimatedDuration.toFixed(1)} 分钟
                      </span>
                    </div>
                    <div className="paragraph-text" style={{ 
                      fontSize: 'var(--font-size-sm)',
                      lineHeight: 'var(--line-height-relaxed)',
                      color: 'var(--text-secondary)',
                      display: '-webkit-box',
                      WebkitLineClamp: 5,
                      WebkitBoxOrient: 'vertical',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis'
                    }}>{paragraph.text}</div>
                  </div>
                ))}
              </div>
              
              {/* 段落详情弹窗 */}
              {showParagraphModal && selectedParagraph && (
                <div style={{
                  position: 'fixed',
                  top: 0,
                  left: 0,
                  width: '100%',
                  height: '100%',
                  backgroundColor: 'rgba(0, 0, 0, 0.5)',
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  zIndex: 1000,
                  padding: 'var(--spacing-8)'
                }}>
                  <div style={{
                    backgroundColor: 'var(--background-alt)',
                    borderRadius: 'var(--border-radius-lg)',
                    maxWidth: '80%',
                    maxHeight: '80%',
                    boxShadow: 'var(--shadow-lg)',
                    position: 'relative',
                    width: '100%',
                    display: 'flex',
                    flexDirection: 'column'
                  }}>
                    {/* 弹窗头部 - 固定 */}
                    <div style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: 'var(--spacing-8) var(--spacing-8) var(--spacing-4) var(--spacing-8)',
                      borderBottom: '1px solid var(--border-color)',
                      backgroundColor: 'var(--background-alt)',
                      borderRadius: 'var(--border-radius-lg) var(--border-radius-lg) 0 0'
                    }}>
                      <div>
                        <h2 style={{
                          fontSize: 'var(--font-size-xl)',
                          fontWeight: 'var(--font-weight-semibold)',
                          color: 'var(--text-primary)',
                          margin: 0
                        }}>段落 {selectedParagraph.paragraphNumber}</h2>
                        <div style={{
                          fontSize: 'var(--font-size-sm)',
                          color: 'var(--text-secondary)',
                          marginTop: 'var(--spacing-1)'
                        }}>
                          {selectedParagraph.wordCount} 词 · {selectedParagraph.estimatedDuration.toFixed(1)} 分钟
                        </div>
                      </div>
                      <button
                        onClick={() => setShowParagraphModal(false)}
                        style={{
                          background: 'none',
                          border: 'none',
                          fontSize: 'var(--font-size-xl)',
                          cursor: 'pointer',
                          color: 'var(--text-secondary)',
                          borderRadius: 'var(--border-radius-full)',
                          width: '40px',
                          height: '40px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          transition: 'all var(--transition-base)'
                        }}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.backgroundColor = 'var(--background-light)';
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.backgroundColor = 'transparent';
                        }}
                      >
                        ×
                      </button>
                    </div>
                    
                    {/* 弹窗内容 - 可滚动 */}
                    <div style={{
                      lineHeight: 'var(--line-height-relaxed)',
                      color: 'var(--text-primary)',
                      fontSize: 'var(--font-size-base)',
                      padding: 'var(--spacing-8)',
                      overflow: 'auto',
                      flex: 1
                    }}>{selectedParagraph.text}</div>
                  </div>
                </div>
              )}
            </div>
          )}
        </section>
      </div>
    </div>
  );
};

export default FileProcessingPage;
