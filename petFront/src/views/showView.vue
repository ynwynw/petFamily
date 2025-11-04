<template>
  <div class="home-wrapper">
    <!-- 顶部数据卡片 -->
    <div class="date-row">
      <!-- 宠物相关统计 -->
      <el-card shadow="hover" :body-style="{ padding: '0px' }" class="data-col">
        <div class="date-block">
          <i class="el-icon-s-goods icon-box" style="background: linear-gradient(45deg, #2d8cf0, #5cadff)" />
          <div class="date-cont">
            <CountTo class="count" :start-val="0" :end-val="statistics.animalCount" :duration="3000" />
            <p class="title">宠物总数</p>
            <p class="sub-title">可领养: {{statistics.availableCount}}</p>
          </div>
        </div>
      </el-card>

      <!-- 用户统计 -->
      <el-card shadow="hover" :body-style="{ padding: '0px' }" class="data-col">
        <div class="date-block">
          <i class="el-icon-s-custom icon-box" style="background: linear-gradient(45deg, #19be6b, #39d353)" />
          <div class="date-cont">
            <CountTo class="count" :start-val="0" :end-val="statistics.userCount" :duration="3000" />
            <p class="title">用户总数</p>
            <p class="sub-title">本月新增: {{statistics.newUserCount}}</p>
          </div>
        </div>
      </el-card>

      <!-- 领养统计 -->
      <el-card shadow="hover" :body-style="{ padding: '0px' }" class="data-col">
        <div class="date-block">
          <i class="el-icon-s-order icon-box" style="background: linear-gradient(45deg, #ff9900, #ffb84d)" />
          <div class="date-cont">
            <CountTo class="count" :start-val="0" :end-val="statistics.adoptCount" :duration="3000" />
            <p class="title">领养总数</p>
            <p class="sub-title">本月: {{statistics.monthAdoptCount}}</p>
          </div>
        </div>
      </el-card>

      <!-- 订单统计 -->
      <el-card shadow="hover" :body-style="{ padding: '0px' }" class="data-col">
        <div class="date-block">
          <i class="el-icon-money icon-box" style="background: linear-gradient(45deg, #e46cbb, #f5a3d7)" />
          <div class="date-cont">
            <CountTo class="count" :start-val="0" :end-val="statistics.orderCount" :duration="3000" />
            <p class="title">订单总数</p>
            <p class="sub-title">总金额: ¥{{statistics.totalAmount}}</p>
          </div>
        </div>
      </el-card>

      <!-- 寄养统计 -->
      <el-card shadow="hover" :body-style="{ padding: '0px' }" class="data-col">
        <div class="date-block">
          <i class="el-icon-house icon-box" style="background: linear-gradient(45deg, #9a66e4, #b794f4)" />
          <div class="date-cont">
            <CountTo class="count" :start-val="0" :end-val="statistics.fosterCount" :duration="3000" />
            <p class="title">寄养总数</p>
            <p class="sub-title">当前寄养: {{statistics.currentFosterCount}}</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="chart-header">
              <span>近1年领养趋势</span>
            </div>
            <div id="adoptTrendChart" class="chart-wrapper"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="chart-header">
              <span>宠物状态分布</span>
            </div>
            <div id="animalStatusChart" class="chart-wrapper"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 品种分布和通知公告区域 -->
    <div class="bottom-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="chart-header">
              <span>宠物品种分布</span>
            </div>
            <div id="animalBreedChart" class="chart-wrapper"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="notice-card">
            <div slot="header" class="notice-header">
              <span>通知公告</span>
        </div>
            <div class="notice-content">
            <el-timeline>
                <el-timeline-item
                  v-for="(announcement, index) in announcements"
                  :key="index"
                  :timestamp="announcement.time"
                  placement="top"
                  :type="index === 0 ? 'primary' : ''"
                  :color="index === 0 ? '#2d8cf0' : ''"
                >
                  <el-card class="announcement-card">
                    <h4 class="announcement-title">{{ announcement.title }}</h4>
                    <p class="announcement-text">{{ announcement.content }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
  </el-card>
        </el-col>
      </el-row>
</div>
  </div>
</template>

<script>
import { Timeline, TimelineItem, Card, Button } from 'element-ui';
import CountTo from 'vue-count-to'
import Request from '../utils/request.js'
import * as echarts from 'echarts'

export default {
  name: 'HomeView',
  components: {
    [Card.name]: Card,
    [Timeline.name]: Timeline,
    [TimelineItem.name]: TimelineItem,
    [Button.name]: Button,
    CountTo
  },
  data() {
    return {
      statistics: {
        animalCount: 0,
        availableCount: 0,
        userCount: 0,
        newUserCount: 0,
        adoptCount: 0,
        monthAdoptCount: 0,
        orderCount: 0,
        totalAmount: 0,
        fosterCount: 0,
        currentFosterCount: 0
      },
      noticeLimit: 10,
      chartData: {
        columns: ['日期', '领养数量'],
        rows: [
          { 日期: '1月1日', 领养数量: 120 },
          // 更多数据
        ]
      },
      chartSettings: {
        // 配置项
      },
      announcements: [],
      adoptTrendChart: null,
      animalStatusChart: null,
      animalBreedChart: null,
    };
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      // 获取统计数据
      Request.get("/show/getStatistics").then(response => {
        if (response.code === '0') {
          this.statistics = response.data
        }
      })
      
      // 获取图表数据
      this.fetchChartData()
      
      // 获取通知公告数据
      this.fetchNotices()
    },
    fetchNotices() {
      // 使用NoticeController的limit接口获取最新的通知
      Request.get("/notice/limit", {
        params: {
          count: this.noticeLimit
        }
      }).then(response => {
        if (response.code === '0') {
          this.announcements = response.data
        }
      })
    },
    fetchChartData() {
      // 获取领养趋势数据
      Request.get("/show/getAdoptTrend").then(response => {
        if (response.code === '0') {
          this.initAdoptTrendChart(response.data)
        }
      })
      
      // 获取宠物状态分布数据
      Request.get("/show/getAnimalStatus").then(response => {
        if (response.code === '0') {
          this.initAnimalStatusChart(response.data)
        }
      })

      // 获取宠物品种分布数据
      Request.get("/show/getAnimalBreed").then(response => {
        if (response.code === '0') {
          this.initAnimalBreedChart(response.data)
        }
      })
    },
    initAdoptTrendChart(data) {
      const chartDom = document.getElementById('adoptTrendChart')
      if (!chartDom) return
      
      if (this.adoptTrendChart) {
        this.adoptTrendChart.dispose()
      }
      
      this.adoptTrendChart = echarts.init(chartDom)
      
      const dates = data.map(item => item.date)
      const counts = data.map(item => item.count)
      
      const option = {
        title: {
          text: '近1年领养趋势'
        },
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const param = params[0];
            return `${param.name}<br/>领养数量：${param.value}`;
          }
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            rotate: 45,
            interval: 'auto',
            formatter: function(value) {
              return value.substring(5); // 只显示月-日
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '领养数量'
        },
        series: [{
          name: '领养数量',
          data: counts,
          type: 'line',
          smooth: true,
          areaStyle: {
            opacity: 0.3
          },
          itemStyle: {
            color: '#2d8cf0'
          }
        }]
      }
      
      this.adoptTrendChart.setOption(option)
    },
    initAnimalStatusChart(data) {
      const chartDom = document.getElementById('animalStatusChart')
      if (!chartDom) return
      
      if (this.animalStatusChart) {
        this.animalStatusChart.dispose()
      }
      
      this.animalStatusChart = echarts.init(chartDom)
      
      const option = {
        title: {
          text: '宠物状态分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle',
          itemWidth: 10,
          itemHeight: 10,
          textStyle: {
            fontSize: 12
          }
        },
        series: [{
          name: '宠物状态',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}: {c}'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: true,
            length: 10,
            length2: 10,
            smooth: true
          },
          data: data.map(item => ({
            name: item.status,
            value: item.count
          }))
        }]
      }
      
      this.animalStatusChart.setOption(option)
    },
    initAnimalBreedChart(data) {
      const chartDom = document.getElementById('animalBreedChart')
      if (!chartDom) return
      
      if (this.animalBreedChart) {
        this.animalBreedChart.dispose()
      }
      
      this.animalBreedChart = echarts.init(chartDom)
      
      const option = {
        title: {
          text: '宠物品种分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle',
          itemWidth: 10,
          itemHeight: 10,
          textStyle: {
            fontSize: 12
          }
        },
        series: [{
          name: '宠物品种',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}: {c}'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: true,
            length: 10,
            length2: 10,
            smooth: true
          },
          data: data.map(item => ({
            name: item.breed,
            value: item.count
          }))
        }]
      }
      
      this.animalBreedChart.setOption(option)
    }
  },
  mounted() {
    this.fetchData()
    // 监听窗口大小变化，重绘图表
    window.addEventListener('resize', () => {
      this.adoptTrendChart?.resize()
      this.animalStatusChart?.resize()
      this.animalBreedChart?.resize()
    })
  },
  beforeDestroy() {
    // 销毁图表实例
    this.adoptTrendChart?.dispose()
    this.animalStatusChart?.dispose()
    this.animalBreedChart?.dispose()
    window.removeEventListener('resize', this.handleResize)
  }
};
</script>

