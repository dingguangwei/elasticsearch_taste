echo "将源码版es中plugin目录下的taste文件夹删除，复制刚才在软件版es的plugin文件夹下安装得到的taste文件夹"
rd /s /Q E:\elasticsearch-5.4.1(1)\elasticsearch-5.4.1\core\src\main\plugins\taste
md E:\elasticsearch-5.4.1(1)\elasticsearch-5.4.1\core\src\main\plugins\taste

xcopy E:\elasticsearch-5.4.1\plugins\taste E:\elasticsearch-5.4.1(1)\elasticsearch-5.4.1\core\src\main\plugins\taste\ /e

