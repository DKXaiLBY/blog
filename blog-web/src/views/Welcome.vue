<template>
  <div class="welcome-page" id="welcome-page">
    <!-- Characters -->
    <div class="characters-stage">
      <!-- Left: Blue round character (guides to Blog card) -->
      <div class="character char-blue" id="char-blue">
        <div class="char-body blue-body">
          <div class="eyes" id="blue-eyes" style="left:22px;top:26px;gap:16px;">
            <div class="eyeball" id="blue-eye-l" style="width:20px;height:20px;">
              <div class="pupil" id="blue-pupil-l" style="width:8px;height:8px;"></div>
            </div>
            <div class="eyeball" id="blue-eye-r" style="width:20px;height:20px;">
              <div class="pupil" id="blue-pupil-r" style="width:8px;height:8px;"></div>
            </div>
          </div>
          <div class="mouth" id="blue-mouth" style="left:27px;top:56px;width:24px;height:10px;border-bottom:3px solid #1e1b4b;border-radius:0 0 12px 12px;"></div>
          <div class="blush" style="left:12px;top:46px;background:rgba(255,150,150,0.35);"></div>
          <div class="blush" style="left:64px;top:46px;background:rgba(255,150,150,0.35);"></div>
        </div>
        <div class="char-label">技术</div>
      </div>

      <!-- Right: Orange tall character (guides to About card) -->
      <div class="character char-orange" id="char-orange">
        <div class="char-body orange-body">
          <div class="eyes" id="orange-eyes" style="left:18px;top:28px;gap:14px;">
            <div class="eyeball" id="orange-eye-l" style="width:18px;height:18px;">
              <div class="pupil" id="orange-pupil-l" style="width:7px;height:7px;"></div>
            </div>
            <div class="eyeball" id="orange-eye-r" style="width:18px;height:18px;">
              <div class="pupil" id="orange-pupil-r" style="width:7px;height:7px;"></div>
            </div>
          </div>
          <div class="mouth" id="orange-mouth" style="left:23px;top:54px;width:18px;height:8px;border-bottom:2.5px solid #7c2d12;border-radius:0 0 9px 9px;"></div>
          <div class="blush" style="left:8px;top:44px;background:rgba(255,150,150,0.3);"></div>
          <div class="blush" style="left:52px;top:44px;background:rgba(255,150,150,0.3);"></div>
        </div>
        <div class="char-label">关于</div>
      </div>
    </div>

    <!-- Content -->
    <div class="welcome-content">
      <h1 class="welcome-title" v-fade-in>Welcome to DKX's Blog</h1>
      <p class="welcome-subtitle" v-fade-in="100">小怪物们会给你带路哦</p>

      <div class="entrance-cards">
        <div
          class="entrance-card card-blog"
          id="card-blog"
          v-fade-in="200"
          @click="$router.push('/blog')"
          @mouseenter="hoverCard = 'blog'"
          @mouseleave="hoverCard = null"
        >
          <div class="card-icon">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
              <line x1="8" y1="7" x2="16" y2="7"/>
              <line x1="8" y1="11" x2="14" y2="11"/>
            </svg>
          </div>
          <h2>技术博客</h2>
          <p>Java、Vue、Spring Boot 技术文章</p>
          <span class="card-arrow">进入 →</span>
        </div>

        <div
          class="entrance-card card-about"
          id="card-about"
          v-fade-in="400"
          @click="$router.push('/about')"
          @mouseenter="hoverCard = 'about'"
          @mouseleave="hoverCard = null"
        >
          <div class="card-icon">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="8" r="4"/>
              <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
            </svg>
          </div>
          <h2>关于博主</h2>
          <p>了解我的技术栈与联系方式</p>
          <span class="card-arrow">进入 →</span>
        </div>
      </div>
    </div>

    <p class="scroll-hint">👆 试试把鼠标移到卡片上</p>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const hoverCard = ref(null)

// ============ STATE ============
let mouseX = window.innerWidth / 2
let mouseY = window.innerHeight / 2
let prevMouseX = mouseX
let prevMouseY = mouseY
let mouseSpeed = 0

let isBlinking = false
let blinkTimer = null

