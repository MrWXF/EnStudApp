import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Button, Avatar, Dropdown, Drawer, Grid } from 'antd';
import {
  HomeOutlined, BookOutlined, MessageOutlined, EditOutlined,
  TranslationOutlined, TeamOutlined, LogoutOutlined, UserOutlined,
  ReadOutlined, RiseOutlined, AppstoreOutlined, ExperimentOutlined,
} from '@ant-design/icons';

const { Header, Sider, Content } = Layout;
const { useBreakpoint } = Grid;

/** 核心学习模块 */
const coreMenuItems = [
  { key: '/dashboard', icon: <RiseOutlined />, label: '学习仪表盘' },
  { key: '/read', icon: <ReadOutlined />, label: '阅读' },
  { key: '/word', icon: <BookOutlined />, label: '单词' },
  { key: '/chat', icon: <MessageOutlined />, label: 'AI 对话' },
];

/** 其他工具模块 */
const toolMenuItems = [
  { key: '/writing', icon: <EditOutlined />, label: '写作' },
  { key: '/translate', icon: <TranslationOutlined />, label: '翻译' },
  { key: '/forum', icon: <TeamOutlined />, label: '论坛' },
  { key: '/vocab-test', icon: <ExperimentOutlined />, label: '词汇量测试' },
];

/** 桌面端完整菜单 */
const desktopMenuItems = [
  ...coreMenuItems,
  { type: 'divider' as const },
  {
    key: 'tools-group',
    label: '其他工具',
    type: 'group' as const,
    children: toolMenuItems,
  },
];

/** 移动端底部 Tab */
const mobileTabItems = [
  { key: '/dashboard', icon: <RiseOutlined />, label: '首页' },
  { key: '/read', icon: <ReadOutlined />, label: '阅读' },
  { key: '/word', icon: <BookOutlined />, label: '单词' },
  { key: '/chat', icon: <MessageOutlined />, label: '对话' },
];

/** "更多" 抽屉中的菜单项 */
const moreMenuItems = [
  { key: '/', icon: <HomeOutlined />, label: '首页概览' },
  ...toolMenuItems,
];

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const screens = useBreakpoint();
  const isMobile = !screens.md; // < 768px
  const [drawerOpen, setDrawerOpen] = useState(false);

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  /** 当前选中 key —— 匹配路由 */
  const selectedKey = location.pathname === '/' ? '/dashboard' : location.pathname;

  /** 桌面端侧边栏 */
  const renderDesktopSider = () => (
    <Sider width={200} style={{ background: '#fff', borderRight: '1px solid #f0f0f0' }}>
      <div
        style={{ padding: '16px', textAlign: 'center', fontWeight: 700, fontSize: 18, color: '#1677ff', cursor: 'pointer' }}
        onClick={() => navigate('/')}
      >
        EnStudApp
      </div>
      <Menu
        mode="inline"
        selectedKeys={[selectedKey]}
        items={desktopMenuItems}
        onClick={({ key }) => navigate(key)}
        style={{ borderRight: 'none' }}
      />
    </Sider>
  );

  /** 移动端底部 Tab 栏 */
  const renderMobileTabs = () => {
    const activeKey = mobileTabItems.find(t => selectedKey.startsWith(t.key))?.key || '/dashboard';
    return (
      <div style={{
        position: 'fixed', bottom: 0, left: 0, right: 0, zIndex: 1000,
        background: '#fff', borderTop: '1px solid #f0f0f0',
        display: 'flex', paddingBottom: 'env(safe-area-inset-bottom, 0px)',
      }}>
        {mobileTabItems.map(tab => {
          const isActive = activeKey === tab.key;
          return (
            <div
              key={tab.key}
              onClick={() => navigate(tab.key)}
              style={{
                flex: 1, textAlign: 'center', padding: '8px 0 6px',
                color: isActive ? '#1677ff' : '#8c8c8c',
                fontSize: 10, cursor: 'pointer',
                transition: 'color 0.2s',
              }}
            >
              <div style={{ fontSize: 20, marginBottom: 2 }}>{tab.icon}</div>
              <div style={{ fontWeight: isActive ? 600 : 400 }}>{tab.label}</div>
            </div>
          );
        })}
        {/* "更多" 按钮 */}
        <div
          onClick={() => setDrawerOpen(true)}
          style={{
            flex: 1, textAlign: 'center', padding: '8px 0 6px',
            color: '#8c8c8c', fontSize: 10, cursor: 'pointer',
          }}
        >
          <div style={{ fontSize: 20, marginBottom: 2 }}><AppstoreOutlined /></div>
          <div>更多</div>
        </div>
      </div>
    );
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* 桌面端侧边栏 */}
      {!isMobile && renderDesktopSider()}

      {/* 移动端 Header */}
      {isMobile && (
        <Header style={{
          background: '#fff', padding: '0 16px',
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          borderBottom: '1px solid #f0f0f0', height: 48,
          position: 'sticky', top: 0, zIndex: 100,
        }}>
          <span style={{ fontWeight: 700, fontSize: 16, color: '#1677ff' }}>EnStudApp</span>
          <Dropdown menu={{
            items: [
              { key: 'profile', icon: <UserOutlined />, label: user.nickname || user.username || '用户', disabled: true },
              { type: 'divider' },
              { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', onClick: handleLogout },
            ],
          }}>
            <Button type="text" icon={<Avatar size="small" icon={<UserOutlined />} />} />
          </Dropdown>
        </Header>
      )}

      <Layout>
        {/* 桌面端 Header */}
        {!isMobile && (
          <Header style={{
            background: '#fff', padding: '0 24px',
            display: 'flex', justifyContent: 'flex-end', alignItems: 'center',
            borderBottom: '1px solid #f0f0f0',
          }}>
            <Dropdown menu={{
              items: [
                { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', onClick: handleLogout },
              ],
            }}>
              <Button type="text" icon={<Avatar size="small" icon={<UserOutlined />} />}>
                {user.nickname || user.username || '用户'}
              </Button>
            </Dropdown>
          </Header>
        )}

        <Content style={{
          padding: isMobile ? '12px 12px 72px' : 24,
          background: '#f5f5f5',
          overflow: 'auto',
        }}>
          <Outlet />
        </Content>
      </Layout>

      {/* 移动端底部 Tab */}
      {isMobile && renderMobileTabs()}

      {/* "更多" 抽屉 */}
      <Drawer
        title="更多功能"
        placement="right"
        onClose={() => setDrawerOpen(false)}
        open={drawerOpen}
        width={260}
      >
        <Menu
          mode="inline"
          items={moreMenuItems}
          onClick={({ key }) => { navigate(key); setDrawerOpen(false); }}
          style={{ borderRight: 'none' }}
        />
      </Drawer>
    </Layout>
  );
}
