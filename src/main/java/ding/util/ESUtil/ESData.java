package ding.util.ESUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.search.SearchHits;

/**
 * 将hits中的数据取出，转换成String类型
 * 属性名称：Header
 * 属性值：Data（前n-1列为可转化为double类型的String，最后一列为类别值
 * @author xiaochenchen
 *
 */
public class ESData {
	
	private String[] Header;				// 头
	private String[][] Data;    			// 数据(最后一列是类别)
	private int NumData;        			// 数据条数
	private int NumAttributes;  			// 属性个数
	private Set<String> deleteAttributeSet; // 待删除的属性名称列表
	private String ClassifyAttributeName;   // 分类属性名称
	
	private int hitsFrom = -1;
	private int hitsSize = -1;
	
	public ArrayList<String> attributeList = new ArrayList<String>(); // 全部属性名称
	public ArrayList<String> continuousAttributeList = new ArrayList<String>();// 若该列全部为数值型数据，不包含空值、字符型数据和其他数据，则为连续性属性
	public ArrayList<String> discreteAttributeList = new ArrayList<String>(); // 若某一列中，有数据不能转化为double，将其属性归类为离散属性discreteAttribute
	private Map<String, Integer> AttributeIndexMap = new HashMap<String, Integer>(); // 离散属性对应原数组中的索引
	
	
	public static boolean VECTORIZE = true; // 进行归一化
	public static boolean noVECTORIZE = false;
	
	public static String DISCRETENULLVALUE = "#$#"; // 代表离散属性值为空
	public static String CONTINUOUSNULLVALUE = "0.5"; // 连续属性中，出现空值，将其设为平均数，0.5
	public static double MAXNUMBER = 99999999;
	public static double MINNUMBER = -99999999;
	
	// 测试
	private boolean printTest = false;
	
	public String[] getHeader(){
		return this.Header;
	}
	public String[][] getData(){
		return this.Data;
	}
	public int getNumData(){
		return NumData;
	}
	public int getNumAttributes(){
		return NumAttributes;
	}
	/**
	 * 将hits转换为header和data
	 * @param hits
	 * @param size
	 * @param classifyAttributeName 分类属性名称
	 */
	public ESData(SearchHits hits, ActionParameter parameter){
		deleteAttributeSet = new HashSet<String>();
		this.deleteAttributeSet.add("path");				// 默认删除文件中的path属性值
		this.deleteAttributeSet.add("CONSIGN_USER_NAME");
		this.deleteAttributeSet.add("FIN_USER_NAME");
		this.deleteAttributeSet.add("WORK_SPECIAL_REQ_24");
		this.deleteAttributeSet.add("WORK_SPECIAL_REQ_27");
		this.deleteAttributeSet.add("WORK_SPECIAL_REQ_28");
		this.deleteAttributeSet.add("WORK_SPECIAL_REQ_29");
		this.deleteAttributeSet.add("WORK_SPECIAL_REQ_2G");
		this.deleteAttributeSet.add("WORK_SPECIAL_REQ_2H");
		this.deleteAttributeSet.add("FIN_USER_NAME");
		this.deleteAttributeSet.add("WORK_SPECIAL_REQ_2H");
		this.deleteAttributeSet.add("message");
		this.deleteAttributeSet.add("ord_nutj");
		
		// 删除指定属性
		if(parameter.deleteAttributeSet.size()!=0){
			Iterator<String> iter = parameter.deleteAttributeSet.iterator();
			while(iter.hasNext()){
				this.deleteAttributeSet.add(iter.next());
			}
		}
		
		this.ClassifyAttributeName = parameter.ClassifyAttributeName; // 设置分类属性，c008中是"tes_tims" 
		
		this.printTest = parameter.printTest;
		
		this.hitsFrom = parameter.From;
		this.hitsSize = parameter.Size;
		this.toHeader(hits);
		int M = parameter.Size, N = this.Header.length;
		this.NumData = M;
		this.NumAttributes = N;
		this.Data = new String[M][N];
		this.toData(hits);
		
//		this.show();
		
		if(parameter.attributeSelect) this.doAttributeSelect();
//		this.show();
		
		if(parameter.vectorize) this.doVectorize();
		
		//所有操作完成后，获取各类属性
		for(int i=0; i<this.Header.length; i++){
			this.attributeList.add(this.Header[i]);
			if( !this.discreteAttributeList.contains(this.Header[i]) ){
				this.continuousAttributeList.add(this.Header[i]);
			}
		}
		
		if(this.printTest)
			this.show();
	}
	
