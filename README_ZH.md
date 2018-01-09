这是一个可以自动生成React组件的PropTypes代码的jetbrains插件，目前仅支持ES6、ES7。如果需要支持ES5，请在issue中留言。支持的平台有:IntelliJ IDEA、PhpStorm、WebStorm、PyCharm、RubyMine、AppCode、CLion、Gogland、Rider

## 安装插件
1. 在插件商店中搜索ReactPropTypes下载安装，这是<a href= https://plugins.jetbrains.com/plugin/10155-reactproptypes>商店链接</a>，欢迎评论.
2. 点击 <a href=https://raw.githubusercontent.com/dpzxsm/ReactPropTypes-Plugin-Intellij/master/ReactPropTypes.jar>ReactPropTypes.jar</a>(最新版本，但可能不太稳定) 下载插件并且打开Setting/Plugins/Install Plugin from disk 本地安装这个插件
   
## 如何使用
1. 选择组件名称
2. 按下 command + N (Windows系统是alt + insert) 并且选择PropTypesGenerate, 或者按下shift + command + alt + P (Windows系统是shift + ctrl + alt + P) 在Mac系统中。
3. 编辑弹框中的表格进行类型的修改稿

## 预览图
![img](./ScreenShots/ScreenShot1.gif)
![img](./ScreenShots/ScreenShot2.gif)

## 特性(更新到1.0.7)
1. 如果你没有选择任何文字，插件将自动找到高亮的文字作为选择的组件名称
1. 在ES6的标准组件中，插件通过找到以props和nextProps为对象名称的引用和解析赋值来找到属性名<br>
2. 在无状态组件中，只有当你的第一个参数命名为props或者是一个解析赋值的参数样式，插件才能识别出来<br>
3. 如果你选择了ES6的代码风格，代码将生产在当前文件的最后一行。当然，如果你选择了ES7的代码风格, 代码将在你所选择的组件内部的第一行生成。
4. 如果自动生成的名字不是你期盼的那样，你可以在表格中双击名字进行修改，当然也可以手动添加一行或者删除你不需要的。
5. 如果你的组件中含有默认值的props, 插件会读取默认值的类型填充到最终表单之中 。
6. 表单中增加了describe项，用于显示props的来源信息
