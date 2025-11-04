<template>
    <div class="login-container">
        <!-- 可爱背景 -->
        <div class="cute-background"></div>
        
        <!-- 装饰元素 -->
        <div class="decoration-elements">
            <img src="../assets/逗狗宠物.svg" class="pet-illustration" alt="逗狗宠物" />
            <div class="bubble bubble-1"></div>
            <div class="bubble bubble-2"></div>
            <div class="bubble bubble-3"></div>
        </div>
        
        <!-- 内容区域 -->
        <div class="content-area">
            <router-view />
        </div>
    </div>
</template>

<script>
export default {
    name: 'LoginLayout'
}
</script>

<style scoped>
.login-container {
    width: 100%;
    height: 100vh;
    background: linear-gradient(120deg, #FFF0F5 0%, #FFD1DC 100%);
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
}

.cute-background {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: radial-gradient(#ffffff 10%, transparent 10%);
    background-size: 20px 20px;
    opacity: 0.3;
    z-index: 0;
}

.decoration-elements {
    position: absolute;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 1;
}

.pet-illustration {
    position: absolute;
    bottom: 20px;
    right: 30px;
    width: 200px;
    height: auto;
    opacity: 0.8;
}

.bubble {
    position: absolute;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.6);
    box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.bubble-1 {
    top: 15%;
    left: 10%;
    width: 60px;
    height: 60px;
}

.bubble-2 {
    top: 30%;
    right: 15%;
    width: 40px;
    height: 40px;
}

.bubble-3 {
    bottom: 20%;
    left: 20%;
    width: 50px;
    height: 50px;
}

.content-area {
    position: relative;
    z-index: 10;
    width: 100%;
    max-width: 900px;
    padding: 0 20px;
}
</style>