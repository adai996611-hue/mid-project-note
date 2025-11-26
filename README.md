# Android NotePad 应用功能扩展实验

## 1. 项目概述
本项目是基于 Google Android SDK 提供的 NotePad 示例代码进行的二次开发。在保留原有功能的基础上，完成了从 Support 库到 AndroidX 的迁移，并根据实验要求扩展了搜索、时间戳显示等基础功能，同时对 UI 进行了卡片化美化，增强了用户交互体验。

## 2. 开发环境
*   **迁移库**：AndroidX (AppCompat, Material Design, ConstraintLayout)
*   **Compile SDK**：34 (Android 14)
*   **Min SDK**：24 (Android 7.0)

## 3. 功能实现列表

### 3.1 基础功能 (Basic Features)
*   **时间戳显示**：
    *   修改数据库查询投影，获取 `COLUMN_NAME_MODIFICATION_DATE` 字段。
    *   在列表项中增加 TextView，并将时间戳格式化为 `yyyy-MM-dd HH:mm:ss` 显示。
*   **笔记搜索**：
    *   在菜单栏集成 `SearchView`。
    *   实现了根据笔记标题的模糊查询功能，支持实时过滤列表显示。

### 3.2 UI 界面美化 (UI Improvements)
*   **卡片式列表 (CardView)**：
    *   重写列表项布局 `noteslist_item.xml`，使用 `CardView` 替换原有的线性布局。
    *   实现了圆角卡片样式，并为卡片底部添加了黑色边框装饰，提升视觉层级。
*   **悬浮动作按钮 (FAB)**：
    *   使用 `CoordinatorLayout` 重构主界面布局。
    *   添加 Material Design 风格的悬浮按钮 (Floating Action Button) 用于新建笔记，替代旧版菜单栏的添加方式。

### 3.3 高级扩展功能 (Extended Features)
*   **笔记排序**：
    *   支持按“最后修改时间”进行升序或降序排列。
*   **上下文菜单增强**：
    *   **分享笔记**：长按笔记可调用系统分享接口，将笔记内容发送至其他应用。
    *   **查看详情**：长按可查看笔记的字数统计、完整创建/修改时间等信息。
*   **剪贴板粘贴**：
    *   重写了粘贴逻辑。点击粘贴时，自动读取系统剪贴板内容并新建一条笔记，避免了原版代码导致的跳转崩溃。

### 3.4 交互体验优化 (UX & Stability)
*   **防误删机制**：
    *   拦截删除操作，弹出 `AlertDialog` 确认提示框，用户确认后才执行删除。
*   **稳定性修复**：
    *   **显式跳转修复**：将列表点击和新建笔记的 Intent 由隐式改为显式 (`new Intent(Context, Class)`)，彻底解决了点击笔记或按钮导致 App 闪退 (`ActivityNotFoundException`) 的问题。
    *   **继承类调整**：将 `NotesList` 和 `NoteEditor` 改为继承 `AppCompatActivity`，确保了应用在现代 Android 系统上的兼容性。
