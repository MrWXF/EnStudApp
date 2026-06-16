import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Input, Button, Tabs, message } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import { login, register } from '../api';

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

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
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
      <Card style={{ width: 400 }}>
        <h1 style={{ textAlign: 'center', color: '#1677ff', marginBottom: 24 }}>EnStudApp 英语学习助手</h1>
        <Tabs items={[
          {
            key: 'login', label: '登录',
            children: (
              <Form onFinish={handleLogin} autoComplete="off">
                <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
                  <Input prefix={<UserOutlined />} placeholder="用户名" />
                </Form.Item>
                <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
                  <Input.Password prefix={<LockOutlined />} placeholder="密码" />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" htmlType="submit" block loading={loading}>登录</Button>
                </Form.Item>
              </Form>
            ),
          },
          {
            key: 'register', label: '注册',
            children: (
              <Form onFinish={handleRegister} autoComplete="off">
                <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
                  <Input prefix={<UserOutlined />} placeholder="用户名" />
                </Form.Item>
                <Form.Item name="email" rules={[{ required: true, type: 'email', message: '请输入正确邮箱' }]}>
                  <Input prefix={<MailOutlined />} placeholder="邮箱" />
                </Form.Item>
                <Form.Item name="password" rules={[{ required: true, min: 6, message: '密码至少6位' }]}>
                  <Input.Password prefix={<LockOutlined />} placeholder="密码" />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" htmlType="submit" block loading={loading}>注册</Button>
                </Form.Item>
              </Form>
            ),
          },
        ]} />
      </Card>
    </div>
  );
}
