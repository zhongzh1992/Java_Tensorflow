package org.demo.bert.predict;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


public class SocketClient {

    private static Object remoteCall(String content) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", content);
        String str = jsonObject.toJSONString();
        // 访问服务进程的套接字
        Socket socket = null;
        try {
            // 初始化套接字，设置访问服务的主机和进程端口号，HOST是访问python进程的主机名称，可以是IP地址或者域名，PORT是python进程绑定的端口号
            socket = new Socket("localhost", 12345);
            // 获取输出流对象
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os);
            Stopwatch stopwatch = Stopwatch.createStarted();

            // 发送内容
            out.print(str);
            // 告诉服务进程，内容发送完毕，可以开始处理
            out.print("over");
            // 获取服务进程的输入流
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String tmp = null;
            StringBuilder sb = new StringBuilder();
            // 读取内容
            while ((tmp = br.readLine()) != null)
                sb.append(tmp);
            // 解析结果
            JSONObject res = JSON.parseObject(sb.toString());
            System.out.println(res.get("class"));
            System.out.println(String.format("time cost %d", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return res.get("class");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
            }
            System.out.println("远程接口调用结束.");
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        remoteCall("武汉加油");
    }
}