	// 仅作单元测试用
	public ESData(String[] header, String[][] data, String classifyAttributeName, boolean vectorize){
		this.Header = header;
		this.Data = data;
		this.NumData = this.Data.length;
		this.NumAttributes = this.Data[0].length;
		this.ClassifyAttributeName = classifyAttributeName;
		this.discreteAttributeList.add("n3");
		this.discreteAttributeList.add("n5");
		this.discreteAttributeList.add("n6");
		this.AttributeIndexMap.put("n3", 2);
		this.AttributeIndexMap.put("n5", 4);
		this.AttributeIndexMap.put("n6", 5);
		if(vectorize){
			this.doVectorize();
		}
		
	}
	
	private void show(){
		System.out.println("#########################################################################################");
		System.out.println("删除属性："+Arrays.toString(this.deleteAttributeSet.toArray()));
		for(int i=0; i<this.Header.length; i++){
			System.out.print(String.format("\t%40d", i));
		}
		System.out.println();
		for(int i=0; i<this.Header.length; i++){
			System.out.print(String.format("\t%40s", Header[i]));
		}
		System.out.println();
		for(int i=0; i<this.Data.length; i++){
			for(int j=0; j<this.Data[0].length; j++){
				System.out.print(String.format("\t%40s", Data[i][j]));
			}
			System.out.println();
		}
		System.out.println("#########################################################################################");
		
	}
	
	/** 
	 * 返回data的第lineNum行
	 * @param lineNum
	 * @return
	 */
	public String[] getData(int lineNum){
		if(lineNum>=0 && lineNum<this.Data.length){
			return this.Data[lineNum];
		}
		else return null;
	}
	
	/**
	 * 获取去重后的分类属性值
	 * @param cnum
	 * @return
	 */
	public List<String> getClassify(int cnum){
		Set<String> set = new HashSet<String>();
		for(int i=0; i<this.Data.length; i++){
			set.add(this.Data[i][cnum]);
		}
		List<String> list = new LinkedList<String>();
		Iterator<String> iter = set.iterator();
		while(iter.hasNext()){
			list.add(iter.next());
		}
		return list;
	}
	
	/**
	 * 仅在属性为三维时获取第一维度和第二维度的范围
	 * Data[][] = [ [x,y,z,classify],[x,y,z,classify] ]
	 * @return
	 */
	public double[] getRangeOfData(){
		if(this.Header.length==4){
			double[] range = new double[5];
			double xmin = Double.parseDouble(Data[0][0]), xmax = xmin;
			double ymin = Double.parseDouble(Data[0][1]), ymax = ymin;
			for(int i=1; i<Data.length; i++){
				double x = Double.parseDouble(Data[i][0]);
				double y = Double.parseDouble(Data[i][1]);
				if(xmin>x) xmin = x;
				if(xmax<x) xmax = x;
				if(ymin>y) ymin = y;
				if(ymax<y) ymax = y;
			}
			double step = 0.1;
			
			range[0] = (int)xmin;
			range[1] = (int)xmax+1;
			range[2] = (int)ymin;
			range[3] = (int)ymax+1;
			range[4] = step;

			return range;
		}
		else{
			return null;
		}
		
	}
	
