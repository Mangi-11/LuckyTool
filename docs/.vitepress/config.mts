import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  lang: 'zh-CN',
  title: 'LuckyTool',
  description: 'LuckyTool — 基于 LSPosed (libxposed) 的 ColorOS/OPPO 系统增强模块文档',
  cleanUrls: true,
  lastUpdated: true,
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: '指南', link: '/guide/', activeMatch: '/guide/' },
      { text: '开发者', link: '/dev/', activeMatch: '/dev/' }
    ],
    sidebar: {
      '/guide/': [
        {
          text: '用户指南',
          items: [
            { text: '简介', link: '/guide/' },
            { text: '安装与激活', link: '/guide/install' },
            { text: '功能列表', link: '/guide/features' },
            { text: '配置说明', link: '/guide/prefs' },
            { text: '常见问题', link: '/guide/faq' }
          ]
        }
      ],
      '/dev/': [
        {
          text: '开发者文档',
          items: [
            { text: '总览', link: '/dev/' },
            { text: '构建与发布', link: '/dev/build' },
            { text: 'Hook 架构', link: '/dev/architecture' },
            { text: '编写 Hook', link: '/dev/hooks' },
            { text: '远程偏好', link: '/dev/remote-prefs' },
            { text: '模块资源注入', link: '/dev/resources' },
            { text: '迁移记录', link: '/dev/migration' }
          ]
        }
      ]
    },
    outline: { level: [2, 3], label: '本页目录' },
    docFooter: { prev: '上一页', next: '下一页' },
    lastUpdated: { text: '最后更新' },
    darkModeSwitchLabel: '外观',
    sidebarMenuLabel: '菜单',
    returnToTopLabel: '返回顶部',
    editLink: {
      pattern: 'https://github.com/luckyzyx/LuckyTool/edit/main/docs/:path'
    },
    search: {
      provider: 'local'
    },
    footer: {
      message: '基于 <a href="https://vitepress.dev/">VitePress</a> 构建',
      copyright: 'Copyright © 2026 luckyzyx'
    }
  },
  markdown: {
    theme: {
      light: 'github-light',
      dark: 'github-dark'
    }
  }
})