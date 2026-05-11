<template>
  <div class="login-page">
    <!-- Left Panel: Animated Characters -->
    <div class="left-panel" id="left-panel">
      <div class="characters-wrapper">
        <div class="characters-scene">
          <!-- Purple character -->
          <div class="character char-purple" id="char-purple">
            <div class="eyes" id="purple-eyes" style="left:45px;top:40px;gap:28px;">
              <div class="eyeball" id="purple-eye-l" style="width:18px;height:18px;">
                <div class="pupil" id="purple-pupil-l" style="width:7px;height:7px;"></div>
              </div>
              <div class="eyeball" id="purple-eye-r" style="width:18px;height:18px;">
                <div class="pupil" id="purple-pupil-r" style="width:7px;height:7px;"></div>
              </div>
            </div>
          </div>
          <!-- Black character -->
          <div class="character char-black" id="char-black">
            <div class="eyes" id="black-eyes" style="left:26px;top:32px;gap:20px;">
              <div class="eyeball" id="black-eye-l" style="width:16px;height:16px;">
                <div class="pupil" id="black-pupil-l" style="width:6px;height:6px;"></div>
              </div>
              <div class="eyeball" id="black-eye-r" style="width:16px;height:16px;">
                <div class="pupil" id="black-pupil-r" style="width:6px;height:6px;"></div>
              </div>
            </div>
          </div>
          <!-- Orange character -->
          <div class="character char-orange" id="char-orange">
            <div class="eyes" id="orange-eyes" style="left:82px;top:90px;gap:28px;">
              <div class="bare-pupil" id="orange-pupil-l" style="width:12px;height:12px;"></div>
              <div class="bare-pupil" id="orange-pupil-r" style="width:12px;height:12px;"></div>
            </div>
          </div>
          <!-- Yellow character -->
          <div class="character char-yellow" id="char-yellow">
            <div class="eyes" id="yellow-eyes" style="left:52px;top:40px;gap:20px;">
              <div class="bare-pupil" id="yellow-pupil-l" style="width:12px;height:12px;"></div>
              <div class="bare-pupil" id="yellow-pupil-r" style="width:12px;height:12px;"></div>
            </div>
            <div class="yellow-mouth" id="yellow-mouth" style="left:40px;top:88px;"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right Panel: Login Form -->
    <div class="right-panel">
      <div class="form-container">
        <h1 class="form-title">管理员登录</h1>
        <p class="form-subtitle">登录以管理博客内容</p>

        <form @submit.prevent="handleSubmit" @keyup.enter="handleSubmit">
          <div class="form-group">
            <label id="user-label" for="username">用户名</label>
            <div class="input-wrapper">
              <input
                id="username"
                v-model="username"
                type="text"
                placeholder="请输入用户名"
                autocomplete="off"
                @focus="setTyping(true)"
                @blur="setTyping(false)"
                @input="updateCharacters"
              />
            </div>
          </div>

          <div class="form-group">
            <label id="pwd-label" for="password">密码</label>
            <div class="input-wrapper">
              <input
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••"
                @focus="onPwdFocus"
                @blur="onPwdBlur"
                @input="updateCharacters"
              />
              <button type="button" class="toggle-pwd" @click="togglePassword">
                <svg v-if="!showPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              </button>
            </div>
          </div>

          <div v-if="errorMsg" class="error-msg" id="error-msg">{{ errorMsg }}</div>

          <button type="submit" class="btn-login" :disabled="loading">
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </button>
        </form>

        <div class="back-link-wrap">
          <router-link to="/" class="back-link">← 返回首页</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { login as loginApi } from '@/api/auth'
import { useUserStore } from '@/store/user'
import { useSEO } from '@/composables/useSEO'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')
const showPassword = ref(false)

// ============ MOUSE TRACKING ============
let mouseX = 0
let mouseY = 0
let isTyping = false
let isLoginError = false
let isPurpleBlinking = false
let isBlackBlinking = false
let isPasswordFocused = false
let isShowingPwd = false
let rafId = null
let typingTimer = null
let errorRecoverTimer = null
const shakeIds = ['purple-eyes','black-eyes','orange-eyes','yellow-eyes','yellow-mouth']

