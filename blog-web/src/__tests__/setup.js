import { config } from '@vue/test-utils'

// Mock Element Plus icons globally (they're ES module SVG components)
config.global.stubs = {
  'el-icon': { template: '<i :class="$attrs.class" />' },
  'el-button': { template: '<button :class="$attrs.class"><slot /></button>' },
  'el-input': { template: '<input />' },
  'el-tag': { template: '<span><slot /></span>' },
  'el-empty': { template: '<div />' },
  'el-avatar': { template: '<div />' },
  'el-message-box': { template: '<div />' },
  'el-popconfirm': { template: '<div />' },
}

// Mock localStorage
const store = {}
global.localStorage = {
  getItem: (key) => store[key] ?? null,
  setItem: (key, value) => { store[key] = String(value) },
  removeItem: (key) => { delete store[key] },
  clear: () => { for (const k of Object.keys(store)) delete store[k] },
  get length() { return Object.keys(store).length },
  key: (i) => Object.keys(store)[i] ?? null
}