// Eye contact state: null=normal, 'looking'=eyes meeting, 'shy'=looking away
let eyeContactState = null
let eyeContactTimer = null
let eyeContactStartTime = 0
const EYE_CONTACT_DURATION = 1000  // look at each other for 1s
const SHY_DURATION = 500           // shy away for 0.5s

// Excitement level per character (0-1), ramps up on card hover
let blueExcite = 0
let orangeExcite = 0
const targetBlueExcite = () => hoverCard.value === 'blog' ? 1 : 0
const targetOrangeExcite = () => hoverCard.value === 'about' ? 1 : 0

// ============ DOM HELPERS ============
function getEl(id) { return document.getElementById(id) }

function calcPupilOffset(el) {
  if (!el) return { x: 0, y: 0 }
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2
  const dx = mouseX - cx
  const dy = mouseY - cy
  const dist = Math.sqrt(dx * dx + dy * dy)
  const maxDist = 5
  const clampedDist = Math.min(dist, maxDist)
  const angle = Math.atan2(dy, dx)
  return { x: Math.cos(angle) * clampedDist, y: Math.sin(angle) * clampedDist * 0.6 }
}

// ============ EYE CONTACT ============
function scheduleEyeContact() {
  const delay = Math.random() * 5000 + 4000  // every 4-9 seconds
  eyeContactTimer = setTimeout(() => {
    eyeContactState = 'looking'
    eyeContactStartTime = Date.now()

    // After EYE_CONTACT_DURATION, switch to shy
    eyeContactTimer = setTimeout(() => {
      eyeContactState = 'shy'
      eyeContactStartTime = Date.now()

      // After SHY_DURATION, back to normal and schedule next
      eyeContactTimer = setTimeout(() => {
        eyeContactState = null
        scheduleEyeContact()
      }, SHY_DURATION)
    }, EYE_CONTACT_DURATION)
  }, delay)
}

// ============ BLINK ============
function scheduleBlink() {
  blinkTimer = setTimeout(() => {
    isBlinking = true
    setTimeout(() => {
      isBlinking = false
      scheduleBlink()
    }, 150)
  }, Math.random() * 3500 + 2500)
}