function calcPosition(el) {
  if (!el) return { faceX: 0, faceY: 0, bodySkew: 0 }
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 3
  const dx = mouseX - cx
  const dy = mouseY - cy
  return {
    faceX: Math.max(-15, Math.min(15, dx / 20)),
    faceY: Math.max(-10, Math.min(10, dy / 30)),
    bodySkew: Math.max(-6, Math.min(6, -dx / 120))
  }
}

function calcPupilOffset(el, maxDist) {
  if (!el) return { x: 0, y: 0 }
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2
  const dx = mouseX - cx
  const dy = mouseY - cy
  const dist = Math.min(Math.sqrt(dx * dx + dy * dy), maxDist)
  const angle = Math.atan2(dy, dx)
  return { x: Math.cos(angle) * dist, y: Math.sin(angle) * dist }
}

function getEl(id) { return document.getElementById(id) }

function updateCharacters() {
  if (isLoginError) return

  const purple = getEl('char-purple')
  const black = getEl('char-black')
  const orange = getEl('char-orange')
  const yellow = getEl('char-yellow')

  const purplePos = calcPosition(purple)
  const blackPos = calcPosition(black)
  const orangePos = calcPosition(orange)
  const yellowPos = calcPosition(yellow)

  const isLookingAway = isPasswordFocused && !showPassword.value

  // Purple
  if (isShowingPwd) {
    if (purple) { purple.style.transform = 'skewX(0deg)'; purple.style.height = '370px' }
  } else if (isLookingAway) {
    if (purple) { purple.style.transform = 'skewX(-14deg) translateX(-20px)'; purple.style.height = '410px' }
  } else if (isTyping) {
    if (purple) { purple.style.transform = `skewX(${(purplePos.bodySkew || 0) - 12}deg) translateX(40px)`; purple.style.height = '410px' }
  } else {
    if (purple) { purple.style.transform = `skewX(${purplePos.bodySkew}deg)`; purple.style.height = '370px' }
  }

  // Purple eyes
  const purpleEyeL = getEl('purple-eye-l')
  const purpleEyeR = getEl('purple-eye-r')
  const purpleEyes = getEl('purple-eyes')
  if (purpleEyeL) purpleEyeL.style.height = isPurpleBlinking ? '2px' : '18px'
  if (purpleEyeR) purpleEyeR.style.height = isPurpleBlinking ? '2px' : '18px'

  if (isLookingAway) {
    if (purpleEyes) { purpleEyes.style.left = '20px'; purpleEyes.style.top = '25px' }
    const po = { x: -5, y: -5 }
    const ppl = getEl('purple-pupil-l')
    const ppr = getEl('purple-pupil-r')
    if (ppl) ppl.style.transform = `translate(${po.x}px, ${po.y}px)`
    if (ppr) ppr.style.transform = `translate(${po.x}px, ${po.y}px)`
  } else if (isShowingPwd) {
    if (purpleEyes) { purpleEyes.style.left = '20px'; purpleEyes.style.top = '35px' }
    const po = { x: -4, y: -4 }
    const ppl = getEl('purple-pupil-l')
    const ppr = getEl('purple-pupil-r')
    if (ppl) ppl.style.transform = `translate(${po.x}px, ${po.y}px)`
    if (ppr) ppr.style.transform = `translate(${po.x}px, ${po.y}px)`
  } else {
    if (purpleEyes) { purpleEyes.style.left = (45 + purplePos.faceX) + 'px'; purpleEyes.style.top = (40 + purplePos.faceY) + 'px' }
    const po = calcPupilOffset(purpleEyeL, 5)
    const ppl = getEl('purple-pupil-l')
    const ppr = getEl('purple-pupil-r')
    if (ppl) ppl.style.transform = `translate(${po.x}px, ${po.y}px)`
    if (ppr) ppr.style.transform = `translate(${po.x}px, ${po.y}px)`
  }

  // Black
  if (isShowingPwd) {
    if (black) black.style.transform = 'skewX(0deg)'
  } else if (isLookingAway) {
    if (black) black.style.transform = 'skewX(12deg) translateX(-10px)'
  } else if (isTyping) {
    if (black) black.style.transform = `skewX(${(blackPos.bodySkew || 0) * 1.5}deg)`
  } else {
    if (black) black.style.transform = `skewX(${blackPos.bodySkew}deg)`
  }

  const blackEyeL = getEl('black-eye-l')
  const blackEyeR = getEl('black-eye-r')
  const blackEyes = getEl('black-eyes')
  if (blackEyeL) blackEyeL.style.height = isBlackBlinking ? '2px' : '16px'
  if (blackEyeR) blackEyeR.style.height = isBlackBlinking ? '2px' : '16px'

  if (isLookingAway) {
    if (blackEyes) { blackEyes.style.left = '10px'; blackEyes.style.top = '20px' }
    const bo = { x: -4, y: -5 }
    const bpl = getEl('black-pupil-l')
    const bpr = getEl('black-pupil-r')
    if (bpl) bpl.style.transform = `translate(${bo.x}px, ${bo.y}px)`
    if (bpr) bpr.style.transform = `translate(${bo.x}px, ${bo.y}px)`
  } else if (isShowingPwd) {
    if (blackEyes) { blackEyes.style.left = '10px'; blackEyes.style.top = '28px' }
    const bo = { x: -4, y: -4 }
    const bpl = getEl('black-pupil-l')
    const bpr = getEl('black-pupil-r')
    if (bpl) bpl.style.transform = `translate(${bo.x}px, ${bo.y}px)`
    if (bpr) bpr.style.transform = `translate(${bo.x}px, ${bo.y}px)`
  } else {
    if (blackEyes) { blackEyes.style.left = (26 + blackPos.faceX) + 'px'; blackEyes.style.top = (32 + blackPos.faceY) + 'px' }
    const bo = calcPupilOffset(blackEyeL, 4)
    const bpl = getEl('black-pupil-l')
    const bpr = getEl('black-pupil-r')
    if (bpl) bpl.style.transform = `translate(${bo.x}px, ${bo.y}px)`
    if (bpr) bpr.style.transform = `translate(${bo.x}px, ${bo.y}px)`
  }

  // Orange
  if (isShowingPwd) {
    if (orange) orange.style.transform = 'skewX(0deg)'
  } else {
    if (orange) orange.style.transform = `skewX(${orangePos.bodySkew}deg)`
  }

  const orangeEyes = getEl('orange-eyes')
  if (isLookingAway) {
    if (orangeEyes) { orangeEyes.style.left = '50px'; orangeEyes.style.top = '75px' }
    const oo = { x: -5, y: -5 }
    const opl = getEl('orange-pupil-l')
    const opr = getEl('orange-pupil-r')
    if (opl) opl.style.transform = `translate(${oo.x}px, ${oo.y}px)`
    if (opr) opr.style.transform = `translate(${oo.x}px, ${oo.y}px)`
  } else if (isShowingPwd) {
    if (orangeEyes) { orangeEyes.style.left = '50px'; orangeEyes.style.top = '85px' }
    const oo = { x: -5, y: -4 }
    const opl = getEl('orange-pupil-l')
    const opr = getEl('orange-pupil-r')
    if (opl) opl.style.transform = `translate(${oo.x}px, ${oo.y}px)`
    if (opr) opr.style.transform = `translate(${oo.x}px, ${oo.y}px)`
  } else {
    if (orangeEyes) { orangeEyes.style.left = (82 + orangePos.faceX) + 'px'; orangeEyes.style.top = (90 + orangePos.faceY) + 'px' }
    const oo = calcPupilOffset(getEl('orange-pupil-l'), 5)
    const opl = getEl('orange-pupil-l')
    const opr = getEl('orange-pupil-r')
    if (opl) opl.style.transform = `translate(${oo.x}px, ${oo.y}px)`
    if (opr) opr.style.transform = `translate(${oo.x}px, ${oo.y}px)`
  }

  // Yellow
  if (isShowingPwd) {
    if (yellow) yellow.style.transform = 'skewX(0deg)'
  } else {
    if (yellow) yellow.style.transform = `skewX(${yellowPos.bodySkew}deg)`
  }

  const yellowEyes = getEl('yellow-eyes')
  const yellowMouth = getEl('yellow-mouth')
  if (isLookingAway) {
    if (yellowEyes) { yellowEyes.style.left = '20px'; yellowEyes.style.top = '30px' }
    const yo = { x: -5, y: -5 }
    const ypl = getEl('yellow-pupil-l')
    const ypr = getEl('yellow-pupil-r')
    if (ypl) ypl.style.transform = `translate(${yo.x}px, ${yo.y}px)`
    if (ypr) ypr.style.transform = `translate(${yo.x}px, ${yo.y}px)`
    if (yellowMouth) { yellowMouth.style.left = '15px'; yellowMouth.style.top = '78px'; yellowMouth.style.transform = 'rotate(0deg)' }
  } else if (isShowingPwd) {
    if (yellowEyes) { yellowEyes.style.left = '20px'; yellowEyes.style.top = '35px' }
    const yo = { x: -5, y: -4 }
    const ypl = getEl('yellow-pupil-l')
    const ypr = getEl('yellow-pupil-r')
    if (ypl) ypl.style.transform = `translate(${yo.x}px, ${yo.y}px)`
    if (ypr) ypr.style.transform = `translate(${yo.x}px, ${yo.y}px)`
    if (yellowMouth) { yellowMouth.style.left = '10px'; yellowMouth.style.top = '88px'; yellowMouth.style.transform = 'rotate(0deg)' }
  } else {
    if (yellowEyes) { yellowEyes.style.left = (52 + yellowPos.faceX) + 'px'; yellowEyes.style.top = (40 + yellowPos.faceY) + 'px' }
    const yo = calcPupilOffset(getEl('yellow-pupil-l'), 5)
    const ypl = getEl('yellow-pupil-l')
    const ypr = getEl('yellow-pupil-r')
    if (ypl) ypl.style.transform = `translate(${yo.x}px, ${yo.y}px)`
    if (ypr) ypr.style.transform = `translate(${yo.x}px, ${yo.y}px)`
    if (yellowMouth) { yellowMouth.style.left = (40 + yellowPos.faceX) + 'px'; yellowMouth.style.top = (88 + yellowPos.faceY) + 'px'; yellowMouth.style.transform = 'rotate(0deg)' }
  }
}

