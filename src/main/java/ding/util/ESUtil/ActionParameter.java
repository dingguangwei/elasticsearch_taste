package ding.util.ESUtil;

import java.util.*;

import org.elasticsearch.rest.RestRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 该类是解析url中调用机器学习命令的参数
 * 
 * 前缀:"http://127.0.0.1:9200/_taste/parameter?"
 * 	    http://127.0.0.1:9200/_taste/parameter?op=00&index=index-c008_03&from=0&size=200&attrSlt=1&vect=1&algo=J48&cAttr=tes_tims&delAttr=ROUGH_MEASURE_METHOD&algoPa=yy&mWpath=xx&mRpath=xx&test=xx
 * 
 * URL含参数：
 * 
 * OperateCode				-	op
 * Index					-	index
 * From, 					-	from
 * Size, 					-	size
 * vectorize, 				-	vect
 * attributeSelect, 		-	attrSlt
 * deleteAttributeSet		-	delAttr
 * ClassifyAttributeName, 	-	cAttr
 * ClfName, 				-	algo
 * AlgorithmParameter, 		-	algPara
 * ModelWritePath, 			-	mWpath
 * ModelReadPath, 			-	mRpath
 * printTest				-	test
 * @author xiaochenchen
 *
 */
public class ActionParameter {
	final Logger log = LogManager.getLogger(ActionParameter.class);
	private Map<String, String> map = new HashMap<String, String>();
	
	// 操作
	public String OperateCode=null;
	public static final String DoNothing_Code = "00";// 测试该类是否能正确获取数据
	public static final String GetData_Code = "01";// 获取数据
	public static final String GetAttribute_Code = "11";// 获取全部属性
	public static final String GetNumberAttribute_Code = "12";// 获取数值属性
	public static final String GetStringAttribute_Code = "13";// 获取离散属性
	public static final String Preprocess_Code = "21";// 数据预处理
	public static final String Train_Code = "31";// 训练
	public static final String Predict_Code = "32";// 预测
	
	// 数据
	public String Index=null;
	public int From=-1;
	public int Size=-1;
	
	// 数据处理
	public boolean vectorize = false;
	public boolean attributeSelect = false;
	public Set<String> deleteAttributeSet = new HashSet<String>();
	
	// 算法
	public String ClassifyAttributeName=null;
	public String ClfName=null;
	public String AlgorithmParameter=null;
	public String ModelWritePath=null; // 模型存储地址（训练用）
	public String ModelReadPath=null; // 模型获取地址（测试用）
	
	// 测试
	public boolean printTest = false;
	
	String splitter = "&"; // URL使用&来分隔不同参数
	
	// 绑定变量名和URL中对应的参数名
	private void initMap(){
		this.map.put("OperateCode", 			"op");
		this.map.put("Index", 					"index");
		this.map.put("From", 					"from");
		this.map.put("Size", 					"size");
		this.map.put("vectorize", 				"vect");
		this.map.put("attributeSelect", 		"attrSlt");
		this.map.put("deleteAttributeSet", 		"delAttr");
		this.map.put("ClassifyAttributeName", 	"cAttr");
		this.map.put("ClfName", 				"algo");
		this.map.put("AlgorithmParameter", 		"algoPa");
		this.map.put("ModelWritePath", 			"mWpath");
		this.map.put("ModelReadPath", 			"mRpath");
		this.map.put("printTest", 				"test");
	}
	
	// 通过RestRequest，可以直接获得URL里面的参数
	public ActionParameter (RestRequest request){
		this.initMap();
		try{
			if((this.OperateCode=request.param(map.get("OperateCode")))==null){
				throw new Exception();
			}
			else{
				this.getRequestValue(request); // 获取request中各个参数的值
				
				if(this.OperateCode.equals(ActionParameter.DoNothing_Code)){
					this.initDoNothing();
				}
				else if(this.OperateCode.equals(ActionParameter.GetData_Code)){
					this.initGetData();
				}
				else if(this.OperateCode.equals(ActionParameter.GetAttribute_Code)){
					this.initGetAttribute();
				}
				else if(this.OperateCode.equals(ActionParameter.GetNumberAttribute_Code)){
					this.initGetNumberAttribute();				
				}
				else if(this.OperateCode.equals(ActionParameter.GetStringAttribute_Code)){
					this.initGetStringAttribute();
				}
				else if(this.OperateCode.equals(ActionParameter.Train_Code)){
					this.initTrain();
				}
				else if(this.OperateCode.equals(ActionParameter.Predict_Code)){
					this.initPredict();
				}
				else{
					throw new Exception();
				}
			}
		}catch (Exception e){
			log.warn("> class ActionParameter: URL problem [must contain correct parameter]  --dgw");
			e.printStackTrace();
		}
	}
	
	// 以下的每一个init函数，对应每一个操作的初始化
	private void initGetData() throws Exception{
		if(this.Index==null)
			throw new Exception();
		if(this.From==-1&&this.Size==-1){
			this.From=0;
			this.Size=1;
		}
	}
	
