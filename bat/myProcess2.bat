D:
cd java Դ����\elasticsearch_taste1\elasticsearch_taste1

echo "es��� : es����ļ��д���"

echo "��mvn��ĿĿ¼�е�target�ļ����´���elasticsearch�ļ���"
md target\elasticsearch

echo "��elasticsearch-taste-0.0.1-SNAPSHOT.jar��plugin-descriptor.properties�ļ����Ƶ�elasticsearch�ļ�����"
copy /y target\elasticsearch-taste-0.0.1-SNAPSHOT.jar  target\elasticsearch
copy /y src\main\resources\plugin-descriptor.properties target\elasticsearch

echo "���elasticsearch�ļ��е�ѹ���ļ����ļ���Ϊtaste-5.4.0.1.zip"
winrar a -ep1 -o+ -inul -r  -iback target\taste-5.4.0.1.zip target\elasticsearch

echo "ɾ��֮ǰF�̵�taste-5.4.0.1.zip�ļ������µõ���zip�ļ����Ƶ�F��"
del F:\taste-5.4.0.1.zip
copy /y target\taste-5.4.0.1.zip  F:\

echo �ű�ִ�����