// Blinking
let blinkPurpleTimer = null
let blinkBlackTimer = null

function scheduleBlinkPurple() {
  blinkPurpleTimer = setTimeout(() => {
    isPurpleBlinking = true
    updateCharacters()
    setTimeout(() => {
      isPurpleBlinking = false
      updateCharacters()
      scheduleBlinkPurple()
    }, 150)
  }, Math.random() * 4000 + 3000)
}

function scheduleBlinkBlack() {
  blinkBlackTimer = setTimeout(() => {
    isBlackBlinking = true
    updateCharacters()
    setTimeout(() => {
      isBlackBlinking = false
      updateCharacters()
      scheduleBlinkBlack()
    }, 150)
  }, Math.random() * 4000 + 3000)
}

// ============ EVENT HANDLERS ============
function setTyping(typing) {
  isTyping = typing
  if (typing) {
    clearTimeout(typingTimer)
    typingTimer = setTimeout(() => { isTyping = false; updateCharacters() }, 800)
  } else {
    isTyping = false
  }
  updateCharacters()
}

function onPwdFocus() {
  isPasswordFocused = true
  updateCharacters()
}

function onPwdBlur() {
  isPasswordFocused = false
  updateCharacters()
}

function togglePassword() {
  showPassword.value = !showPassword.value
  isShowingPwd = password.value.length > 0 && showPassword.value
  updateCharacters()
}

