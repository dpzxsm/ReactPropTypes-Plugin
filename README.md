# React-PropTypes-Plugin-IDEA
## Preview：
![img](./ScreenShot.gif)

## 中文:

这是一个可以自动生成React组件的PropTypes代码的IDEA插件，目前仅支持ES6代码。如果需要支持ES5，请在issue中留言。

#### 安装插件
1. 在插件商店中搜索ReactPropTypes下载安装，这是<a href= https://plugins.jetbrains.com/plugin/10155-reactproptypes>商店链接</a>，欢迎评论.
2. 点击 <a href=https://raw.githubusercontent.com/dpzxsm/React-PropTypes-Plugin-IDEA/master/ReactPropTypes.jar>ReactPropTypes.jar</a> 下载插件并且打开
   IDEA Setting/Plugins/Install Plugin from disk 本地安装这个插件
   
#### 如何使用
1. 选择组件名称
2. 按下shift + alt + command + P 或者 command + N 在列表中选择PropTypesGenerate
3. 编辑弹框中的表格进行类型的修改稿

#### 提示
1. 如果你的代码中没有import的PropTypes的模块，本插件将自动optional import。  
2. 如果你的代码中已经包含对组件进行了类型检测，存在的类型将作为默认值，最后覆盖你之前的代码。
3. 目前本插件不能预测属性的具体类型，所以请在弹框中自行设置。  

## English:

This is a IDEA plug-in for automatically generates PropTypes code , which currently supports only ES6 code. If you need to support ES5, please leave a message in issue.

#### Installed
1. In plugin store search "ReactPropTypes" and install it , this is <a href= https://plugins.jetbrains.com/plugin/10155-reactproptypes>Store Link</a>, Welcome comments.
2. Click <a href=https://raw.githubusercontent.com/dpzxsm/React-PropTypes-Plugin-IDEA/master/ReactPropTypes.jar>ReactPropTypes.jar</a> to download and open
   IDEA Setting/Plugins/Install Plugin from disk to install.
   
#### How to use
1. Select your Component's name
2. Press shift + alt + command + P or command + N show generate List and select PropTypesGenerate
3. Edit the PropTypes Table to modify default type

#### Tips
1. If your code does not have the import PropTypes module, the plug-in will automatically optional import.
2. If your code already contains the type checking of the component, The existing type will be used as the default, finally overwriting your previous code.
3. Currently, this plugin cannot predict the specific type of attribute, so please set it yourself in the bomb box.