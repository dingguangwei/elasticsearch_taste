D:
cd java 源程序\elasticsearch_taste1\elasticsearch_taste1

echo "es插件 : es插件文件夹创建"

echo "在mvn项目目录中的target文件夹下创建elasticsearch文件夹"
md target\elasticsearch

echo "将elasticsearch-taste-0.0.1-SNAPSHOT.jar和plugin-descriptor.properties文件复制到elasticsearch文件夹下"
copy /y target\elasticsearch-taste-0.0.1-SNAPSHOT.jar  target\elasticsearch
copy /y src\main\resources\plugin-descriptor.properties target\elasticsearch

echo "添加elasticsearch文件夹到压缩文件，文件名为taste-5.4.0.1.zip"
winrar a -ep1 -o+ -inul -r  -iback target\taste-5.4.0.1.zip target\elasticsearch

echo "删除之前F盘的taste-5.4.0.1.zip文件，将新得到的zip文件复制到F盘"
del F:\taste-5.4.0.1.zip
copy /y target\taste-5.4.0.1.zip  F:\

echo 脚本执行完成