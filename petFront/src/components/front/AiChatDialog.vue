<template>
  <el-dialog
    title="宠物AI对话"
    :visible.sync="visible"
    width="50%"
    class="ai-chat-dialog"
    :close-on-click-modal="false"
  >
    <div class="chat-container">
      <div class="pet-selector">
        <el-select
          v-model="selectedPet"
          placeholder="请选择要对话的宠物"
          @change="handlePetChange"
        >
          <el-option
            v-for="pet in petList"
            :key="pet.id"
            :label="pet.name"
            :value="pet.id"
          >
            <span style="float: left">
              <el-avatar :size="30" :src="HOST + pet.img"></el-avatar>
              {{ pet.name }}
            </span>
          </el-option>
        </el-select>
      </div>

      <div class="chat-messages" ref="messageContainer">
        <div
          v-for="(msg, index) in chatMessages"
          :key="index"
          :class="['message', msg.isUser ? 'user-message' : 'pet-message']"
        >
          <el-avatar
            :size="40"
            :src="
              msg.isUser
                ? userAvatar
                : selectedPetInfo
                ? HOST + selectedPetInfo.img
                : ''
            "
          ></el-avatar>
          <div class="message-content">
            <p>{{ msg.content }}</p>
            <span class="message-time">{{ formatTime(msg.time) }}</span>
          </div>
        </div>
        <div v-if="isLoading" class="message pet-message">
          <el-avatar
            :size="40"
            :src="selectedPetInfo ? HOST + selectedPetInfo.img : ''"
          ></el-avatar>
          <div class="message-content loading">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="messageInput"
          type="textarea"
          :rows="3"
          placeholder="输入消息..."
          :maxlength="200"
          show-word-limit
          @keyup.enter.native="sendMessage"
        ></el-input>
        <el-button
          type="primary"
          @click="sendMessage"
          :disabled="!messageInput.trim() || !selectedPet"
        >
          发送消息
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import Request from '@/utils/request.js'

export default {
  inject: ['userInfo'],
  data() {
    return {
      visible: false,
      selectedPet: null,
      selectedPetInfo: null,
      petList: [],
      messageInput: '',
      chatMessages: [],
      userAvatar: require('@/assets/user.png'),
      messageCount: 0,
      lastMessageTime: null,
      isLoading: false,
    }
  },
  methods: {
    handleCreate() {
      this.visible = true
      this.loadPets()
    },
    async loadPets() {
      try {
        const res = await Request.get('/animal/selectAll')
        if (res.code === '0') {
          this.petList = res.data
        }
      } catch (error) {
        this.$message.error('获取宠物列表失败')
      }
    },
    async handlePetChange(petId) {
      this.selectedPetInfo = this.petList.find((pet) => pet.id === petId)
      this.chatMessages = []
    },
    async sendMessage() {
      if (!this.messageInput.trim()) return

      // 检查消息频率限制
      const now = new Date()
      if (
        this.lastMessageTime &&
        now - this.lastMessageTime < 60000 &&
        this.messageCount >= 10
      ) {
        this.$message.warning('发送消息过于频繁，请稍后再试')
        return
      }

      // 添加用户消息
      this.chatMessages.push({
        content: this.messageInput,
        time: new Date(),
        isUser: true,
      })

      const message = this.messageInput
      this.messageInput = ''
      this.isLoading = true

      try {
        // 修改为使用 URL 参数形式发送请求
        const response = await Request.post(
          `/api/pet/ai/chat?userId=${this.userInfo.id}&petId=${
            this.selectedPet
          }&message=${encodeURIComponent(message)}`
        )

        if (response.code === '0') {
          console.log(response)
          // 添加宠物回复
          this.chatMessages.push({
            content: response.data.response,
            time: new Date(response.data.createdAt),
            isUser: false,
          })

          // 更新消息计数
          if (!this.lastMessageTime || now - this.lastMessageTime >= 60000) {
            this.messageCount = 1
          } else {
            this.messageCount++
          }
          this.lastMessageTime = now

          this.$nextTick(() => {
            this.scrollToBottom()
          })
        }
      } catch (error) {
        let errorMsg = '发送消息失败'
        if (error.response) {
          switch (error.response.status) {
            case 404:
              errorMsg = '该宠物不存在'
              break
            case 500:
              errorMsg = 'AI服务暂时不可用'
              break
          }
        }
        this.$message.error(errorMsg)
      } finally {
        this.isLoading = false
      }
    },
    scrollToBottom() {
      const container = this.$refs.messageContainer
      container.scrollTop = container.scrollHeight
    },
    formatTime(date) {
      return new Date(date).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
      })
    },
  },
}
</script>

<style lang="less" scoped>
.ai-chat-dialog {
  /deep/ .el-dialog {
    border-radius: 16px;
    overflow: hidden;

    .el-dialog__header {
      background: #f8f9fb;
      padding: 15px 20px;
      margin: 0;
      border-bottom: 1px solid #ebeef5;

      .el-dialog__title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }
  }

  .chat-container {
    display: flex;
    flex-direction: column;
    height: 600px;

    .pet-selector {
      margin: 20px;

      .el-select {
        width: 100%;

        /deep/ .el-input__inner {
          border-radius: 8px;
        }
      }
    }

    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
      background: #f8f9fb;
      border-radius: 12px;
      margin: 0 20px 20px;
      box-shadow: inset 0 2px 6px rgba(0, 0, 0, 0.05);

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-thumb {
        background: #dcdfe6;
        border-radius: 3px;
      }

      .message {
        display: flex;
        align-items: flex-start;
        margin-bottom: 25px;

        .el-avatar {
          margin-right: 12px;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .message-content {
          max-width: 70%;
          padding: 12px 16px;
          border-radius: 12px;
          position: relative;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

          p {
            margin: 0;
            word-break: break-word;
            line-height: 1.5;
          }

          .message-time {
            font-size: 12px;
            color: #909399;
            position: absolute;
            bottom: -20px;
          }
        }

        &.user-message {
          flex-direction: row-reverse;

          .el-avatar {
            margin-right: 0;
            margin-left: 12px;
          }

          .message-content {
            background: #67c23a;
            color: white;

            .message-time {
              right: 0;
              color: #909399;
            }
          }
        }

        &.pet-message .message-content {
          background: white;
          border: 1px solid #ebeef5;

          .message-time {
            left: 0;
          }
        }
      }

      .message-content.loading {
        min-width: 60px;
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 6px;
        padding: 16px;

        .dot {
          width: 8px;
          height: 8px;
          background: #909399;
          border-radius: 50%;
          animation: bounce 1.4s infinite ease-in-out;
          opacity: 0.6;

          &:nth-child(1) {
            animation-delay: 0s;
          }
          &:nth-child(2) {
            animation-delay: 0.32s;
          }
          &:nth-child(3) {
            animation-delay: 0.64s;
          }
        }
      }
    }

    .chat-input {
      margin: 0 20px 20px;

      .el-textarea {
        margin-bottom: 12px;

        /deep/ .el-textarea__inner {
          border-radius: 8px;
          resize: none;
          transition: all 0.3s;

          &:focus {
            box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.2);
          }
        }
      }

      .el-button {
        width: 100%;
        border-radius: 8px;
        font-size: 16px;
        height: 40px;
        transition: all 0.3s;

        &:not(:disabled):hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(103, 194, 58, 0.2);
        }
      }
    }
  }
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
