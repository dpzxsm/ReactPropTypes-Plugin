# React-PropTypes-Plugin-IDEA
## Preview：
![img](./ScreenShot.gif)

## Download Url
<a href=https://raw.githubusercontent.com/dpzxsm/React-PropTypes-Plugin-IDEA/master/ReactPropTypes.jar>ReactPropTypes.jar</a>

## 中文:

这是一个可以自动生成React组件的PropTypes代码的IDEA插件，目前仅支持ES6代码。如果需要支持ES5，请在issue中留言。

#### 如何使用
1. 选择组件名称
2. 按下shift + alt + command + P 或者 command + N 在列表中选择PropTypesGenerate
3. 编辑弹框中的表格进行类型的修改稿

#### 提示
1. 如果你的代码中没有import的PropTypes的模块，本插件将自动optional import。  
2. 如果你的代码中已经包含对组件进行了类型检测，本插件将覆盖你之前的代码。
3. 目前本插件不能预测属性的具体类型，所以请在弹框中自行设置。  

## English:

This is a IDEA plug-in for automatically generates PropTypes code , which currently supports only ES6 code. If you need to support ES5, please leave a message in issue.

#### How to use
1. Select your Component's name
2. Press shift + alt + command + P or command + N show generate List and select PropTypesGenerate
3. edit the PropTypes Table to modify default type

#### Tips
1. if your code does not have the import PropTypes module, the plug-in will automatically optional import.
2. if your code already contains the type checking of the component, the plug-in will overwrite the code before you.
3. currently, this plugin cannot predict the specific type of attribute, so please set it yourself in the bomb box.