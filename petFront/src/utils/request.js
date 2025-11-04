import axios from 'axios'
import router from "../router";

const request = axios.create({
    baseURL: '/api',
    timeout: 500000
})
window.onerror = function(message, url, lineNumber) {

    console.error("发生未捕获的错误:", message);

  };
  
request.interceptors.response.use(
    response => {
        // 处理文件下载等特殊响应类型
        if (response.config.responseType === 'blob') {
            return response;
        }

        // 确保返回的数据是JSON格式
        let res = response.data;
        if (typeof res === 'string') {
            res = JSON.parse(res);
        }

        // 根据响应状态码处理不同的逻辑
        switch (response.status) {
            case 200:
                // 正常逻辑处理
                break;
            case 500:
                // 服务器内部错误
                alert('服务器内部错误，请稍后再试！');
                break;
            case 403:
                // 无权限访问
                alert('您没有权限访问该资源！');
                break;
            case 408:
                // 请求超时
                alert('请求超时，请检查您的网络连接！');
                break;
            case 401:
                // 未授权
                alert('登录失效，请重新登录！');
                break;
            // 可以继续添加其他状态码的处理
            default:
                // 其他状态码的处理
                break;
        }

        return res;
    },
    error => {
        // 错误处理
        const { status,data } = error.response;
        console.log("服务器错误")
        console.log(status+"   "+data);
        if(status==500){
            alert('服务器内部错误，请稍后再试！');
        }

        return Promise.reject(error);

    }
);

export default request

