package ding.util.WEKAUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ding.util.DrawUtil.drawScatterAndLine2D;
import ding.util.ESUtil.ActionParameter;
import ding.util.ESUtil.ESData;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WEKAop {
	
	/**
	 * 根据算法名称，返回算法模型实例
	 * @param algorithmName 算法名称
	 * @return 模型实例
	 */
	public Classifier generateClassifier(String algorithmName){
		Classifier cls = null;
		try{
			if(algorithmName.equals("ZeroR"))
				cls = new ZeroR();
			else if(algorithmName.equals("BayesNet"))
				cls = new BayesNet();
			else if(algorithmName.equals("NaiveBayes"))
				cls = new NaiveBayes();
			else if(algorithmName.equals("Logistic"))
				cls = new Logistic();
			else if(algorithmName.equals("MultilayerPerceptron"))
				cls = new MultilayerPerceptron();
			else if(algorithmName.equals("SMO"))
				cls = new SMO();
			else if(algorithmName.equals("AdaBoostM1"))
				cls = new AdaBoostM1();
			else if(algorithmName.equals("AttributeSelectedClassifier"))
				cls = new AttributeSelectedClassifier();
			else if(algorithmName.equals("Bagging"))
				cls = new Bagging();
			else if(algorithmName.equals("J48"))
				cls = new J48();
			else if(algorithmName.equals("RandomForest"))
				cls = new RandomForest();
			else if(algorithmName.equals("RandomTree"))
				cls = new RandomTree();
			else{
				throw new Exception();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return cls;
	}
	
	/**
	 * 对一个模型进行交叉验证
	 * @param clf
	 * @param instances
	 * @throws Exception 
	 */
	public Evaluation crossValidation(Classifier clf, Instances instances) throws Exception{
		Evaluation eval = new Evaluation(instances);
		try{
			Random rand = new Random(1);
			int fold = 3;
			eval.crossValidateModel(clf, instances, fold, rand);//使用crossValidateModel时，分类器不需要先训练
		}catch(Exception e){
			e.printStackTrace();
		}
		return eval;
	}

	/**
	 * 使用训练集对训练模型进行测试
	 * @param clfName
	 * @param trainSet
	 * @param midResult 返回迭代参数，如果改写了函数的话
	 * @return
	 * @throws Exception
	 */
	public Evaluation trainThenTest(String clfName, Instances trainSet, ArrayList<double[]> midResult) throws Exception{
		// train
		trainSet.setClassIndex(trainSet.numAttributes() - 1);
		
		Evaluation eval = new Evaluation(trainSet);
		
		if(clfName.equals("Logistic")){
			Logistic clf = new Logistic();
			clf.buildClassifier(trainSet, midResult);
			for(int i=0; i<trainSet.numInstances(); i++){
				eval.evaluateModelOnceAndRecordPrediction(clf, trainSet.instance(i));
				System.out.println(Arrays.toString(trainSet.instance(i).toDoubleArray()));
				
//				System.out.println(Arrays.toString());;
			}
			this.printEval(eval);
			
		}
		else{
			Classifier clf = this.generateClassifier(clfName);
			clf.buildClassifier(trainSet);
			for(int i=0; i<trainSet.numInstances(); i++){
				eval.evaluateModelOnceAndRecordPrediction(clf, trainSet.instance(i));
//				double temp = eval.evaluateModelOnceAndRecordPrediction(clf, trainSet.instance(i));
			}
			this.printEval(eval);
		}
		return eval;
	}
	
	// 训练模型（存储）
	public void Train(Instances instances, String algorithmName, String modelPath) throws Exception{
		// create Classifier
		Classifier cls;
		cls = this.generateClassifier(algorithmName);
		
		// train
		cls.buildClassifier(instances);
		
		// serialize model
		if(modelPath!=null && modelPath.length()!=0){
			weka.core.SerializationHelper.write(modelPath, cls);//"/some/where/j48.model"
			System.out.println("存储训练模型："+modelPath);
		}
	}
	
	// 假性预测（已知分类结果）
	public String[][] fakePredict(Instances instances, String savedModelPath) throws Exception{
		String[][] res = new String[4][instances.numInstances()];
		Classifier cls = (Classifier) weka.core.SerializationHelper.read(savedModelPath);
		Evaluation eval = new Evaluation(instances);
		//System.out.println("id : [ToF, predict, actually]");
		int count = 0;
		for(int i=0; i<instances.numInstances(); i++){
			double temp = eval.evaluateModelOnceAndRecordPrediction(cls, instances.instance(i));
			
			res[1][i] = String.valueOf(temp);
			res[2][i] = String.valueOf(instances.instance(i).classValue());
			if(instances.instance(i).classValue()!=temp){
				res[0][i] = "false";
			}
			else{
				count++;
				res[0][i] = "true";
			}
			//System.out.println(i+" : ["+res[0][i]+", "+res[1][i]+", "+res[2][i]+"]");
		}
		res[3] = new String[2];
		res[3][0] = String.valueOf(count);
		res[3][1] = String.valueOf(instances.numInstances());
		return res;
	}
	
	public void printEval(Evaluation eval){
		try {
			System.out.println(eval.toClassDetailsString());
			System.out.println(eval.toSummaryString("=== Summary Accurcy ===", false));
			System.out.println(eval.toMatrixString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * ESData -> Instances类
	 * @param esData
	 * @param classIndex 分类属性索引，标号从0开始
	 * @return
	 */
	public Instances generateInstances(ESData esData, int classIndex){
		String[] header = esData.getHeader();
		String[][] data = esData.getData();
		// 1、创建属性，属性数据为numeric类型，分类数据为String类型
		ArrayList<Attribute> attributes = new ArrayList<>();
		Attribute classifyAttribute=null;
		for(int i=0; i<header.length; i++){
			if(classIndex!=i){
				Attribute attribute = new Attribute(header[i]);
				attributes.add(attribute);
			}
			else{
				classifyAttribute = new Attribute(header[i], esData.getClassify(classIndex));
				attributes.add(classifyAttribute);
			}
		}
		// 2、初始化Instances数据集实例
		Instances instances = new Instances("instances",attributes,0);
		instances.setClassIndex(classIndex); // 设置分类标志
		// 3、添加数据
		for(int i=0; i<data.length; i++){
			Instance instance = new DenseInstance(attributes.size());
			for(int j=0; j<data[0].length; j++){
				if(classIndex!=j){
					try{
						instance.setValue(j,Double.parseDouble(data[i][j]));
					}catch(Exception e){
						e.printStackTrace();
						Logger LOGGER = LogManager.getLogger(WEKAop.class);
						LOGGER.warn("Warn: ESData went wrong in class WEKAop! --Ding");
					}
				}
				else{
					instance.setValue(classifyAttribute,data[i][j]);
				}
				
			}
			instances.add(instance);
		}
		return instances;
	}

	/**
	 * 默认设置最后一列为分类位
	 * @param esData
	 * @return
	 */
	public Instances generateInstances(ESData esData){
		
		return this.generateInstances(esData, esData.getNumAttributes() - 1);
	}
	
	public void test1(){
		String[] header = {"a","b","Classify"};
		String[][] data = {{"1","2","0"},
				{"3","2.3","0"},
				{"2","2.5","0"},
				{"1","5","1"},
				{"2","4","1"},
				{"3","3.4","1"}};
		ESData esData = new ESData(header, data, "Classify", ESData.noVECTORIZE );
		try {
			ArrayList<double[]> midResult = new ArrayList<double[]>();
			this.trainThenTest("Logistic", this.generateInstances(esData), midResult);
			System.out.println("\n________________________________________");
			for(double[] temp : midResult){
				System.out.println(Arrays.toString(temp));
			}
			System.out.println("________________________________________");
			
			
			double[][][] data2d = new double[2][3][2];
			for(int i=0; i<data2d[0].length; i++)
				for(int j=0; j<data2d[0][i].length; j++)
					data2d[0][i][j] = Double.parseDouble(data[i][j]);
			for(int i=0; i<data2d[0].length; i++)
				for(int j=0; j<data2d[0][i].length; j++)
					data2d[1][i][j] = Double.parseDouble(data[i+3][j]);
			
			double[][] lineCoefficient = new double[midResult.size()-2][];
			for(int i=0; i<lineCoefficient.length; i++) 
				lineCoefficient[i] = midResult.get(i+2);
			String[] attrName = new String[midResult.size()+2];
			attrName[0] = "a";
			attrName[1] = "b";
			for(int i=2; i<attrName.length; i++) attrName[i] = "m"+i;
			drawScatterAndLine2D draw = new drawScatterAndLine2D(data2d, attrName, lineCoefficient, "demo", true);
			draw.draw();
			new Thread(draw).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void test2() throws IOException{
		FileReader fr = new FileReader("F:/bc/iris/mydata-3-2.csv");
		BufferedReader br = new BufferedReader(fr);
		br.readLine();
		
		int N = 150;
		String line;
		int lineNum = 0;
		List<String[]> list = new ArrayList<String[]>();
		while(lineNum<N && (line = br.readLine()) != null){
			list.add(line.split(","));
			lineNum++;
		}
		br.close();
		fr.close();
		
		String[] header = {"a","b","Classify"};
		String[][] data = new String[list.size()][];
		for(int i=0; i<data.length; i++) data[i]=list.get(i);
		ESData esData = new ESData(header, data, "Classify", ESData.noVECTORIZE );
		ArrayList<double[]> midResult = new ArrayList<double[]>();
		try {
			this.trainThenTest("Logistic", this.generateInstances(esData), midResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n________________________________________");
		for(double[] temp : midResult){
			System.out.println(Arrays.toString(temp));
		}
		System.out.println("________________________________________");
		
		this.test2draw(midResult, data);
	}
	
	public void test2draw(ArrayList<double[]> midResult, String[][] data){
		Map<String, Integer> map1 = new HashMap<String, Integer>(); // 属性->索引
		Map<Integer, Integer> map2 = new HashMap<Integer, Integer>(); // 索引->个数
		int count = 0;
		for(int i=0; i<data.length; i++){
			if(!map1.containsKey(data[i][2])){
				map1.put(data[i][2], count);
				map2.put(count, 1);
				count++;
			}
			else{
				int index = map1.get(data[i][2]);
				map2.replace(index, map2.get(index)+1);
			}
		}
		
		double[][][] data2d = new double[map1.size()][][];
		
		for(int i=0; i<data2d.length; i++){
			data2d[i] = new double[map2.get(i)][2];
//			for(int j=0; j<data2d[i].length; j++){
//				for(int k=0; k<data2d[i][j].length; k++){
//					data2d[i][j][k] = Double.parseDouble(data[inum][k]);
//				}
//				inum++;
//			}
		}
		
		int[] num = new int[data2d.length];
		for(int i=0; i<data.length; i++){
			int index = map1.get(data[i][2]);
			for(int j=0; j<data2d[index][num[index]].length; j++)
				data2d[index][num[index]][j] = Double.parseDouble(data[i][j]);
			num[index]++;
		}
		
		double[][] lineCoefficient = new double[midResult.size()-2][];
		for(int i=0; i<lineCoefficient.length; i++) 
			lineCoefficient[i] = midResult.get(i+2);
		String[] attrName = new String[midResult.size()+2];
		attrName[0] = "a";
		attrName[1] = "b";
		for(int i=2; i<attrName.length; i++) attrName[i] = "m"+i;

		// 当dynamic设置为true时，调用draw仅展示第一次迭代的分类平面，启动对象多线程时才可以动态展示
		drawScatterAndLine2D draw = new drawScatterAndLine2D(data2d, attrName, lineCoefficient, "demo", true);
		draw.draw();
		new Thread(draw).start();
	}
	
	public static void main(String[] args) throws IOException{
		WEKAop ob = new WEKAop();
//		ob.test1();
		ob.test2();
	}
	
}
