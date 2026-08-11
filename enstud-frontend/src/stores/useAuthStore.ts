import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { login as apiLogin, register as apiRegister, getProfile } from '../api/auth';
import type { LoginRequest, RegisterRequest, UserProfile } from '../types';

interface AuthState {
  token: string | null;
  user: UserProfile | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  fetchProfile: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      isAuthenticated: false,
      isLoading: false,

      login: async (data: LoginRequest) => {
        set({ isLoading: true });
        try {
          const res = await apiLogin(data);
          if (res.code === 0 && res.data) {
            localStorage.setItem('token', res.data.accessToken);
            set({
              token: res.data.accessToken,
              isAuthenticated: true,
              isLoading: false,
            });
            // Fetch profile after login
            await get().fetchProfile();
          } else {
            set({ isLoading: false });
            throw new Error(res.msg || '登录失败');
          }
        } catch (e) {
          set({ isLoading: false });
          throw e;
        }
      },

      register: async (data: RegisterRequest) => {
        set({ isLoading: true });
        try {
          const res = await apiRegister(data);
          if (res.code === 0 && res.data) {
            localStorage.setItem('token', res.data.accessToken);
            set({
              token: res.data.accessToken,
              isAuthenticated: true,
              isLoading: false,
            });
          } else {
            set({ isLoading: false });
            throw new Error(res.msg || '注册失败');
          }
        } catch (e) {
          set({ isLoading: false });
          throw e;
        }
      },

      logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        set({ token: null, user: null, isAuthenticated: false });
      },

      fetchProfile: async () => {
        try {
          const res = await getProfile();
          if (res.code === 0 && res.data) {
            set({ user: res.data });
            localStorage.setItem('user', JSON.stringify(res.data));
          }
        } catch (e) {
          // Profile fetch failed but user may still be logged in
          console.warn('Failed to fetch profile', e);
        }
      },
    }),
    {
      name: 'enstud-auth',
      partialize: (state) => ({
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