	/**
	 * 从hits中取出header
	 * @param hits
	 */
	private void toHeader(SearchHits hits){
		ArrayList<String> list = new ArrayList<String>();
		Map<String, Object> map = hits.getHits()[this.hitsFrom].getSource();
		
//		System.out.println("测试专用##########################################");
//		for(int i=0; i<10; i++){
//			System.out.println("i:");
//			map = hits.getHits()[i].getSource();
//			System.out.println("uid = "+map.get("uid")+" - "+map.toString());
//		}
//		System.out.println("##########################################");
		
		
		Iterator<Map.Entry<String, Object>> iter = map.entrySet().iterator();
		
		boolean flag = false; // 标志传入的分类属性参数 deleteAttributeSet 是否在数据属性名里面

		while(iter.hasNext()==true){
			Map.Entry<String, Object> temp = iter.next();
			if(this.deleteAttributeSet.contains(temp.getKey()))	// 若遇到该删除的属性名，不会添加进header
				continue;
			if(this.ClassifyAttributeName==null || this.ClassifyAttributeName.length()==0 
					|| !this.ClassifyAttributeName.equals(temp.getKey())){
				list.add(temp.getKey());
			}
			else{
				flag = true;
			}
		}
		// 若分类属性名ClassifyAttributeName不为空，并且存在于数据属性名里面，将其加入header的末尾
		if(flag && this.ClassifyAttributeName!=null && this.ClassifyAttributeName.length()!=0) list.add(this.ClassifyAttributeName);
		this.Header = new String[list.size()];
		list.toArray(this.Header);
	}
	
