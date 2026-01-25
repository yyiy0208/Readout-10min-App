import { ParseResult } from '../types';

// 文件解析服务类
class ParseService {
  // 解析文件的主方法
  async parseFile(file: File): Promise<ParseResult> {
    try {
      // 获取文件扩展名
      const fileExtension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'));
      
      // 读取文件为ArrayBuffer
      const arrayBuffer = await file.arrayBuffer();
      
      // 根据文件类型选择解析器
      let content: string;
      switch (fileExtension) {
        case '.pdf':
          content = await this.parsePDF(arrayBuffer);
          break;
        case '.docx':
        case '.doc':
          content = await this.parseWord(arrayBuffer);
          break;
        case '.txt':
          content = await this.parseTXT(arrayBuffer);
          break;
        default:
          throw new Error(`不支持的文件类型: ${fileExtension}`);
      }
      
      // 拆分段落
      const paragraphs = this.splitParagraphs(content);
      
      return {
        content,
        paragraphs
      };
    } catch (error) {
      console.error('文件解析失败:', error);
      throw error;
    }
  }
  
  // 解析PDF文件
  private async parsePDF(arrayBuffer: ArrayBuffer): Promise<string> {
    try {
      // 直接使用pdfjs-dist库进行PDF解析，绕过pdf-parse库
      // 这样可以更精确地控制workerSrc的设置
      
      // 动态导入pdfjs-dist库
      const pdfjsLib = await import('pdfjs-dist');
      
      // 使用项目本地的worker文件路径，避免CDN 404错误
      // 让Vite处理worker文件的路径
      pdfjsLib.GlobalWorkerOptions.workerSrc = new URL('pdfjs-dist/build/pdf.worker.min.mjs', import.meta.url).href;
      
      // 使用pdfjsLib直接解析PDF
      const loadingTask = pdfjsLib.getDocument({
        data: arrayBuffer,
        verbosity: 0
      });
      
      // 获取PDF文档
      const pdfDocument = await loadingTask.promise;
      
      // 获取总页数
      const numPages = pdfDocument.numPages;
      
      // 用于存储所有页面的文本
      let fullText = '';
      
      // 遍历所有页面，获取文本
      for (let pageNum = 1; pageNum <= numPages; pageNum++) {
        // 获取单个页面
        const page = await pdfDocument.getPage(pageNum);
        
        // 获取页面文本
        const textContent = await page.getTextContent();
        
        // 提取文本内容
        const pageText = textContent.items.map((item: any) => item.str).join(' ');
        
        // 添加到完整文本中
        fullText += pageText + '\n\n';
        
        // 释放页面资源
        await page.cleanup();
      }
      
      // 关闭PDF文档
      await pdfDocument.destroy();
      
      // 验证解析结果
      if (!fullText || fullText.trim().length === 0) {
        throw new Error('PDF解析返回空内容');
      }
      
      return fullText.trim();
    } catch (error) {
      console.error('PDF解析失败:', error);
      console.error('PDF解析错误详情:', error instanceof Error ? error.stack : JSON.stringify(error, null, 2));
      throw new Error(`PDF解析失败: ${error instanceof Error ? error.message : '未知错误'}`);
    }
  }
  
  // 解析Word文件
  private async parseWord(arrayBuffer: ArrayBuffer): Promise<string> {
    try {
      const mammoth = await import('mammoth');
      
      // 获取正确的mammoth对象
      const mammothInstance = mammoth.default || mammoth;
      
      // 检查是否有extractRawText方法
      if (!mammothInstance.extractRawText || typeof mammothInstance.extractRawText !== 'function') {
        throw new Error('Word解析库导出错误: 无法找到extractRawText方法');
      }
      
      // 解析Word文件
      const result = await mammothInstance.extractRawText({ arrayBuffer });
      
      // 验证解析结果
      if (!result.value || result.value.trim().length === 0) {
        throw new Error('Word解析返回空内容');
      }
      
      return result.value;
    } catch (error) {
      console.error('Word解析失败:', error);
      throw new Error(`Word解析失败: ${error instanceof Error ? error.message : '未知错误'}`);
    }
  }
  
