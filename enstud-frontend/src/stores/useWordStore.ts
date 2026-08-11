import { create } from 'zustand';

interface LearningProgress {
  todayLearned: number;
  totalLearned: number;
  todayReviewed: number;
}

interface WordState {
  progress: LearningProgress;
  currentWords: string[]; // 当前正在学习的单词
  isLearning: boolean;

  updateProgress: (progress: Partial<LearningProgress>) => void;
  setCurrentWords: (words: string[]) => void;
  setLearning: (isLearning: boolean) => void;
  reset: () => void;
}

export const useWordStore = create<WordState>((set) => ({
  progress: {
    todayLearned: 0,
    totalLearned: 0,
    todayReviewed: 0,
  },
  currentWords: [],
  isLearning: false,

  updateProgress: (progress) =>
    set((state) => ({
      progress: { ...state.progress, ...progress },
    })),

  setCurrentWords: (words) => set({ currentWords: words }),

  setLearning: (isLearning) => set({ isLearning }),

  reset: () =>
    set({
      progress: { todayLearned: 0, totalLearned: 0, todayReviewed: 0 },
      currentWords: [],
      isLearning: false,
    }),
}));
