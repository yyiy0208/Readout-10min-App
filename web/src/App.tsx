
import { useState } from 'react'
import { Layout, Menu } from 'antd'
import type { MenuProps } from 'antd'
import './App.css'

const { Header, Content, Footer } = Layout

type MenuItem = Required<MenuProps>['items'][number]

const App: React.FC = () => {
  const [current, setCurrent] = useState('file-management')

  const items: MenuItem[] = [
    {
      key: 'file-management',
      label: '文件管理',
    },
    {
      key: 'file-processing',
      label: '文件处理',
    },
  ]

  const onClick: MenuProps['onClick'] = (e) => {
    setCurrent(e.key)
  }

  return (
    <Layout className="layout">
      <Header>
        <div className="logo" />
        <Menu
          theme="dark"
          mode="horizontal"
          defaultSelectedKeys={['file-management']}
          items={items}
          onClick={onClick}
        />
      </Header>
      <Content style={{ padding: '0 50px' }}>
        <div className="site-layout-content">
          {current === 'file-management' && (
            <div>
              <h1>文件管理页面</h1>
              <p>文件上传和管理功能将在这里实现</p>
            </div>
          )}
          {current === 'file-processing' && (
            <div>
              <h1>文件处理页面</h1>
              <p>文件解析和段落拆分功能将在这里实现</p>
            </div>
          )}
        </div>
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        Readout-10min-App ©2026 Created by Vibe Coding
      </Footer>
    </Layout>
  )
}

export default App
