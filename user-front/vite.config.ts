import { defineConfig } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';
import path from 'path';
import type { Plugin } from 'vite';
import fs from 'fs';

// Inject requiredPrivateInfos into app.json for WeChat mini program
function injectPrivateInfos(): Plugin {
  return {
    name: 'inject-private-infos',
    closeBundle() {
      const appJsonPath = path.resolve(__dirname, 'dist/dev/mp-weixin/app.json');
      if (fs.existsSync(appJsonPath)) {
        const appJson = JSON.parse(fs.readFileSync(appJsonPath, 'utf8'));
        appJson.requiredPrivateInfos = ['chooseLocation', 'getLocation'];
        fs.writeFileSync(appJsonPath, JSON.stringify(appJson, null, 2), 'utf8');
      }
    },
  };
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni(), injectPrivateInfos()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `
          @import "@/styles/variables.scss";
          @import "@/styles/mixins.scss";
        `,
      },
    },
  },
});
