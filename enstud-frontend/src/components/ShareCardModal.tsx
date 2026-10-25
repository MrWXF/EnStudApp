import { useEffect, useRef, useState } from 'react';
import { Modal, Button, Spin } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { getDailyJourney, getUserStats } from '../api/dashboard';
import type { DailyJourney, UserStats, VocabTestResult } from '../types';

const W = 750;
const H = 1100;
const FONT = `-apple-system, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif`;

interface ShareCardModalProps {
  open: boolean;
  onClose: () => void;
  /** 卡片类型：vocab 词汇量成绩卡 / stats 学习成就卡 */
  variant: 'vocab' | 'stats';
  /** variant=vocab 时的测试结果 */
  vocabResult?: VocabTestResult | null;
}

/**
 * 分享卡片：Canvas 绘制成图，长按/下载保存到本地后分享
 */
export default function ShareCardModal({ open, onClose, variant, vocabResult }: ShareCardModalProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [stats, setStats] = useState<UserStats | null>(null);
  const [journey, setJourney] = useState<DailyJourney | null>(null);
  const [loading, setLoading] = useState(false);

  // 打开时拉取数据
  useEffect(() => {
    if (!open) return;
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setLoading(true);
    Promise.all([
      getUserStats().then((res) => res.data).catch(() => null),
      getDailyJourney().then((res) => res.data).catch(() => null),
    ]).then(([statsData, journeyData]) => {
      setStats(statsData);
      setJourney(journeyData);
    }).finally(() => setLoading(false));
  }, [open]);

  // 数据就绪后绘制
  useEffect(() => {
    if (!open || loading || !stats || !canvasRef.current) return;
    drawCard(canvasRef.current, { variant, vocabResult, stats, journey });
  }, [open, loading, stats, journey, variant, vocabResult]);

  const handleDownload = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    canvas.toBlob((blob) => {
      if (!blob) return;
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = variant === 'vocab' ? 'enstud-vocab-card.png' : 'enstud-stats-card.png';
      a.click();
      URL.revokeObjectURL(url);
    }, 'image/png');
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      title={variant === 'vocab' ? '词汇量成绩卡' : '学习成就卡'}
      footer={
        <Button type="primary" icon={<DownloadOutlined />} onClick={handleDownload}>
          下载图片
        </Button>
      }
      width={420}
      centered
    >
      {loading ? (
        <div style={{ textAlign: 'center', padding: 48 }}>
          <Spin />
        </div>
      ) : (
        <canvas
          ref={canvasRef}
          width={W}
          height={H}
          style={{ width: '100%', borderRadius: 12, display: 'block' }}
        />
      )}
    </Modal>
  );
}