// Error shake animation
function triggerLoginError() {
  if (errorRecoverTimer) { clearTimeout(errorRecoverTimer); errorRecoverTimer = null }
  const shakeEls = shakeIds.map(id => getEl(id)).filter(Boolean)
  shakeEls.forEach(el => el.classList.remove('shake-head'))
  void document.body.offsetHeight

  isLoginError = true
  updateCharacters()

  setTimeout(() => { shakeEls.forEach(el => el.classList.add('shake-head')) }, 350)

  errorRecoverTimer = setTimeout(() => {
    isLoginError = false
    shakeEls.forEach(el => el.classList.remove('shake-head'))
    updateCharacters()
  }, 2500)
}

// Error state rendering (sad look)
function renderErrorState() {
  // Purple
  const purpleEyes = getEl('purple-eyes')
  if (purpleEyes) { purpleEyes.style.left = '30px'; purpleEyes.style.top = '55px' }
  ;['purple-pupil-l','purple-pupil-r'].forEach(id => {
    const el = getEl(id); if (el) el.style.transform = 'translate(-3px, 4px)'
  })
  // Black
  const blackEyes = getEl('black-eyes')
  if (blackEyes) { blackEyes.style.left = '15px'; blackEyes.style.top = '40px' }
  ;['black-pupil-l','black-pupil-r'].forEach(id => {
    const el = getEl(id); if (el) el.style.transform = 'translate(-3px, 4px)'
  })
  // Orange
  const orangeEyes = getEl('orange-eyes')
  if (orangeEyes) { orangeEyes.style.left = '60px'; orangeEyes.style.top = '95px' }
  ;['orange-pupil-l','orange-pupil-r'].forEach(id => {
    const el = getEl(id); if (el) el.style.transform = 'translate(-3px, 4px)'
  })
  // Yellow
  const yellowEyes = getEl('yellow-eyes')
  if (yellowEyes) { yellowEyes.style.left = '35px'; yellowEyes.style.top = '45px' }
  ;['yellow-pupil-l','yellow-pupil-r'].forEach(id => {
    const el = getEl(id); if (el) el.style.transform = 'translate(-3px, 4px)'
  })
  const ym = getEl('yellow-mouth')
  if (ym) { ym.style.left = '30px'; ym.style.top = '92px'; ym.style.transform = 'rotate(-8deg)' }
}

