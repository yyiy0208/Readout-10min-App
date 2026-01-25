
import './App.css'
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import FileManagementPage from './pages/FileManagementPage'
import FileProcessingPage from './pages/FileProcessingPage'

const App: React.FC = () => {
  return (
    <div className="app-container">
      {/* 路由容器 */}
      <Router>
        {/* 顶部导航栏 */}
        <Navbar />

        <Routes>
          {/* 文件管理页面 */}
          <Route path="/" element={<FileManagementPage />} />
          {/* 文件处理页面 */}
          <Route path="/file-processing" element={<FileProcessingPage />} />
          {/* 其他页面可以在这里添加 */}
        </Routes>

        {/* 页脚 */}
        <footer className="footer">
          <p>Readout-10min-App © 2026</p>
        </footer>
      </Router>
    </div>
  )
}

export default App
