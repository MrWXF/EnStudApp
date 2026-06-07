import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Input, Button, Select, Space, message } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { createPost, getCategories } from '../api';

const { TextArea } = Input;

export default function CreatePostPage() {
  const [categories, setCategories] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  useEffect(() => {
    getCategories().then((res: any) => setCategories(res.data || []));
  }, []);

  const handleSubmit = async (values: any) => {
    setLoading(true);
    try {
      const res: any = await createPost({
        title: values.title,
        content: values.content,
        categoryId: values.categoryId,
        tags: values.tags,
      });
      message.success('发帖成功');
      navigate(`/forum/${res.data.id}`);
    } catch {
      // 错误已在 client 统一处理
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/forum')}>返回论坛</Button>
      </Space>
      <Card title="发布新帖">
        <Form form={form} layout="vertical" onFinish={handleSubmit} style={{ maxWidth: 720 }}>
          <Form.Item name="title" label="标题" rules={[
            { required: true, message: '请输入帖子标题' },
            { max: 200, message: '标题不能超过200字' },
          ]}>
            <Input placeholder="请输入帖子标题" maxLength={200} showCount />
          </Form.Item>

          <Form.Item name="categoryId" label="板块" rules={[{ required: true, message: '请选择板块' }]}>
            <Select
              placeholder="请选择板块"
              options={categories.map((c: any) => ({ label: c.name, value: c.id }))}
            />
          </Form.Item>

          <Form.Item name="tags" label="标签" extra="多个标签用逗号分隔，如：学习心得,考试,四级">
            <Input placeholder="输入标签，逗号分隔" />
          </Form.Item>

          <Form.Item name="content" label="内容" rules={[
            { required: true, message: '请输入帖子内容' },
          ]}>
            <TextArea
              rows={12}
              placeholder="请输入帖子内容..."
              showCount
              maxLength={5000}
            />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading}>发布帖子</Button>
              <Button onClick={() => navigate('/forum')}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
