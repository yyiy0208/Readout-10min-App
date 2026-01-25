
import './App.css'
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import FileManagementPage from './pages/FileManagementPage'

const App: React.FC = () => {
  return (
    <div className="app-container">
      {/* 顶部导航栏 */}
      <Navbar />

      {/* 路由容器 */}
      <Router>
        <Routes>
          {/* 文件管理页面 */}
          <Route path="/" element={<FileManagementPage />} />
          {/* 其他页面可以在这里添加 */}
        </Routes>
      </Router>

      {/* 页脚 */}
      <footer className="footer">
        <p>Readout-10min-App © 2026</p>
      </footer>
    </div>
  )
}

export default App
