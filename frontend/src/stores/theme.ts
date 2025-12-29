import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'system'

export const useThemeStore = defineStore(
  'theme',
  () => {
    const mode = ref<ThemeMode>('system')
    const isDark = ref(false)

    // 获取系统主题偏好
    function getSystemTheme(): boolean {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }

    // 应用主题
    function applyTheme(dark: boolean) {
      isDark.value = dark
      const html = document.documentElement
      if (dark) {
        html.classList.add('dark')
        html.setAttribute('data-theme', 'dark')
      } else {
        html.classList.remove('dark')
        html.setAttribute('data-theme', 'light')
      }
    }

    // 设置主题模式
    function setMode(newMode: ThemeMode) {
      mode.value = newMode
      if (newMode === 'system') {
        applyTheme(getSystemTheme())
      } else {
        applyTheme(newMode === 'dark')
      }
    }

    // 切换主题
    function toggleTheme() {
      if (mode.value === 'light') {
        setMode('dark')
      } else if (mode.value === 'dark') {
        setMode('system')
      } else {
        setMode('light')
      }
    }

    // 初始化
    function init() {
      // 监听系统主题变化
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      mediaQuery.addEventListener('change', (e) => {
        if (mode.value === 'system') {
          applyTheme(e.matches)
        }
      })

      // 应用保存的主题
      if (mode.value === 'system') {
        applyTheme(getSystemTheme())
      } else {
        applyTheme(mode.value === 'dark')
      }
    }

    return {
      mode,
      isDark,
      setMode,
      toggleTheme,
      init,
    }
  },
  {
    persist: {
      key: 'interview-kb-theme',
      storage: localStorage,
      pick: ['mode'],
    },
  }
)
