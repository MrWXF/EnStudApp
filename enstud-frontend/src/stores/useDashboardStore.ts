import { create } from 'zustand';

interface DashboardStats {
  wordsToday: number;
  wordsTotal: number;
  articlesRead: number;
  chatCount: number;
  writingCount: number;
  streak: number;
}

interface AiUsage {
  chat: { usedToday: number; dailyLimit: number };
  writing: { usedToday: number; dailyLimit: number };
  translate: { usedToday: number; dailyLimit: number };
}

interface DashboardState {
  stats: DashboardStats;
  aiUsage: AiUsage | null;
  isLoading: boolean;

  setStats: (stats: Partial<DashboardStats>) => void;
  setAiUsage: (usage: AiUsage) => void;
  setLoading: (loading: boolean) => void;
  reset: () => void;
}

const defaultStats: DashboardStats = {
  wordsToday: 0,
  wordsTotal: 0,
  articlesRead: 0,
  chatCount: 0,
  writingCount: 0,
  streak: 0,
};

export const useDashboardStore = create<DashboardState>((set) => ({
  stats: defaultStats,
  aiUsage: null,
  isLoading: false,

  setStats: (stats) =>
    set((state) => ({
      stats: { ...state.stats, ...stats },
    })),

  setAiUsage: (usage) => set({ aiUsage: usage }),

  setLoading: (loading) => set({ isLoading: loading }),

  reset: () => set({ stats: defaultStats, aiUsage: null, isLoading: false }),
}));
