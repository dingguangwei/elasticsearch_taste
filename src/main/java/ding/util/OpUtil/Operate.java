package ding.util.OpUtil;


import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHits;

import ding.util.ESUtil.ActionParameter;
import ding.util.ESUtil.ESData;
import ding.util.WEKAUtil.WEKAbuilder;

/**
 * 该类负责由不同的参数获得不同的处理方式
 * @author xiaochenchen
 *
 */
public class Operate {
	private final Logger log = LogManager.getLogger(Operate.class);
	
	public ActionParameter parameter;
	
	
	public Operate(SearchHits ESHits, ActionParameter parameter, XContentBuilder builder) throws Exception{
		this.parameter = parameter;
		ESData esData = new ESData(ESHits, parameter);
		
		if(this.parameter.OperateCode.equals(ActionParameter.GetData_Code)){
			log.info("GetData -- Analyze & Predict");
			builder.startObject();
			
			this.writeESData(esData, builder);
			this.writeESAttribute(esData, builder);
			this.writeESNumberAttribute(esData, builder);
			this.writeESStringAttribute(esData, builder);
			
			builder.endObject();
		}
		else if(this.parameter.OperateCode.equals(ActionParameter.GetAttribute_Code)){
			log.info("GetAttribute -- Analyze & Predict");
			builder.startObject();
			this.writeESAttribute(esData, builder);
			builder.endObject();			
		}
		else if(this.parameter.OperateCode.equals(ActionParameter.GetNumberAttribute_Code)){
			log.info("GetNumberAttribute -- Analyze & Predict");
			builder.startObject();
			this.writeESNumberAttribute(esData, builder);
			builder.endObject();
		}
		else if(this.parameter.OperateCode.equals(ActionParameter.GetStringAttribute_Code)){
			log.info("GetStringAttribute -- Analyze & Predict");
			builder.startObject();
			this.writeESStringAttribute(esData, builder);
			builder.endObject();
		}
		else if(this.parameter.OperateCode.equals(ActionParameter.Train_Code)){
			log.info("Train -- Analyze & Predict");
			builder.startObject();
			new WEKAbuilder().getOutputTrain(ESHits, parameter, builder);
			builder.endObject();
		}
		else if(this.parameter.OperateCode.equals(ActionParameter.Predict_Code)){
			log.info("Predict -- Analyze & Predict");
			builder.startObject();
			this.writeESData(esData, builder);
			this.writeESAttribute(esData, builder);
			new WEKAbuilder().getOutputPredict(ESHits, parameter, builder);
			builder.endObject();
		}
		else{
			log.warn("error OperateCode(op) -- Analyze & Predict");
		}
		
	}
	
	public void writeESData(ESData esData, XContentBuilder builder) throws IOException{
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
	}
	
	public void writeESAttribute(ESData esData, XContentBuilder builder) throws IOException{
		String[] arr1 = new String[esData.attributeList.size()];
		esData.attributeList.toArray(arr1);
		builder.array("attribute", arr1);
	}
	
	public void writeESNumberAttribute(ESData esData, XContentBuilder builder) throws IOException{
		String[] arr2 = new String[esData.continuousAttributeList.size()];
		esData.continuousAttributeList.toArray(arr2);
		builder.array("numberAttribute", arr2);
	}
	
	public void writeESStringAttribute(ESData esData, XContentBuilder builder) throws IOException{
		String[] arr3 = new String[esData.discreteAttributeList.size()];
		esData.discreteAttributeList.toArray(arr3);
		builder.array("stringAttribute", arr3);
	}

}
