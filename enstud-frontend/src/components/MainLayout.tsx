import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Button, Avatar, Dropdown } from 'antd';
import {
  HomeOutlined, BookOutlined, MessageOutlined, EditOutlined,
  TranslationOutlined, TeamOutlined, LogoutOutlined, UserOutlined,
  ReadOutlined,
} from '@ant-design/icons';

const { Header, Sider, Content } = Layout;

const menuItems = [
  { key: '/', icon: <HomeOutlined />, label: '首页' },
  { key: '/word', icon: <BookOutlined />, label: '单词学习' },
  { key: '/chat', icon: <MessageOutlined />, label: 'AI 对话' },
  { key: '/writing', icon: <EditOutlined />, label: '写作练习' },
  { key: '/translate', icon: <TranslationOutlined />, label: '翻译工具' },
  { key: '/read', icon: <ReadOutlined />, label: '热门阅读' },
  { key: '/forum', icon: <TeamOutlined />, label: '学习论坛' },
];

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={200} style={{ background: '#fff', borderRight: '1px solid #f0f0f0' }}>
        <div style={{ padding: '16px', textAlign: 'center', fontWeight: 700, fontSize: 18, color: '#1677ff' }}>
          EnStudApp
        </div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname === '/' ? '/' : '/' + location.pathname.split('/')[1]]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', justifyContent: 'flex-end', alignItems: 'center', borderBottom: '1px solid #f0f0f0' }}>
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
        <Content style={{ padding: 24, background: '#f5f5f5', overflow: 'auto' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
