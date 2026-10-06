# 参与贡献 · Contributing

感谢你考虑为这个项目贡献力量！

## 请先读这个

本项目的特殊性质：

1. **纯 AI 打造**：全部代码由大语言模型生成，未经人工逐行审查。
2. **非官方移植**：代码主体来源于 IC2 反编译，版权边界敏感（详见 README 声明）。

因此，**在你投入大量时间之前，请先开 Issue 讨论**，以免方向不合而浪费精力。

## 如何贡献

### 报告问题（最有价值的贡献）

当前 alpha 阶段最需要的是**实测反馈**：

- 在 [Issues](https://github.com/modred123/ic2-neoforge/issues) 开新 issue
- 描述：做了什么操作、期望什么、实际发生了什么
- 附件：`logs/latest.log`、崩溃报告、截图/录屏
- 环境：NeoForge 版本、其他已装模组列表

### 提交代码

1. Fork 本仓库
2. 创建分支（`fix/xxx` 或 `feature/xxx`）
3. 提交你的修改，commit message 简要说明动机
4. 发 Pull Request，说明：
   - 解决了什么问题（关联 Issue）
   - 如何验证（测试步骤）

注意：

- 提交即表示你同意你的贡献以与本项目相同的方式（All Rights Reserved）发布
- 修改应尽量保持与原作（IC2 1.12.2）语义一致 —— 本项目的目标是「**忠实移植**」，
  不是重做玩法；新增功能请先开 Issue 讨论
- 代码风格保持与现有代码一致（本项目为 CFR 反编译基线 + 迁移补丁）

## 本地开发

```bash
# 需要 JDK 21
gradlew.bat build          # 构建
gradlew.bat runClient      # 启动开发客户端
gradlew.bat runServer      # 启动开发服务端
```

## 沟通

- 主要语言：中文、英文均可
- Issue 与 PR 默认中文交流；维护者会视需要提供英文摘要
