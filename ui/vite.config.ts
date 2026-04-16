import { fileURLToPath, URL } from 'node:url'

import { defineConfig, type Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

function inlineBuildAssets(): Plugin {
  return {
    name: 'inline-build-assets',
    apply: 'build',
    enforce: 'post',
    generateBundle(_, bundle) {
      const indexHtml = bundle['index.html']

      if (!indexHtml || indexHtml.type !== 'asset' || typeof indexHtml.source !== 'string') {
        return
      }

      const readBundleText = (path: string): string | null => {
        const fileName = path.replace(/^\.\//, '')
        const file = bundle[fileName]

        if (!file) {
          return null
        }

        delete bundle[fileName]

        if (file.type === 'asset') {
          return typeof file.source === 'string'
            ? file.source
            : Buffer.from(file.source).toString('utf8')
        }

        return file.code
      }

      indexHtml.source = indexHtml.source
        .replace(/<link[^>]*rel="stylesheet"[^>]*href="([^"]+)"[^>]*>/g, (match: string, href: string) => {
          const css = readBundleText(href)

          return css === null
            ? match
            : `<style>${css.replaceAll('</style>', '<\\/style>')}</style>`
        })
        .replace(/<script[^>]*src="([^"]+)"[^>]*><\/script>/g, (match: string, src: string) => {
          const js = readBundleText(src)

          return js === null
            ? match
            : `<script type="module">${js.replaceAll('</script>', '<\\/script>')}</script>`
        })
    },
  }
}

// https://vite.dev/config/
export default defineConfig({
  base: './',
  build: {
    assetsInlineLimit: Number.MAX_SAFE_INTEGER,
    cssCodeSplit: false,
    modulePreload: false,
    outDir: process.env.LIFESTOLEN_VITE_OUT_DIR ?? 'dist',
    emptyOutDir: true,
  },
  plugins: [
    vue(),
    vueDevTools(),
    inlineBuildAssets(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