const onMouseMove = (e) => {
  mouseX = e.clientX
  mouseY = e.clientY
  if (!isTyping && !isLoginError) updateCharacters()
}

// ============ LOGIN ============
const handleSubmit = async () => {
  errorMsg.value = ''

  if (!username.value.trim()) {
    errorMsg.value = '请输入用户名'
    renderErrorState()
    triggerLoginError()
    return
  }
  if (!password.value) {
    errorMsg.value = '请输入密码'
    renderErrorState()
    triggerLoginError()
    return
  }

  loading.value = true
  try {
    const res = await loginApi(username.value.trim(), password.value)
    userStore.login(res.data.token)
    ElMessage.success('登录成功')
    router.push('/admin')
  } catch {
    errorMsg.value = '用户名或密码错误'
    renderErrorState()
    triggerLoginError()
  } finally {
    loading.value = false
  }
}

// ============ LIFECYCLE ============
onMounted(() => {
  useSEO({ title: '管理员登录', description: '登录以管理博客内容' })
  document.addEventListener('mousemove', onMouseMove)
  updateCharacters()
  scheduleBlinkPurple()
  scheduleBlinkBlack()
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  clearTimeout(typingTimer)
  clearTimeout(errorRecoverTimer)
  clearTimeout(blinkPurpleTimer)
  clearTimeout(blinkBlackTimer)
  cancelAnimationFrame(rafId)
})
</script>

<style scoped>
.login-page {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: calc(100vh - 56px);
}

/* ===== LEFT PANEL ===== */
.left-panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #d4d0dc 0%, #c8c4d0 50%, #bbb7c5 100%);
  overflow: hidden;
}
.left-panel::after {
  content: '';
  position: absolute;
  top: 20%;
  right: 15%;
  width: 260px;
  height: 260px;
  background: rgba(180,170,200,0.25);
  border-radius: 50%;
  filter: blur(80px);
}
.left-panel::before {
  content: '';
  position: absolute;
  bottom: 15%;
  left: 10%;
  width: 350px;
  height: 350px;
  background: rgba(200,195,210,0.2);
  border-radius: 50%;
  filter: blur(100px);
}