  // 解析TXT文件
  private parseTXT(arrayBuffer: ArrayBuffer): string {
    try {
      const decoder = new TextDecoder('utf-8');
      const content = decoder.decode(arrayBuffer);
      
      // 验证解析结果
      if (!content || content.trim().length === 0) {
        throw new Error('TXT解析返回空内容');
      }
      
      return content;
    } catch (error) {
      console.error('TXT解析失败:', error);
      throw new Error(`TXT解析失败: ${error instanceof Error ? error.message : '未知错误'}`);
    }
  }
  
  // 计算词数
  private countWords(text: string): number {
    // 简单的英文词数统计，实际应用中可能需要更复杂的算法
    // 处理中文和英文混合的情况
    if (/[\u4e00-\u9fa5]/.test(text)) {
      // 包含中文，使用字符计数（中文每个字算1词）
      return text.replace(/\s+/g, '').length;
    } else {
      // 英文，按空格拆分
      return text.trim().split(/\s+/).filter(word => word.length > 0).length;
    }
  }
  
  // 估算阅读时长（分钟）
  private estimateDuration(wordCount: number, text: string): number {
    // 假设平均阅读速度：
    // 中文：300字/分钟
    // 英文：200词/分钟
    const chineseRegex = /[\u4e00-\u9fa5]/;
    const readingSpeed = chineseRegex.test(text) ? 300 : 200;
    return wordCount / readingSpeed;
  }
  
  // 按目标时长拆分段落
  public splitParagraphsByDuration(content: string, targetDuration: number = 10): Array<{text: string, wordCount: number, estimatedDuration: number}> {
    // 1. 首先按空行拆分原始段落
    const originalParagraphs = content
      .replace(/\r\n/g, '\n')
      .split(/\n\s*\n/)
      .filter(para => para.trim().length > 0)
      .map(para => para.trim());
    
    // 2. 统计总词数
    const totalWords = originalParagraphs.reduce((sum, para) => sum + this.countWords(para), 0);
    
    // 3. 计算每个段落的阅读速度
    const chineseRegex = /[\u4e00-\u9fa5]/;
    const contentHasChinese = chineseRegex.test(content);
    const readingSpeed = contentHasChinese ? 300 : 200;
    
    // 4. 计算总估算时长
    const estimatedTotalDuration = totalWords / readingSpeed;
    
    // 5. 计算需要的段落数量
    const requiredParagraphs = Math.ceil(estimatedTotalDuration / targetDuration);
    
    // 6. 智能合并或拆分段落
    let result: Array<{text: string, wordCount: number, estimatedDuration: number}> = [];
    
    if (originalParagraphs.length <= requiredParagraphs) {
      // 原始段落数量小于等于需要的段落数量，直接返回
      result = originalParagraphs.map(para => {
        const wordCount = this.countWords(para);
        return {
          text: para,
          wordCount,
          estimatedDuration: this.estimateDuration(wordCount, para)
        };
      });
    } else {
      // 需要合并段落
      const paragraphsPerGroup = Math.ceil(originalParagraphs.length / requiredParagraphs);
      
      for (let i = 0; i < originalParagraphs.length; i += paragraphsPerGroup) {
        const group = originalParagraphs.slice(i, i + paragraphsPerGroup);
        const combinedText = group.join('\n\n');
        const wordCount = this.countWords(combinedText);
        
        result.push({
          text: combinedText,
          wordCount,
          estimatedDuration: this.estimateDuration(wordCount, combinedText)
        });
      }
    }
    
    return result;
  }
  
  // 拆分段落（原始方法，保持兼容性）
  private splitParagraphs(content: string): string[] {
    // 1. 移除多余的空白字符
    const cleanedContent = content
      .replace(/\r\n/g, '\n') // 统一换行符
      .replace(/\n{3,}/g, '\n\n') // 最多保留两个连续换行
      .trim();
    
    // 2. 按空行拆分段落
    const paragraphs = cleanedContent.split(/\n\s*\n/);
    
    // 3. 过滤空段落并修剪每个段落
    return paragraphs
      .filter(para => para.trim().length > 0)
      .map(para => para.trim());
  }
}

// 导出单例实例
export default new ParseService();
