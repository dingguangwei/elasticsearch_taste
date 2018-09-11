package ding.util.WEKAUtil;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHits;

import ding.util.DrawUtil.MutliIteratorPlane;
import ding.util.ESUtil.ActionParameter;
import ding.util.ESUtil.ESData;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class WEKAbuilder {
	final Logger LOGGER = LogManager.getLogger(WEKAbuilder.class);
	ActionParameter parameter;
	
	// 包含三维过程还原
	public void getOutputTrainWith3D (SearchHits ESHits, ActionParameter parameter, XContentBuilder builder) throws Exception{
		ESData esData = new ESData(ESHits, parameter);
		this.parameter = parameter;
		String res = "";
		
		// 1、展示数据
		LOGGER.info("Info: Output The Data Set!  --Ding");
		builder.startObject();
		builder.startArray("data");
		for(int i=0; i<esData.getNumData(); i++){
			builder.startObject();
			for(int j=0; j<esData.getNumAttributes(); j++){
				try{
					// 如果数据是数字类型，json中就填写数字类型
					double temp = Double.parseDouble(esData.getData()[i][j]);
					builder.field(esData.getHeader()[j], temp);
				}
				catch(NumberFormatException e){
					builder.field(esData.getHeader()[j], esData.getData()[i][j]);
				}
			}
			builder.endObject();
		}
		builder.endArray();
		
		
		if(parameter.ClfName!=null){
    		// 2、机器学习
			ArrayList<double[]> midResult = new ArrayList<double[]>();
			LOGGER.info("Info: Runing Meachine Learning!  --Ding");
			WEKAop wekaop = new WEKAop();
    		Instances inst = wekaop.generateInstances(esData);
    		Evaluation eval= wekaop.trainThenTest(parameter.ClfName, inst, midResult);
    		res = new WEKAresult(parameter.Index, parameter.From, parameter.Size, parameter.ClfName, eval).toJSON();
    		builder.field("res");
    		this.String2Json(builder, res);
    		
    		// 三维制点
    		if(midResult.size()!=0 && esData.getHeader().length-1==3){
    			builder.field("midResult");
    			MutliIteratorPlane miPlane = new MutliIteratorPlane(midResult, esData.getHeader(), esData.getRangeOfData(), esData.getData());
    			miPlane.writeBuilder(builder);
    		}
    	}
		builder.endObject();
    }

	// 训练，存储，十折交叉验证
	public void getOutputTrain (SearchHits ESHits, ActionParameter parameter, XContentBuilder builder) throws Exception{
		ESData esData = new ESData(ESHits, parameter);
		WEKAop wekaop = new WEKAop();
		Instances instances = wekaop.generateInstances(esData);
		
		wekaop.Train(instances, parameter.ClfName, parameter.ModelWritePath);
		Evaluation eval= wekaop.crossValidation(wekaop.generateClassifier(parameter.ClfName), instances);
		String res = new WEKAresult(parameter.Index, parameter.From, parameter.Size, parameter.ClfName, eval).toJSON();
		builder.field("res");
		this.String2Json(builder, res);
	}
	public void getOutputPredict (SearchHits ESHits, ActionParameter parameter, XContentBuilder builder) throws Exception{
		ESData esData = new ESData(ESHits, parameter);
		WEKAop wekaop = new WEKAop();
		Instances instances = wekaop.generateInstances(esData);
		String[][] res = wekaop.fakePredict(instances, parameter.ModelReadPath);
		builder.array("ToF", res[0]); // 是否正确
		builder.array("predict", res[1]); // 预测结果
		builder.array("actually", res[2]); // 真实结果
		int correctNum = Integer.parseInt(res[3][0]), totalNum = Integer.parseInt(res[3][1]);
		builder.field("correctNum", correctNum);
		builder.field("totalNum", totalNum);
	}
	
	// 将String类型json格式的res变为builder，为eval的结果提供的
	public void String2Json(XContentBuilder builder, String res){
		try{
			String s = res;
			StringBuffer text = new StringBuffer();
			
			while(s.length()>0){
				if(s.charAt(0)=='{'){
					builder.startObject();
					text.append("{");
					s = s.substring(1);
				}
				else if(s.charAt(0)=='"'){
					int from = 1;
					int to = s.indexOf("\"", 2);
					String key = s.substring(from, to);
					s = s.substring(to+1);
					while(s.indexOf(0)==' ') s = s.substring(1);
					if(s.charAt(0)!=':') throw new Exception();
					s = s.substring(1);
					while(s.charAt(0)==' ') s = s.substring(1);
					// 没有value
					if(s.charAt(0)=='{'){
						builder.field(key);
						text.append("\""+key+"\":");
					}
					// value为字符串
					else if(s.charAt(0)=='"'){
						int valuefrom = 1;
						int valueto = s.indexOf("\"", 2);
						String value = s.substring(valuefrom, valueto);
						builder.field(key, value);
						s = s.substring(valueto+1);
						text.append("\""+key+"\":\""+value+"\"");
					}
					// value为数字
					else{
						int index1 = s.indexOf(",");
						int index2 = s.indexOf("}");
						if(index1==-1) index1=index2;
						else if(index2==-1) index2 = index1;
						int valueto = index1<index2?index1:index2;
						String valuestr = s.substring(0, valueto);
						double value = Double.parseDouble(valuestr);
						builder.field(key, value);
						s = s.substring(valueto);
						text.append("\""+key+"\":"+value+"");
					}
				}
				else if(s.charAt(0)==','){
					s = s.substring(1);
					text.append(",");
					while(s.charAt(0)==' ') s = s.substring(1);
					if(s.charAt(0)!='"') throw new Exception();
				}
				else if(s.charAt(0)==' '){
					s = s.substring(1);
				}
				else if(s.charAt(0)=='}'){
					builder.endObject();
					text.append("}");
					s = s.substring(1);
				}
			}
//			if(parameter.printTest)
//				System.out.println("String转builder："+text.toString());
		}catch (Exception e){
			e.printStackTrace();
			return;
		}
	}
	
	
	/*public static void main(String[] args){
		String[] header = {"a","b","c","d","Classify"};
		String[][] data = {{"1","2","1","2","m"},
				{"2","1","3","2","n"},
				{"2","4","3","5","o"},
				{"2","1","3","5","x"},
				{"2","4","4","5","y"},
				{"2","4","3","8","z"}};
		ESData esData = new ESData(header, data, "Classify", ESData.noVECTORIZE);
		WEKAop wekaop = new WEKAop();
		try {
			WEKAresult ob = new WEKAresult();
			Evaluation eval = wekaop.trainThenTest(wekaop.generateClassifier("RandomForest"), wekaop.generateInstances(esData));
			ob.eval = eval;
			System.out.println(ob.toString());
			String resjson = ob.toJSON();
			System.out.println(resjson);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}*/

}