.characters-wrapper {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 420px;
}

.characters-scene {
  position: relative;
  width: 480px;
  height: 360px;
}

/* Characters */
.character {
  position: absolute;
  bottom: 0;
  transition: all 0.7s ease-in-out;
  transform-origin: bottom center;
}

.char-purple {
  left: 60px;
  width: 170px;
  height: 370px;
  background: #6C3FF5;
  border-radius: 10px 10px 0 0;
  z-index: 1;
}

.char-black {
  left: 220px;
  width: 115px;
  height: 290px;
  background: #2D2D2D;
  border-radius: 8px 8px 0 0;
  z-index: 2;
}

.char-orange {
  left: 0;
  width: 230px;
  height: 190px;
  background: #FF9B6B;
  border-radius: 115px 115px 0 0;
  z-index: 3;
}

.char-yellow {
  left: 290px;
  width: 135px;
  height: 215px;
  background: #E8D754;
  border-radius: 68px 68px 0 0;
  z-index: 4;
}

/* Eyes */
.eyes {
  position: absolute;
  display: flex;
  transition: all 0.7s ease-in-out;
}

.eyeball {
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: height 0.15s ease;
  overflow: hidden;
}

.pupil {
  border-radius: 50%;
  background: #2D2D2D;
  transition: transform 0.1s ease-out;
}

.bare-pupil {
  border-radius: 50%;
  background: #2D2D2D;
  transition: transform 0.7s ease-in-out;
}

.yellow-mouth {
  position: absolute;
  width: 50px;
  height: 4px;
  background: #2D2D2D;
  border-radius: 2px;
  transition: all 0.7s ease-in-out;
}

/* Shake head animation */
@keyframes shakeHead {
  0%, 100% { translate: 0 0; }
  10%  { translate: -9px 0; }
  20%  { translate: 7px 0; }
  30%  { translate: -6px 0; }
  40%  { translate: 5px 0; }
  50%  { translate: -4px 0; }
  60%  { translate: 3px 0; }
  70%  { translate: -2px 0; }
  80%  { translate: 1px 0; }
  90%  { translate: -0.5px 0; }
}

.eyes.shake-head, .yellow-mouth.shake-head {
  animation: shakeHead 0.8s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

/* ===== RIGHT PANEL ===== */
.right-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  padding: 40px;
}

.form-container {
  width: 100%;
  max-width: 380px;
}

.form-title {
  text-align: center;
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.form-subtitle {
  text-align: center;
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 36px;
}

.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
}
.input-wrapper input {
  width: 100%;
  height: 48px;
  border: none;
  border-bottom: 1.5px solid var(--border);
  padding: 0 40px 0 0;
  font-size: 15px;
  color: var(--text-primary);
  background: transparent;
  outline: none;
  transition: border-color 0.3s;
}
.input-wrapper input:focus {
  border-bottom-color: var(--accent);
}
.input-wrapper input::placeholder {
  color: var(--text-muted);
}

.toggle-pwd {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-muted);
  padding: 6px;
  transition: color 0.2s;
}
.toggle-pwd:hover { color: var(--text-primary); }

.error-msg {
  padding: 10px 14px;
  font-size: 13px;
  color: #dc2626;
  background: rgba(220,38,38,0.08);
  border: 1px solid rgba(220,38,38,0.2);
  border-radius: 10px;
  margin-bottom: 16px;
}

.btn-login {
  width: 100%;
  height: 50px;
  border-radius: 25px;
  border: none;
  background: var(--accent);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.3s;
}
.btn-login:hover { opacity: 0.9; }
.btn-login:disabled { opacity: 0.6; cursor: default; }

.back-link-wrap {
  text-align: center;
  margin-top: 24px;
}
.back-link {
  font-size: 13px;
  color: var(--text-muted);
  text-decoration: none;
  transition: color 0.2s;
}
.back-link:hover { color: var(--accent); }

@media (max-width: 900px) {
  .login-page { grid-template-columns: 1fr; }
  .left-panel { display: none; }
}
</style>
