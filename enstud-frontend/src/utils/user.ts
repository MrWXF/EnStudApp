/**
 * 用户信息工具
 */

export interface UserInfo {
  userId: number;
  username: string;
  nickname?: string;
}

/**
 * 从 localStorage 获取当前用户信息
 */
export function getCurrentUser(): UserInfo | null {
  const raw = localStorage.getItem('user');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

/**
 * 获取当前用户 ID
 */
export function getCurrentUserId(): number | null {
  return getCurrentUser()?.userId ?? null;
}

/**
 * 获取当前用户显示名称（优先昵称）
 */
export function getCurrentUserName(): string {
  const user = getCurrentUser();
  if (!user) return '用户';
  return user.nickname || user.username;
}

/**
 * 获取用户头像首字母
 */
export function getUserAvatar(name?: string | null): string {
  if (!name) return 'U';
  return name.charAt(0).toUpperCase();
}