<style lang="less">


.home-wrapper {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.date-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.data-col {
  flex: 1;
  min-width: 200px;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-5px);
  }
}

.date-block {
  display: flex;
  align-items: center;
  padding: 20px;
}

.icon-box {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
  font-size: 24px;
}

.date-cont {
  flex: 1;
}

.count {
  font-size: 24px;
  font-weight: bold;
  color: #2d8cf0;
  margin-bottom: 5px;
}

.title {
  font-size: 16px;
  color: #666;
  margin: 5px 0;
}

.sub-title {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.charts-container {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-header {
  font-size: 16px;
  font-weight: bold;
  color: #2d8cf0;
}

.chart-wrapper {
  height: 400px;
  width: 100%;
}

.bottom-container {
  margin-top: 20px;
}

.notice-card {
  height: 100%;
}

.notice-header {
  font-size: 18px;
  font-weight: bold;
  color: #2d8cf0;
  text-align: center;
}

.notice-content {
  height: 400px;
  overflow-y: auto;
  padding: 20px;
}

.announcement-card {
  margin-bottom: 10px;
  transition: all 0.3s;
  
  &:hover {
    transform: translateX(5px);
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  }
}

.announcement-title {
  color: #2d8cf0;
  margin: 0 0 10px 0;
  font-size: 16px;
}

.announcement-text {
  color: #666;
  margin: 0;
  line-height: 1.6;
}

// 自定义滚动条样式
.notice-content::-webkit-scrollbar {
  width: 6px;
}

.notice-content::-webkit-scrollbar-thumb {
  background-color: #2d8cf0;
  border-radius: 3px;
}

.notice-content::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}
</style>