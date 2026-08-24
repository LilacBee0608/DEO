package com.bili.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 仿哔哩哔哩后端启动类
 * 启动算法思路(端口监听 / 精确打印 localhost 网址):
 * 1. SpringApplication.run() 返回 ConfigurableApplicationContext,在其初始化完成后
 *    Web 服务器已绑定端口,此时可通过 Environment 取出实际运行端口
 * 2. local.server.port 由 ServletWebServerInitializedEvent 回填 → 这是真正绑定的端口
 *    (即使 application.yml 配置 server.port=0 随机端口,此处也能拿到最终端口号)
 * 3. server.servlet.context-path 作为接口前缀,与 host + port 拼接得到完整 API Base URL
 * 4. 同时取本机局域网 IP,方便同网段其他设备联调
 * wcr
 * @author bili-demo
 */
@SpringBootApplication
@MapperScan("com.bili.demo.mapper")  // 扫描 Mapper 接口
public class BiliDemoApplication {

    public static void main(String[] args) {
        // 1. 启动 SpringBoot,拿到上下文
        ConfigurableApplicationContext context = SpringApplication.run(BiliDemoApplication.class, args);

        // 2. 从 Environment 读取真实运行信息(不再硬编码端口)
        ConfigurableEnvironment env = context.getEnvironment();

        // local.server.port 是 Web 容器实际绑定到的端口(推荐优先使用)
        // 若因特殊情况为空,回退使用配置文件里的 server.port
        String portStr = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
        int port = Integer.parseInt(portStr);

        // 接口前缀 (context-path),默认 "" 表示无前缀
        String contextPath = env.getProperty("server.servlet.context-path", "");

        // server.address 监听地址,未配置则使用 localhost
        String address = env.getProperty("server.address", "localhost");
        if (address.isEmpty() || "0.0.0.0".equals(address) || "::".equals(address)) {
            address = "localhost"; // 展示给用户用 localhost 更友好
        }

        // 3. 拼接 URL
        String apiBaseUrl = "http://" + address + ":" + port + contextPath;
        String swaggerUiUrl = ""; // 本项目暂未集成 Knife4j,留占位,可后续扩展

        // 4. 额外取本机局域网 IP,方便多设备联调
        String lanIp = resolveLocalLanIp();

        // 5. 统一打印(颜色前缀 \033[32m 绿色,IDEA 控制台支持 ANSI)
        String green = "\033[32m";
        String reset = "\033[0m";
        System.out.println();
        System.out.println(green + "============================================" + reset);
        System.out.println(green + "  DEO后端启动成功!" + reset);
        System.out.println(green + "  · 实际监听端口 : " + reset + port);
        System.out.println(green + "  · Context-Path : " + reset + (contextPath.isEmpty() ? "(无)" : contextPath));
        System.out.println(green + "  · API 根路径   : " + reset + apiBaseUrl);
        System.out.println(green + "  · 本地访问     : " + reset + "http://localhost:" + port + contextPath);
        if (lanIp != null) {
            System.out.println(green + "  · 局域网访问   : " + reset + "http://" + lanIp + ":" + port + contextPath);
        }
        System.out.println(green + "  · 前端首页     : " + reset + "http://localhost:5173/");
        System.out.println(green + "============================================" + reset);
        System.out.println();
    }

    /**
     * 解析本机局域网 IPv4 地址
     * 遇到多网卡时,优先选择 192.168.x.x / 10.x.x.x / 172.16.x.x 段的地址
     * 无法解析时返回 null,不影响主流程
     */
    private static String resolveLocalLanIp() {
        try {
            String defaultHost = InetAddress.getLocalHost().getHostAddress();
            if (isLanAddress(defaultHost)) return defaultHost;
            // 若默认地址回落到 127.0.0.1,遍历所有网卡查找局域网地址
            for (java.net.NetworkInterface ni : java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || ni.isVirtual() || !ni.isUp()) continue;
                for (java.net.InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    String ip = ia.getAddress().getHostAddress();
                    if (isLanAddress(ip)) return ip;
                }
            }
            return defaultHost;
        } catch (UnknownHostException | java.net.SocketException ignored) {
            return null;
        }
    }

    /** 判断是否为私有局域网站段 */
    private static boolean isLanAddress(String ip) {
        if (ip == null) return false;
        return ip.startsWith("192.168.") || ip.startsWith("10.")
                || ip.startsWith("172.16.") || ip.startsWith("172.17.") || ip.startsWith("172.18.") || ip.startsWith("172.19.")
                || ip.startsWith("172.2") || ip.startsWith("172.30.") || ip.startsWith("172.31.");
    }
}
