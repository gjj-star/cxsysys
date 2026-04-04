/**
 * 版本管理脚本 — 基于 Git 自动生成 versionName 和 versionCode
 *
 * 工作原理：
 * - versionName: 读取最近的 Git Tag（去掉前缀 v），无 tag 时默认 "1.0.0-dev"
 * - versionCount: 统计当前 commit 距离最近 tag 的提交数，用于构建版本名后缀
 * - versionCode: 读取 Git 总 commit 数，保证单调递增
 *
 * 使用方式：
 *   在 app/build.gradle.kts 中引用:
 *     val version = rootProject.extra["versionInfo"] as Map<String, String>
 *     versionName = version["versionName"]!!
 *     versionCode = version["versionCode"]!!.toInt()
 */

// 执行 git 命令的辅助函数
fun runGitCommand(args: List<String>, default: String): String {
    return try {
        val proc = ProcessBuilder(args)
            .directory(rootProject.projectDir)
            .redirectErrorStream(true)
            .start()
        val output = proc.inputStream.bufferedReader().readText().trim()
        if (proc.waitFor() == 0 && output.isNotEmpty()) output else default
    } catch (e: Exception) {
        default
    }
}

// 1. 获取最近的 Git Tag（如 v1.2.0 → 1.2.0）
val gitTag = runGitCommand(
    listOf("git", "describe", "--tags", "--abbrev=0"),
    "v1.0.0"
)

// 清理 tag 名：去掉前面的 v 前缀
val cleanTag = if (gitTag.startsWith("v")) gitTag.removePrefix("v") else gitTag

// 2. 获取距离最近 tag 的 commit 数（用于构建元数据）
val commitCountSinceTag = runGitCommand(
    listOf("git", "rev-list", "--count", "$gitTag..HEAD"),
    "0"
)

// 3. 获取总 commit 数（用作 versionCode，保证递增）
val totalCommits = runGitCommand(
    listOf("git", "rev-list", "--count", "HEAD"),
    "1"
)

// 4. 获取当前 commit 的短 hash（7位）
val commitHash = runGitCommand(
    listOf("git", "rev-parse", "--short", "HEAD"),
    "unknown"
)

// 构建最终版本信息
val versionName = if (commitCountSinceTag == "0") {
    // 正好在 tag 上，显示干净版本号
    cleanTag
} else {
    // 有新的 commit，追加构建信息，例如: 1.2.0+build.5.abc1234
    "$cleanTag+build.$commitCountSinceTag.$commitHash"
}

val versionCode = totalCommits

// 输出日志（Gradle Sync 时在 Build Output 可见）
println("╔══════════════════════════════════════════════╗")
println("║           📦 版本信息 (Auto Generated)       ║")
println("╠══════════════════════════════════════════════╣")
println("║  Version Name: $versionName".padEnd(43) + "║")
println("║  Version Code:  $versionCode".padEnd(43) + "║")
println("║  Git Tag:       $cleanTag".padEnd(43) + "║")
println("║  Commits since tag: $commitCountSinceTag".padEnd(43) + "║")
println("║  Commit hash:   $commitHash".padEnd(43) + "║")
println("╚══════════════════════════════════════════════╝")

// 暴露给子项目使用
extra["versionInfo"] = mapOf(
    "versionName" to versionName,
    "versionCode" to versionCode,
    "tag" to cleanTag,
    "commitHash" to commitHash,
    "commitsSinceTag" to commitCountSinceTag
)