// ============ UPDATE ============
function updateCharacters() {
  // Update excitement levels (smooth ramp)
  const tBlue = targetBlueExcite()
  const tOrange = targetOrangeExcite()
  blueExcite += (tBlue - blueExcite) * 0.12
  orangeExcite += (tOrange - orangeExcite) * 0.12

  // Calculate mouse speed
  const dx = mouseX - prevMouseX
  const dy = mouseY - prevMouseY
  mouseSpeed = Math.sqrt(dx * dx + dy * dy)
  prevMouseX = mouseX
  prevMouseY = mouseY

  const isDizzy = mouseSpeed > 30  // rapid movement threshold

  // === BLUE CHARACTER ===
  const blue = getEl('char-blue')
  if (blue) {
    const tilt = Math.max(-4, Math.min(4, (mouseX - window.innerWidth / 2) / 100))
    blue.style.transform = `rotate(${tilt}deg)`
  }

  const blueEyeL = getEl('blue-eye-l')
  const blueEyeR = getEl('blue-eye-r')
  const blueMouth = getEl('blue-mouth')

  // Blue eyes - blink
  if (blueEyeL) blueEyeL.style.height = isBlinking ? '2px' : '20px'
  if (blueEyeR) blueEyeR.style.height = isBlinking ? '2px' : '20px'

  // Blue pupil position
  let bOffset
  if (isDizzy && mouseSpeed > 50) {
    // Dizzy: eyes spin in circles
    const t = Date.now() / 200
    bOffset = { x: Math.cos(t) * 4, y: Math.sin(t) * 4 }
  } else if (eyeContactState === 'looking') {
    // Looking at orange character (to the right)
    bOffset = { x: 4, y: 0 }
  } else if (eyeContactState === 'shy') {
    // Shy: look down-left away from orange
    bOffset = { x: -4, y: 3 }
  } else if (blueExcite > 0.1) {
    // Excited: look down toward the blog card below
    const po = calcPupilOffset(blueEyeL)
    bOffset = { x: po.x * 0.3, y: Math.max(po.y, 2) }  // bias downward
  } else {
    bOffset = calcPupilOffset(blueEyeL)
  }

  ;['blue-pupil-l', 'blue-pupil-r'].forEach(id => {
    const el = getEl(id)
    if (el) el.style.transform = `translate(${bOffset.x}px, ${bOffset.y}px)`
  })

  // Blue mouth
  if (blueMouth) {
    if (blueExcite > 0.5) {
      // Big O mouth when excited
      blueMouth.style.width = '16px'
      blueMouth.style.height = '16px'
      blueMouth.style.borderRadius = '50%'
      blueMouth.style.border = '3px solid #1e1b4b'
      blueMouth.style.borderBottom = '3px solid #1e1b4b'
      blueMouth.style.background = 'transparent'
    } else if (isDizzy && mouseSpeed > 50) {
      // Wavy mouth when dizzy
      blueMouth.style.width = '28px'
      blueMouth.style.height = '6px'
      blueMouth.style.borderRadius = '0'
      blueMouth.style.border = 'none'
      blueMouth.style.borderBottom = '3px solid #1e1b4b'
      blueMouth.style.borderRadius = '3px'
    } else {
      // Normal smile
      blueMouth.style.width = '24px'
      blueMouth.style.height = '10px'
      blueMouth.style.borderRadius = '0 0 12px 12px'
      blueMouth.style.border = 'none'
      blueMouth.style.borderBottom = '3px solid #1e1b4b'
    }
  }

  // === ORANGE CHARACTER ===
  const orange = getEl('char-orange')
  if (orange) {
    const tilt = Math.max(-4, Math.min(4, (mouseX - window.innerWidth / 2) / 100))
    orange.style.transform = `rotate(${tilt}deg)`
  }

  const orangeEyeL = getEl('orange-eye-l')
  const orangeEyeR = getEl('orange-eye-r')
  const orangeMouth = getEl('orange-mouth')

  if (orangeEyeL) orangeEyeL.style.height = isBlinking ? '2px' : '18px'
  if (orangeEyeR) orangeEyeR.style.height = isBlinking ? '2px' : '18px'

  let oOffset
  if (isDizzy && mouseSpeed > 50) {
    const t = Date.now() / 200 + Math.PI
    oOffset = { x: Math.cos(t) * 4, y: Math.sin(t) * 4 }
  } else if (eyeContactState === 'looking') {
    // Looking at blue character (to the left)
    oOffset = { x: -4, y: 0 }
  } else if (eyeContactState === 'shy') {
    // Shy: look down-right away from blue
    oOffset = { x: 4, y: 3 }
  } else if (orangeExcite > 0.1) {
    const po = calcPupilOffset(orangeEyeL)
    oOffset = { x: po.x * 0.3, y: Math.max(po.y, 2) }
  } else {
    oOffset = calcPupilOffset(orangeEyeL)
  }

  ;['orange-pupil-l', 'orange-pupil-r'].forEach(id => {
    const el = getEl(id)
    if (el) el.style.transform = `translate(${oOffset.x}px, ${oOffset.y}px)`
  })

  // Orange mouth
  if (orangeMouth) {
    if (orangeExcite > 0.5) {
      orangeMouth.style.width = '14px'
      orangeMouth.style.height = '14px'
      orangeMouth.style.borderRadius = '50%'
      orangeMouth.style.border = '2.5px solid #7c2d12'
      orangeMouth.style.borderBottom = '2.5px solid #7c2d12'
      orangeMouth.style.background = 'transparent'
    } else if (isDizzy && mouseSpeed > 50) {
      orangeMouth.style.width = '22px'
      orangeMouth.style.height = '5px'
      orangeMouth.style.borderRadius = '0'
      orangeMouth.style.border = 'none'
      orangeMouth.style.borderBottom = '2.5px solid #7c2d12'
      orangeMouth.style.borderRadius = '2px'
    } else {
      orangeMouth.style.width = '18px'
      orangeMouth.style.height = '8px'
      orangeMouth.style.borderRadius = '0 0 9px 9px'
      orangeMouth.style.border = 'none'
      orangeMouth.style.borderBottom = '2.5px solid #7c2d12'
    }
  }
}

// ============ EVENTS ============
const onMouseMove = (e) => {
  mouseX = e.clientX
  mouseY = e.clientY
}

