import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import VMdEditor from '@kangc/v-md-editor'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js'
import '@kangc/v-md-editor/lib/theme/style/github.css'
import VMdPreview from '@kangc/v-md-editor/lib/preview'
import 'highlight.js/styles/github.css'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import js from 'highlight.js/lib/languages/javascript'
import yaml from 'highlight.js/lib/languages/yaml'
import sql from 'highlight.js/lib/languages/sql'
import bash from 'highlight.js/lib/languages/bash'
import json from 'highlight.js/lib/languages/json'
import xml from 'highlight.js/lib/languages/xml'

hljs.registerLanguage('java', java)
hljs.registerLanguage('javascript', js)
hljs.registerLanguage('js', js)
hljs.registerLanguage('yaml', yaml)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('json', json)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('html', xml)

import App from './App.vue'
import router from './router'
import fadeIn from './directives/fadeIn'
import codeCopy from './directives/codeCopy'
import lazyImage from './directives/lazyImage'

VMdEditor.use(githubTheme, { Hljs: hljs })
VMdPreview.use(githubTheme, { Hljs: hljs })

// code theme stylesheet (created dynamically for theme switching)
const codeThemeLink = document.createElement('link')
codeThemeLink.rel = 'stylesheet'
codeThemeLink.id = 'code-theme-link'
codeThemeLink.href = ''
document.head.appendChild(codeThemeLink)

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.use(VMdEditor)
app.use(VMdPreview)
app.directive('fade-in', fadeIn)
app.directive('code-copy', codeCopy)
app.directive('lazy-img', lazyImage)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.mount('#app')