	private void initGetAttribute() throws Exception{
		if(this.Index==null)
			throw new Exception();
		else{
			this.From=0;
			this.Size=1;
		}
	}
	
	private void initGetNumberAttribute() throws Exception{
		if(this.Index==null)
			throw new Exception();
		else{
			this.From=0;
			this.Size=1;
		}
	}
	
	private void initGetStringAttribute() throws Exception{
		if(this.Index==null)
			throw new Exception();
		else{
			this.From=0;
			this.Size=1;
		}
	}
	
	private void initTrain() throws Exception{
		if(this.Index==null||this.From==-1||this.Size==-1||this.ClassifyAttributeName==null||this.ClfName==null)
			throw new Exception();
	}
	
	private void initPredict() throws Exception{
		if(this.Index==null||this.From==-1||this.Size==-1||this.ModelReadPath==null)
			throw new Exception();
	}
	
	private void initDoNothing(){
		System.out.println("OperateCode="+this.OperateCode+" op");
		System.out.println("Index="+this.Index+" index");
		System.out.println("From="+this.From+" from");
		System.out.println("Size="+this.Size+" size");
		System.out.println("vectorize="+this.vectorize+" vect");
		System.out.println("attributeSelect="+this.attributeSelect+" attrSlt");
//		System.out.println("deleteAttributeSet="+this+" delAttr");
		System.out.println("ClassifyAttributeName="+this.ClassifyAttributeName+" cAttr");
		System.out.println("ClfName="+this.ClfName+" algo");
		System.out.println("AlgorithmParameter="+this.AlgorithmParameter+" algoPa");
		System.out.println("ModelWritePath="+this.ModelWritePath+" mWpath");
		System.out.println("ModelReadPath="+this.ModelReadPath+" mRpath");
		System.out.println("printTest="+this.printTest+" test");
	}
	
	
	private void getRequestValue(RestRequest request){
		//------------------数据--------------------
		this.Index=request.param(map.get("Index"));
		if(request.param("from")!=null){
			this.From = Integer.parseInt(request.param(map.get("From")));
		}
		if(request.param("size")!=null){
			this.Size = Integer.parseInt(request.param(map.get("Size")));
		}
		//------------------数据处理：向量化--------------------
		if(request.param(map.get("vectorize"))!=null && request.param(map.get("vectorize")).equals("1"))
			this.vectorize = true;
		else
			this.vectorize = false;
		//------------------数据处理：属性选择--------------------
		if(request.param(map.get("attributeSelect"))!=null && request.param(map.get("attributeSelect")).equals("1"))
			this.attributeSelect = true;
		else
			this.attributeSelect = false;
		//------------------数据处理：属性删除--------------------
		if(request.param(map.get("deleteAttributeSet"))!=null && request.param(map.get("deleteAttributeSet")).length()!=0){
			String[] s = request.param(map.get("deleteAttributeSet")).split(",");
			for(int i=0; i<s.length; i++){
				this.deleteAttributeSet.add(s[i]);
			}
		}
		//------------------测试--------------------
		if(request.param(map.get("printTest"))!=null && request.param(map.get("printTest")).equals("1"))
			this.printTest = true;
		else
			this.printTest = false;
		
		//------------------算法--------------------
		this.ClfName = request.param(map.get("ClfName"));
		if(this.ClfName==null || this.ClfName.length()==0)
			this.ClfName=null;
		this.AlgorithmParameter=request.param(map.get("AlgorithmParameter"));
		this.ClassifyAttributeName=request.param(map.get("ClassifyAttributeName"));
		
		this.ModelWritePath = request.param(map.get("ModelWritePath"));
		this.ModelReadPath = request.param(map.get("ModelReadPath"));
	}
	
	
	// 从request中解析出字符串参数action，然后再依次解析每个参数（已废弃，不推荐使用）
	public ActionParameter (String action){
		try{
			// index=c008&from=0&size=10&classifyAttributeName=tes_tims
			String[] a = action.split(this.splitter);
			Map<String, String> map = new HashMap<String, String>();
			for(int i=0; i<a.length; i++){
				String[] temp = a[i].split("=");
				if(a[i].indexOf("=")==-1 || temp.length!=2)
					throw new Exception();
				else
					map.put(temp[0], temp[1]);
			}
			if(map.containsKey("index")){
				this.Index = map.get("index");
			}
			if(map.containsKey("from")){
				this.From = Integer.parseInt(map.get("from"));
			}
			if(map.containsKey("size")){
				this.Size = Integer.parseInt(map.get("size"));
			}
			if(map.containsKey("clfName")){
				
				this.ClfName = map.get("clfName");
			}
			if(map.containsKey("classifyAttributeName")){
				this.ClassifyAttributeName = map.get("classifyAttributeName");
			}
			if(map.containsKey("printTest")){
				this.ClassifyAttributeName = map.get("classifyAttributeName");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
