import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Input, Button, Tabs, message, Typography } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, ReadOutlined } from '@ant-design/icons';
import { login, register } from '../api';
import { useAuthStore } from '../stores';

const { Text } = Typography;

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login: storeLogin } = useAuthStore();

  const handleLogin = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      const res = await login(values);
      localStorage.setItem('token', res.data.accessToken);
      localStorage.setItem('user', JSON.stringify({
        username: res.data.username,
        nickname: res.data.nickname,
        userId: res.data.userId,
      }));
      // 同步到 zustand store
      storeLogin(values);
      message.success('登录成功');
      navigate('/');
    } catch (err: unknown) {
      message.error((err as { msg?: string })?.msg || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (values: { username: string; password: string; email: string }) => {
    setLoading(true);
    try {
      await register(values);
      message.success('注册成功，请登录');
    } catch (err: unknown) {
      message.error((err as { msg?: string })?.msg || '注册失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center',
      minHeight: '100vh', background: 'linear-gradient(135deg, #e6f4ff 0%, #f0f5ff 50%, #f6ffed 100%)',
      padding: '16px',
    }}>
      <Card style={{ width: '100%', maxWidth: 420, boxShadow: '0 4px 24px rgba(0,0,0,0.08)' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <h1 style={{ color: '#1677ff', marginBottom: 8, fontSize: 24 }}>
            <ReadOutlined style={{ marginRight: 8 }} />
            EnStudApp
          </h1>
          <Text type="secondary" style={{ fontSize: 14 }}>
            在阅读中学习，在对话中巩固
          </Text>
        </div>
        <Tabs items={[
          {
            key: 'login', label: '登录',
            children: (
              <Form onFinish={handleLogin} autoComplete="off">
                <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
                  <Input prefix={<UserOutlined />} placeholder="用户名" size="large" />
                </Form.Item>
                <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
                  <Input.Password prefix={<LockOutlined />} placeholder="密码" size="large" />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" htmlType="submit" block size="large" loading={loading}>登录</Button>
                </Form.Item>
              </Form>
            ),
          },
          {
            key: 'register', label: '注册',
            children: (
              <Form onFinish={handleRegister} autoComplete="off">
                <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
                  <Input prefix={<UserOutlined />} placeholder="用户名" size="large" />
                </Form.Item>
                <Form.Item name="email" rules={[{ required: true, type: 'email', message: '请输入正确邮箱' }]}>
                  <Input prefix={<MailOutlined />} placeholder="邮箱" size="large" />
                </Form.Item>
                <Form.Item name="password" rules={[{ required: true, min: 6, message: '密码至少6位' }]}>
                  <Input.Password prefix={<LockOutlined />} placeholder="密码" size="large" />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" htmlType="submit" block size="large" loading={loading}>注册</Button>
                </Form.Item>
              </Form>
            ),
          },
        ]} />
      </Card>
    </div>
  );
}
