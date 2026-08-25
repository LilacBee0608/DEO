package com.bili.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

//组员签名 hyc zyl wcr lmr lcq

/**
 * DEO后端启动类
 * 启动算法思路(端口监听 / 精确打印 localhost 网址):
 * 1. SpringApplication.run() 返回 ConfigurableApplicationContext,在其初始化完成后
 *    Web 服务器已绑定端口,此时可通过 Environment 取出实际运行端口
 * 2. local.server.port 由 ServletWebServerInitializedEvent 回填 → 这是真正绑定的端口
 *    (即使 application.yml 配置 server.port=0 随机端口,此处也能拿到最终端口号)
 * 3. server.servlet.context-path 作为接口前缀,与 host + port 拼接得到完整 API Base URL
 * 4. 同时取本机局域网 IP,方便同网段其他设备联调
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

        // 6. 自动启动前端开发服务器 (npm run dev)
        //    读取 application.yml 的 frontend.* 配置
        //    Windows 下新开命令行窗口启动,与后端日志分离便于排错
        startFrontendDevServer(env);
    }

    /**
     * 自动启动前端开发服务器
     * 原理: 通过 ProcessBuilder 在新窗口执行 npm run dev
     * - Windows: cmd /c start "标题" cmd /k "npm run dev" (新窗口保持开启)
     * - Linux/Mac: sh -c "npm run dev" 后台运行
     * 配置项 (application.yml):
     *   frontend.auto-start: 是否自动启动 (默认 true)
     *   frontend.dir:        前端项目目录 (留空则自动探测)
     *   frontend.command:    启动命令 (默认 npm run dev)
     *
     * 路径自动探测算法:
     * 由于 IDEA 运行后端时 user.dir 可能是项目根目录或 backend 子目录,
     * 不再依赖固定相对路径,而是从当前工作目录向上逐级查找,
     * 找到第一个包含 "frontend/package.json" 的目录作为前端项目根目录。
     */
    private static void startFrontendDevServer(ConfigurableEnvironment env) {
        // 读取配置
        boolean autoStart = Boolean.parseBoolean(env.getProperty("frontend.auto-start", "true"));
        String yellow = "\033[33m";
        String green = "\033[32m";
        String red = "\033[31m";
        String reset = "\033[0m";

        if (!autoStart) {
            System.out.println(yellow + "  · 前端自动启动     : 已禁用 (frontend.auto-start=false)" + reset);
            return;
        }

        String command = env.getProperty("frontend.command", "npm run dev");
        String configuredDir = env.getProperty("frontend.dir", "");
        File dir;

        if (!configuredDir.isEmpty()) {
            // 显式配置了前端目录,直接使用
            dir = new File(configuredDir);
            if (!dir.isDirectory() || !new File(dir, "package.json").exists()) {
                System.out.println(yellow + "  · 前端自动启动     : 跳过 (配置的 frontend.dir 无效: " + configuredDir + ")" + reset);
                return;
            }
        } else {
            // 未配置目录,自动探测:从当前工作目录向上查找 frontend 子目录
            dir = detectFrontendDir();
            if (dir == null) {
                System.out.println(yellow + "  · 前端自动启动     : 跳过 (未自动探测到 frontend 目录,请在 application.yml 配置 frontend.dir)" + reset);
                return;
            }
        }

        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(dir);
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                // Windows: 用 cmd /c start 在新窗口启动
                // 标题用 "DEO前端-Vite",/k 表示执行后保留窗口便于查看日志
                // 优先尝试 npm.cmd(Windows 下 npm 是批处理脚本,必须用 .cmd 后缀才能被 ProcessBuilder 正确执行)
                String npmCmd = resolveNpmCommand();
                pb.command("cmd", "/c", "start", "\"DEO前端-Vite\"", "cmd", "/k", npmCmd + " run dev");
            } else {
                // Linux/Mac: 后台执行
                pb.command("sh", "-c", command);
            }
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // 异步读取子进程 stdout/stderr,防止缓冲区满导致阻塞
            // (Windows 新窗口模式下输出已在独立窗口显示,这里只消耗流)
            new Thread(() -> {
                try (var is = process.getInputStream()) {
                    byte[] buf = new byte[1024];
                    while (is.read(buf) != -1) {
                        // 丢弃输出(已在新窗口显示)
                    }
                } catch (Exception ignored) {
                }
            }, "frontend-dev-server-reader").start();

            System.out.println(green + "  · 前端自动启动     : " + reset + "成功 (" + command + ")");
            System.out.println(green + "  · 前端目录         : " + reset + dir.getAbsolutePath());
            System.out.println(green + "  · 前端开发地址     : " + reset + "http://localhost:5173/");
            System.out.println(green + "============================================" + reset);
            System.out.println();
        } catch (Exception e) {
            System.out.println(red + "  · 前端自动启动失败 : " + reset + e.getMessage());
            System.out.println(yellow + "  · 请手动在前端目录执行: " + reset + command);
        }
    }

    /**
     * 自动探测前端项目目录
     * 从当前工作目录开始,先检查同级是否有 frontend 子目录,
     * 若没有则逐级向上一级目录查找,直到找到包含 frontend/package.json 的目录。
     * 这样无论 IDEA 的 user.dir 是项目根还是 backend 子目录都能正确定位。
     *
     * @return 前端目录的 File 对象,未找到返回 null
     */
    private static File detectFrontendDir() {
        File current = new File(System.getProperty("user.dir")).getAbsoluteFile();
        // 最多向上查找 5 级,防止无限循环
        for (int i = 0; i < 5 && current != null; i++) {
            // 检查同级 frontend 子目录
            File frontend = new File(current, "frontend");
            if (frontend.isDirectory() && new File(frontend, "package.json").exists()) {
                return frontend;
            }
            // 也检查当前目录本身是否就是 frontend(直接运行 frontend 下代码的边缘情况)
            if ("frontend".equals(current.getName()) && new File(current, "package.json").exists()) {
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    /**
     * 解析 Windows 下 npm 命令的实际路径
     * ProcessBuilder 在 Windows 下无法直接执行 "npm"(它是批处理脚本 npm.cmd),
     * 需要找到 npm.cmd 的绝对路径或退化为 "npm" 让 cmd /k 自行解析。
     * 这里先尝试直接用 "npm",因为 cmd /k 会从 PATH 查找 npm.cmd。
     *
     * @return npm 命令字符串(默认 "npm")
     */
    private static String resolveNpmCommand() {
        // cmd /k 启动的新窗口会继承 PATH,可直接用 npm
        // 若 PATH 中没有 npm,组员需自行确认 Node.js 已安装并配置 PATH
        return "npm";
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
