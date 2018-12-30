一、es软件版插件安装方法：
（1）在该项目主目录（./）下打开命令行，安装（编译）该项目：mvn clean install compile -Dmaven.test.skip=true
（2）./target文件夹中存放了编译好的文件。在./target目录下新建./target/elasticsearch文件夹，然后将target\elasticsearch-taste-0.0.1-SNAPSHOT.jar和src\main\resources\plugin-descriptor.properties这两个文件移到./target/elasticsearch中。将./target/elasticsearch文件夹打包成zip文件
（3）如果之前安装过同名plugin，需要卸载，在es源码项目中打开elasticsearch-5.4.1\bin，执行：elasticsearch-plugin remove demo
（4）安装插件，cd elasticsearch-5.4.1\bin；  elasticsearch-plugin install file:F:/taste-5.4.0.1.zip

二、es源码版插件安装方法：
（1）（2）同上
（3）复制./target/elasticsearch文件夹下的两个文件，粘贴至E:\elasticsearch-5.4.1(1)\elasticsearch-5.4.1\core\src\main\plugins\taste\
PS：具体目录根据自己安装情况而定

三、本项目./bat文件夹下存放了软件版安装方法的脚本文件，步骤一一对应（除了第5步），可供参考