let rafId = null
function animationLoop() {
  updateCharacters()
  rafId = requestAnimationFrame(animationLoop)
}

// ============ LIFECYCLE ============
onMounted(() => {
  document.addEventListener('mousemove', onMouseMove)
  updateCharacters()
  scheduleBlink()
  scheduleEyeContact()
  rafId = requestAnimationFrame(animationLoop)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  clearTimeout(blinkTimer)
  clearTimeout(eyeContactTimer)
  cancelAnimationFrame(rafId)
})
</script>

<style scoped>
.welcome-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  position: relative;
  overflow: hidden;
  padding: 40px 24px;
}

/* ===== Characters ===== */
.characters-stage {
  display: flex;
  align-items: flex-end;
  gap: 40px;
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.character {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.char-label {
  font-size: 11px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 1px;
  opacity: 0.6;
}

/* Blue character */
.char-blue {
  width: 80px;
  animation: floatBlue 3s ease-in-out infinite;
}

.blue-body {
  position: relative;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #818cf8, #6366f1);
  border-radius: 50%;
  box-shadow: 0 8px 24px rgba(99,102,241,0.3), inset 0 -4px 8px rgba(0,0,0,0.1);
  transition: box-shadow 0.3s;
}

/* Orange character */
.char-orange {
  width: 66px;
  animation: floatOrange 3s ease-in-out 0.5s infinite;
}

.orange-body {
  position: relative;
  width: 66px;
  height: 88px;
  background: linear-gradient(135deg, #fb923c, #f97316);
  border-radius: 33px;
  box-shadow: 0 8px 24px rgba(249,115,22,0.3), inset 0 -4px 8px rgba(0,0,0,0.1);
  transition: box-shadow 0.3s;
}

@keyframes floatBlue {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
@keyframes floatOrange {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

/* Eyes */
.eyes {
  position: absolute;
  display: flex;
}

.eyeball {
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: height 0.12s ease;
  overflow: hidden;
}

.pupil {
  border-radius: 50%;
  background: #1e1b4b;
  transition: transform 0.08s ease-out;
}

/* Mouth */
.mouth {
  position: absolute;
  transition: all 0.3s ease;
}

/* Blush */
.blush {
  position: absolute;
  width: 12px;
  height: 7px;
  border-radius: 50%;
  pointer-events: none;
}

/* ===== Content ===== */
.welcome-content {
  text-align: center;
  max-width: 700px;
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.welcome-subtitle {
  font-size: 15px;
  color: var(--text-muted);
  margin-bottom: 40px;
}

/* ===== Cards ===== */
.entrance-cards {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  justify-content: center;
}

.entrance-card {
  width: 260px;
  padding: 28px 24px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: var(--bg-secondary);
  cursor: pointer;
  text-align: left;
  transition: transform 0.25s, border-color 0.25s, box-shadow 0.25s;
  position: relative;
  overflow: hidden;
}
.entrance-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  transition: background 0.25s;
}
.card-blog::before { background: var(--accent); }
.card-about::before { background: var(--success); }

.entrance-card:hover {
  transform: translateY(-6px);
  border-color: var(--accent);
  box-shadow: 0 12px 32px rgba(0,0,0,0.2);
}
.card-about:hover { border-color: var(--success); }

.card-icon {
  margin-bottom: 14px;
  color: var(--text-primary);
}
.card-blog .card-icon { color: var(--accent); }
.card-about .card-icon { color: var(--success); }

.entrance-card h2 {
  font-size: 18px;
  margin-bottom: 6px;
}

.entrance-card p {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
  margin-bottom: 16px;
}

.card-arrow {
  font-size: 13px;
  color: var(--accent);
  font-weight: 500;
}
.card-about .card-arrow { color: var(--success); }
.entrance-card:hover .card-arrow { text-decoration: underline; }

.scroll-hint {
  position: absolute;
  bottom: 28px;
  font-size: 13px;
  color: var(--text-muted);
  opacity: 0.5;
}

@media (max-width: 640px) {
  .welcome-title { font-size: 24px; }
  .entrance-cards { flex-direction: column; align-items: center; }
  .entrance-card { width: 100%; max-width: 320px; }
  .characters-stage { gap: 28px; }
}
</style>
