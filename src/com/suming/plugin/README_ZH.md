## 预览图：
![img](./ScreenShot.gif)

这是一个可以自动生成React组件的PropTypes代码的jetbrains插件，目前仅支持ES6代码。如果需要支持ES5，请在issue中留言。支持的平台有:IntelliJ IDEA、PhpStorm、WebStorm、PyCharm、RubyMine、AppCode、CLion、Gogland、Rider

#### 安装插件
1. 在插件商店中搜索ReactPropTypes下载安装，这是<a href= https://plugins.jetbrains.com/plugin/10155-reactproptypes>商店链接</a>，欢迎评论.
2. 点击 <a href=https://raw.githubusercontent.com/dpzxsm/ReactPropTypes-Plugin-Intellij/master/ReactPropTypes.jar>ReactPropTypes.jar</a>(最新版本，但可能不太稳定) 下载插件并且打开Setting/Plugins/Install Plugin from disk 本地安装这个插件
   
#### 如何使用
1. 选择组件名称
2. 按下shift + alt + command + P 或者 command + N 在列表中选择PropTypesGenerate
3. 编辑弹框中的表格进行类型的修改稿

#### 提示
1. 如果你的代码中没有import的PropTypes的模块，本插件将自动optional import。  
2. 如果你的代码中已经包含对组件进行了类型检测，存在的类型将作为默认值，最后覆盖你之前的代码。
3. 目前本插件不能预测属性的具体类型，所以请在弹框中自行设置。  