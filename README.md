# 🌻 植物大战僵尸 - Java Swing 实现

> 使用 Java Swing 开发的简易植物大战僵尸桌面小游戏，适合 Java 面向对象编程学习。

![Java](https://img.shields.io/badge/Java-17+-orange)
![Swing](https://img.shields.io/badge/Swing-GUI-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## 游戏功能

- 🌱 **植物种植**：豌豆射手、向日葵等多种植物
- 🧟 **僵尸入侵**：普通僵尸按波次进攻
- ☀️ **阳光系统**：向日葵生产阳光，种植消耗阳光
- 💥 **子弹射击**：豌豆射手自动攻击僵尸
- 🎮 **完整游戏流程**：开始界面 → 游戏进行 → 结算

## 项目结构

```
src/
├─ entity/           # 游戏实体
│   ├─ Bullet.java          # 子弹
│   ├─ NormalZombie.java    # 普通僵尸
│   ├─ PeaShooter.java      # 豌豆射手
│   ├─ Plant.java           # 植物父类
│   ├─ Sun.java             # 阳光
│   ├─ Sunflower.java       # 向日葵
│   └─ Zombie.java          # 僵尸父类
├─ factory/          # 简单工厂模式
│   └─ PlantFactory.java    # 植物工厂
├─ game/             # 游戏核心
│   ├─ GameController.java  # 游戏控制器
│   ├─ GameFrame.java       # 游戏主窗口
│   └─ GamePanel.java       # 游戏面板（绘图）
└─ utils/            # 工具类
    └─ Constants.java       # 常量定义
```

## 设计模式

- **继承**：植物/僵尸各有父类，子类扩展行为
- **简单工厂模式**：`PlantFactory` 统一创建植物对象
- **MVC 架构**：实体、视图、控制分离

## 运行方法

### 方法一：命令行编译运行

```bash
cd src
javac -encoding UTF-8 @../src_files.txt
java game.GameFrame
```

### 方法二：IDE 运行

用 IntelliJ IDEA / Eclipse 打开项目，将 `src` 设为源码根目录，直接运行 `GameFrame.java`。

### 游戏操作

1. 点击向日葵/豌豆射手卡牌选中植物
2. 点击草坪格子种植植物
3. 豌豆射手自动向右侧发射子弹
4. 向日葵定时生成阳光，收集后用于种植
5. 挡住所有僵尸即为胜利

## 技术栈

| 技术 | 说明 |
|------|------|
| Java Swing | GUI 图形界面 |
| Java AWT | 事件监听与绘图 |
| 面向对象 | 继承、封装、多态 |
| 设计模式 | 简单工厂模式 |

## License

MIT License - 详见 [LICENSE](LICENSE) 文件
