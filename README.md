# ReactPropTypes-Plugin
 <a href=https://github.com/dpzxsm/ReactPropTypes-Plugin/blob/master/README_ZH.md>中文文档</a>
 
## Preview：
![img](./ScreenShot.gif)

This is a JetBrains plug-in that automatically generates PropTypes code for React components, and only supports ES6 code at the moment. If you need to support ES5, please leave a message in issue.Compatible with: IntelliJ IDEA, PhpStorm, WebStorm, PyCharm, RubyMine, AppCode, CLion, Gogland, Rider.

#### Installed
1. In plugin store search "ReactPropTypes" and install it , this is <a href= https://plugins.jetbrains.com/plugin/10155-reactproptypes>Store Link</a>, Welcome comments.
2. Click <a href=https://raw.githubusercontent.com/dpzxsm/ReactPropTypes-Plugin-Intellij/master/ReactPropTypes.jar>ReactPropTypes.jar</a>(Recently, but may Unstable) to download and open Setting/Plugins/Install Plugin from disk to install.
   
#### How to use
1. Select your Component's name
2. Press shift + alt + command + P or command + N show generate List and select PropTypesGenerate
3. Edit the PropTypes Table to modify default type

#### Tips
1. If your code does not have the import PropTypes module, the plug-in will automatically optional import.
2. If your code already contains the type checking of the component, The existing type will be used as the default, finally overwriting your previous code.
3. Currently, this plugin cannot predict the specific type of attribute, so please set it yourself in the bomb box.