/** 绘制卡片（仅文字与图形，不加载远程图片，避免 canvas 跨域污染） */
function drawCard(
  canvas: HTMLCanvasElement,
  data: {
    variant: 'vocab' | 'stats';
    vocabResult?: VocabTestResult | null;
    stats: UserStats;
    journey: DailyJourney | null;
  },
) {
  const ctx = canvas.getContext('2d');
  if (!ctx) return;
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const nickname = user.nickname || user.username || '学习者';
  const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });

  // 背景：淡蓝紫渐变 + 装饰圆
  const bg = ctx.createLinearGradient(0, 0, W, H);
  bg.addColorStop(0, '#eef3ff');
  bg.addColorStop(1, '#f6efff');
  ctx.fillStyle = bg;
  ctx.fillRect(0, 0, W, H);
  ctx.globalAlpha = 0.5;
  ctx.fillStyle = '#d6e4ff';
  ctx.beginPath(); ctx.arc(W - 80, 120, 160, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#efdbff';
  ctx.beginPath(); ctx.arc(60, H - 140, 180, 0, Math.PI * 2); ctx.fill();
  ctx.globalAlpha = 1;

  // 顶部：应用名 + 日期
  ctx.fillStyle = '#8c8c8c';
  ctx.font = `600 24px ${FONT}`;
  ctx.textAlign = 'left';
  ctx.fillText('EnStudApp · AI 英语学习助手', 48, 64);
  ctx.textAlign = 'right';
  ctx.fillText(today, W - 48, 64);

  // 头像圈（首字母）+ 昵称
  ctx.textAlign = 'center';
  ctx.fillStyle = '#1677ff';
  ctx.beginPath(); ctx.arc(W / 2, 170, 52, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#fff';
  ctx.font = `bold 48px ${FONT}`;
  ctx.textBaseline = 'middle';
  ctx.fillText((nickname as string).charAt(0).toUpperCase(), W / 2, 174);
  ctx.fillStyle = '#262626';
  ctx.font = `bold 36px ${FONT}`;
  ctx.fillText(nickname, W / 2, 262);

  // 白色主面板
  const panelY = 310;
  const panelH = data.variant === 'vocab' ? 560 : 460;
  roundRect(ctx, 48, panelY, W - 96, panelH, 24);
  ctx.fillStyle = '#ffffff';
  ctx.shadowColor = 'rgba(22, 119, 255, 0.12)';
  ctx.shadowBlur = 24;
  ctx.fill();
  ctx.shadowBlur = 0;

  if (data.variant === 'vocab' && data.vocabResult) {
    const r = data.vocabResult;
    ctx.fillStyle = '#8c8c8c';
    ctx.font = `400 28px ${FONT}`;
    ctx.fillText('我的估算词汇量', W / 2, panelY + 70);
    ctx.fillStyle = '#1677ff';
    ctx.font = `bold 130px ${FONT}`;
    ctx.fillText(String(r.estimatedSize), W / 2, panelY + 190);
    // 水平标签胶囊
    const label = r.levelLabel;
    ctx.font = `600 30px ${FONT}`;
    const labelW = ctx.measureText(label).width + 64;
    roundRect(ctx, W / 2 - labelW / 2, panelY + 250, labelW, 56, 28);
    ctx.fillStyle = '#1677ff';
    ctx.fill();
    ctx.fillStyle = '#fff';
    ctx.fillText(label, W / 2, panelY + 279);
    // 频段条形图
    const barX = 110;
    const barW = W - 220;
    let y = panelY + 350;
    ctx.font = `500 24px ${FONT}`;
    for (const b of r.bands) {
      ctx.textAlign = 'right';
      ctx.fillStyle = '#595959';
      ctx.fillText(b.label, barX - 20, y);
      ctx.textAlign = 'left';
      roundRect(ctx, barX, y - 16, barW, 22, 11);
      ctx.fillStyle = '#f0f0f0';
      ctx.fill();
      if (b.ratio > 0.01) {
        roundRect(ctx, barX, y - 16, Math.max(24, barW * b.ratio), 22, 11);
        ctx.fillStyle = '#1677ff';
        ctx.fill();
      }
      y += 44;
    }
  } else {
    // 学习成就卡：连续天数大数字 + 三项累计
    ctx.fillStyle = '#8c8c8c';
    ctx.font = `400 28px ${FONT}`;
    ctx.fillText('我已连续学习', W / 2, panelY + 70);
    ctx.fillStyle = '#faad14';
    ctx.font = `bold 130px ${FONT}`;
    ctx.fillText(`${data.journey?.streakDays ?? data.stats.streakDays} 天`, W / 2, panelY + 190);
    const items = [
      { label: '累计单词', value: data.stats.totalLearnedWords },
      { label: '阅读文章', value: data.stats.totalReadArticles },
      { label: 'AI 对话', value: data.stats.totalChats },
    ];
    const colW = (W - 96) / 3;
    items.forEach((item, i) => {
      const cx = 48 + colW * i + colW / 2;
      ctx.fillStyle = '#262626';
      ctx.font = `bold 52px ${FONT}`;
      ctx.fillText(String(item.value), cx, panelY + 310);
      ctx.fillStyle = '#8c8c8c';
      ctx.font = `400 26px ${FONT}`;
      ctx.fillText(item.label, cx, panelY + 360);
    });
  }

  // 底部口号
  ctx.textAlign = 'center';
  ctx.fillStyle = '#595959';
  ctx.font = `600 30px ${FONT}`;
  ctx.fillText('和我一起用 AI 学英语', W / 2, panelY + panelH + 90);
  ctx.fillStyle = '#bfbfbf';
  ctx.font = `400 24px ${FONT}`;
  ctx.fillText('EnStudApp · 阅读 · 单词 · AI 对话 · 写作', W / 2, panelY + panelH + 140);
}

/** 圆角矩形路径 */
function roundRect(ctx: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, r: number) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}
