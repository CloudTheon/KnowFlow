package com.cloudtheon.knowflowcore.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码执行工具
 * <p>
 * 使用 JDK 单文件源码运行模式（{@code java Xxx.java}）执行用户提供的完整 Java 代码，
 * 返回程序的标准输出。带超时限制与输出截断，防止恶意/死循环代码拖垮服务。
 * </p>
 * <p>
 * 注意：非沙箱环境，仅供本机学习自用；若部署到公网需接入真正的安全沙箱。
 * </p>
 */
@Component
public class CodeExecutorTool {

    private static final long TIMEOUT_SECONDS = 15;
    private static final int MAX_OUTPUT = 4000;
    private static final Pattern PUBLIC_CLASS = Pattern.compile("public\\s+class\\s+(\\w+)");

    @Tool(description = "执行一段完整的 Java 代码（必须包含 main 方法和类定义），返回程序的标准输出。用于帮助用户调试或运行 Java 代码片段")
    public String executeJava(
            @ToolParam(description = "完整的 Java 源码，例如：public class Main { public static void main(String[] args) { System.out.println(\"hello\"); } }") String code) {
        if (code == null || code.isBlank()) {
            return "错误：代码为空";
        }
        Path dir = null;
        try {
            dir = Files.createTempDirectory("knowflow-code");
            String className = extractClassName(code);
            Path file = dir.resolve(className + ".java");
            Files.writeString(file, code, StandardCharsets.UTF_8);

            ProcessBuilder pb = new ProcessBuilder("java", file.toAbsolutePath().toString());
            pb.directory(dir.toFile());
            pb.redirectErrorStream(true);

            Process p = pb.start();
            boolean finished = p.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return "错误：执行超时（超过 " + TIMEOUT_SECONDS + " 秒，可能存在死循环）";
            }

            String output = readStream(p.getInputStream());
            int exitCode = p.exitValue();
            String result = "退出码: " + exitCode + "\n输出:\n" + output.trim();
            if (result.length() > MAX_OUTPUT) {
                result = result.substring(0, MAX_OUTPUT) + "\n...(输出过长，已截断)";
            }
            return result;
        } catch (Exception e) {
            return "执行失败: " + e.getMessage();
        } finally {
            if (dir != null) {
                try {
                    try (var stream = Files.walk(dir)) {
                        stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException ignored) {
                            }
                        });
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    /** 从源码提取 public class 名称，用于匹配文件名（Java 单文件运行要求） */
    private String extractClassName(String code) {
        Matcher m = PUBLIC_CLASS.matcher(code);
        return m.find() ? m.group(1) : "Main";
    }

    private String readStream(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
                if (sb.length() > MAX_OUTPUT) {
                    break;
                }
            }
        }
        return sb.toString();
    }
}
