植物大战僵尸 Java Swing 实现
使用 Java Swing 实现的简易植物大战僵尸桌面小游戏，练习面向对象、工厂模式、GUI 图形编程。
项目介绍
本项目基于 Java Swing 开发，实现植物大战僵尸基础游戏 demo。采用面向对象思想，使用继承、简单工厂模式，将游戏实体、视图、业务逻辑分层解耦。
目录结构
plaintext
src
├─ entity          // 游戏实体对象
│  ├─ Bullet.java         // 子弹
│  ├─ NormalZombie.java   // 普通僵尸
│  ├─ PeaShooter.java     // 豌豆射手
│  ├─ Plant.java          // 植物父类
│  ├─ Sun.java            // 阳光
│  ├─ Sunflower.java      // 向日葵
│  └─ Zombie.java         // 僵尸父类
├─ factory         // 简单工厂
│  └─ PlantFactory.java   // 植物工厂，创建植物实例
├─ game            // 游戏核心
│  ├─ GameController.java // 游戏逻辑控制器：碰撞检测、对象更新
│  ├─ GameFrame.java      // 程序主窗口入口
│  └─ GamePanel.java      // 绘图面板，负责画面渲染
└─ utils           // 工具与常量
   └─ Constants.java      // 全局常量：窗口尺寸、血量、坐标等
环境要求
JDK 8 及以上
支持 Swing 图形库（JDK 自带，无需额外引入依赖）
运行方式
将项目导入 IDEA / Eclipse
找到 game.GameFrame 类，运行 main 方法启动游戏
弹出游戏窗口，即可开始体验
已实现功能
✅ 植物：向日葵、豌豆射手
✅ 僵尸：普通僵尸
✅ 子弹发射与移动
✅ 阳光生成
✅ 游戏画面循环渲染
✅ 使用简单工厂模式创建植物对象
✅ 父类抽取，减少重复代码
后续可拓展
增加更多植物、僵尸种类
实现阳光收集、植物种植交互
完善碰撞伤害逻辑
增加游戏结束判定、分数统计
开源
MIT License