	/**
	 * 从hits中取出data
	 * @param hits
	 */
	private void toData(SearchHits hits){
		for(int i=0; i<this.Data.length; i++){
			Map<String, Object> map = hits.getHits()[i].getSource();
			for(int j=0; j<this.Header.length; j++){
				String key = this.Header[j];
				
				try{
					if(key==null || map.get(key)==null){
						System.out.println(Arrays.toString(Header));
						System.out.println(Arrays.toString(Data[i]));
						System.out.println("key="+key);
						throw new Exception();
					}
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
				
				this.Data[i][j] = map.get(key).toString();
				
				// 如果对应该值为离散值，并且不属于分类属性，将其归类为离散属性
				// 离散值定义：字符串，长度大于等于8的数字串，包含2017字样的数字串
				// 注意：空值对应属性的处理，按照空值对应属性的非空值部分处理
				try{
					if( this.Data[i][j].length()==0 ) continue;
					if( this.Data[i][j].length()>=8 || this.Data[i][j].contains("2017") )
						throw new NumberFormatException();
					Double.parseDouble(this.Data[i][j]);
				}
				catch(NumberFormatException e){
					if( ( this.ClassifyAttributeName==null || !this.ClassifyAttributeName.equals(this.Header[j]) ) 
							&& !this.discreteAttributeList.contains(this.Header[j]) ){
						this.discreteAttributeList.add(this.Header[j]);
					}
				}
			}
			this.printTest=false;
		}
		this.updateAttributeIndexMap();
	}

	// 对Header和Data进行操作之后，由于可能删除了部分属性，需要更新属性索引和离散属性列表
	private void updateAttributeIndexMap(){
		Iterator<String> iter = this.deleteAttributeSet.iterator();
		while(iter.hasNext()){
			String temp = iter.next();
			this.AttributeIndexMap.remove(temp);
			if(this.discreteAttributeList.contains(temp)){
				this.discreteAttributeList.remove(temp);
			}
		}
		for(int i=0; i<this.Header.length; i++){
			this.AttributeIndexMap.put(this.Header[i], i);
		}
	}
	
//######################################################################################################
//下面对数据进行属性选择处理
//	如果某一列数据完全相同，或者为空值，将其删除
//######################################################################################################
	private void doAttributeSelect(){
		String[] oldHeader = this.Header;
		String[][] oldData = this.Data;
		
		ArrayList<String> newHeaderList = new ArrayList<String>();
		ArrayList<ArrayList<String>> newDataList = new ArrayList<ArrayList<String>>();
		for(int i=0; i<this.NumData; i++) newDataList.add( new ArrayList<String>() );
		
		for(int j=0; j<oldData[0].length; j++){
			String value = oldData[0][j];
			boolean allEqualFlag = true;
			for(int i=0; i<oldData.length; i++){
				if(!value.equals(oldData[i][j])){
					allEqualFlag = false;
					break;
				}
			}
			if( allEqualFlag && 
					(this.ClassifyAttributeName==null || !oldHeader[j].equals(this.ClassifyAttributeName)) ){
				this.deleteAttributeSet.add(oldHeader[j]);
				if(this.discreteAttributeList.contains(oldHeader[j])){
					this.discreteAttributeList.remove(oldHeader[j]);
				}
			}
			else{
				newHeaderList.add(oldHeader[j]);
				for(int i=0; i<oldData.length; i++){
					newDataList.get(i).add(oldData[i][j]);
				}
			}		
		}
		
		// 更新Header和Data
		this.Header = (String[]) newHeaderList.toArray( new String[newHeaderList.size()] );
		for(int i=0; i<newDataList.size(); i++){
			this.Data[i] = (String[]) newDataList.get(i).toArray( new String[newDataList.get(i).size()] );
		}
		this.NumData = newDataList.size();
		this.NumAttributes = newDataList.get(0).size();
		// 更新属性索引
		this.updateAttributeIndexMap();
	}
	
//######################################################################################################
//下面对数据向量化处理，分为两步
//		第一步：离散值向量化
//		第二步：连续值归一化
//	处理后，离散属性在前，连续属性在后
//	注意：一定要保证次操作是最后一步数据操作，因为此操作之后，this.AttributeIndexMap属性索引不再更新
//######################################################################################################
	
	private void doVectorize(){
		String[] oldHeader = this.Header;
		String[][] oldData = this.Data;
		
		ArrayList<String> newHeaderList = new ArrayList<String>();
		ArrayList<ArrayList<String>> newDataList = new ArrayList<ArrayList<String>>();
		for(int i=0; i<this.NumData; i++) newDataList.add( new ArrayList<String>() );
		int newHeaderNum = 0;
		
		String mySplit = " with ";
		String mySplitFrom = " from ";
		String mySplitTo = " to ";
		
		// 1、离散
		for(int discreteAttributeListNum=0; discreteAttributeListNum<this.discreteAttributeList.size(); discreteAttributeListNum++){
			String itemHeader = this.discreteAttributeList.get(discreteAttributeListNum); 	// 离散属性名
			int index = this.AttributeIndexMap.get(itemHeader); 								// 离散属性在原数组的列号
			
			Set<String> itemHeaderSet = new HashSet<String>();
			for(int i=0; i<oldData.length; i++){
				if(oldData[i][index].length()!=0)
					itemHeaderSet.add(oldData[i][index]);
				else
					itemHeaderSet.add(ESData.DISCRETENULLVALUE); // 标志空值
			}
			
			Iterator<String> iter = itemHeaderSet.iterator();
			while(iter.hasNext()) newHeaderList.add(itemHeader+mySplit+iter.next());
			
			for(int i=0; i<this.NumData; i++){
				for(int j=newHeaderNum; j<newHeaderList.size(); j++){
					String[] temp = newHeaderList.get(j).split(mySplit);
					if( temp[temp.length-1].equals(oldData[i][index]) 
							|| (oldData[i][index].length()==0 && temp[temp.length-1].equals(ESData.DISCRETENULLVALUE)) ){
						newDataList.get(i).add("1"); // 若newHeaderList.get(j)后缀与元素值oldData[i][index]相等，或同为空值，则新属性值为1
					}
					else newDataList.get(i).add("0");
				}
			}
			newHeaderNum = newHeaderList.size();
		}
		
		// 2、连续
		for(int index=0; index<oldData[0].length; index++){
			try{
				if( this.discreteAttributeList.contains(oldHeader[index]) 
						|| ( this.ClassifyAttributeName!=null && this.ClassifyAttributeName.equals(oldHeader[index]) ) ) continue;
				String itemHeader = oldHeader[index];
				
				// 找最值
				double minvalue=ESData.MAXNUMBER, maxvalue=ESData.MINNUMBER;
				boolean lowHasInit = false;
				for(int i=0; i<oldData.length; i++){
					if( oldData[i][index].length()==0 ) continue;
					
					double item = Double.parseDouble( oldData[i][index] );
					
					if( !lowHasInit ){
						lowHasInit = true;
						minvalue = item;
						maxvalue = item;
					}
					
					if(item<minvalue) minvalue = item;
					if(item>maxvalue) maxvalue = item;
				}
				
				// 归一化
				if( !lowHasInit ){ // 如果全是空值，以离散值的形式存储
					newHeaderList.add(oldHeader[index]+mySplit+ESData.DISCRETENULLVALUE);
					for(int i=0; i<oldData.length; i++) newDataList.get(i).add("1");
				}
				else if(minvalue==maxvalue){ // 如果最大值等于最小值，全部设置为0.5
					newHeaderList.add(oldHeader[index]+mySplitFrom+minvalue+mySplitTo+maxvalue);
					for(int i=0; i<oldData.length; i++) newDataList.get(i).add(ESData.CONTINUOUSNULLVALUE);
				}
				else{
					newHeaderList.add(oldHeader[index]+mySplitFrom+minvalue+mySplitTo+maxvalue);
					for(int i=0; i<oldData.length; i++){
						if( oldData[i][index].length()==0 ) newDataList.get(i).add(ESData.CONTINUOUSNULLVALUE);
						else{
							double item = Double.parseDouble( oldData[i][index] );
							double itemVectorize = (item-minvalue)/(maxvalue-minvalue);
							newDataList.get(i).add( String.valueOf(itemVectorize) );
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}	
		}
		
		// 分类属性
		if(this.ClassifyAttributeName!=null){
			if(this.ClassifyAttributeName.equals(oldHeader[oldHeader.length-1])){
				newHeaderList.add(this.ClassifyAttributeName);
				for(int i=0; i<newDataList.size(); i++){
					newDataList.get(i).add(oldData[i][oldData[0].length-1]);
				}
			}
		}
		
		
		this.Header = (String[]) newHeaderList.toArray( new String[newHeaderList.size()] );
		for(int i=0; i<newDataList.size(); i++){
			this.Data[i] = (String[]) newDataList.get(i).toArray( new String[newDataList.get(i).size()] );
		}
		this.NumData = newDataList.size();
		this.NumAttributes = this.Data[0].length;
		
//		this.show();
		
		// 验证有无漏掉的属性################################################################
		// oldHeader newHeader
//		Set<String> oldset = new HashSet<String>();
//		Set<String> newset = new HashSet<String>();
//		
//		for(int i=0; i<oldHeader.length; i++) oldset.add(oldHeader[i]);
//		for(int i=0; i<this.Header.length; i++){
//			String head = "";
//			if(this.Header[i].contains(mySplit)) head = this.Header[i].split(mySplit)[0];
//			else head = this.Header[i].split(mySplitFrom)[0];
//			newset.add(head);
//		}
//		int count = 0;
//		System.out.println("old");
//		Iterator<String> iter = oldset.iterator();
//		while(iter.hasNext()) System.out.print(String.format("\t%d:%20s", count++, iter.next()));
//		System.out.println("new");
//		iter = newset.iterator();
//		count = 0;
//		while(iter.hasNext()) System.out.print(String.format("\t%d:%20s", count++, iter.next()));
//		System.out.println();
	}
	
	public static void main(String[] args){
		String[] header = {"n1","n2","n3","n4","n5","n6","class"};
		String[][] data =  {{"1","7","x","0.2","定","20170308","A"},
							{"2","8","y","0.3","x","20170308","B"},
							{"","9","x","0.4","有","","C"},
							{"4","0","z","0.5","有","20170309","B"},
							{"5","1","y","0.6","","20170309","B"},
							{"6","2","z","0.7","定","20170309","A"}};
		ESData esData = new ESData(header, data, "class", ESData.VECTORIZE);
		
		
	}

}










