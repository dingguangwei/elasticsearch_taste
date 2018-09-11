package ding.util.WEKAUtil;

import weka.classifiers.evaluation.Evaluation;
import weka.core.Utils;

/**
 * 将运行结果转化为json格式
 * @author xiaochenchen
 *
 */
public class WEKAresult {
	String IndexName;
	int From;
	int Size;
	String ClfName;
	
	public Evaluation eval;
	
	
	public WEKAresult(){
		
	}
	public WEKAresult(String IndexName, int From, int Size, String ClfName, Evaluation eval){
		this.IndexName = IndexName;
		this.From = From;
		this.Size = Size;
		this.ClfName = ClfName;
		this.eval = eval;
	}
	
	// 尽量不要用
	public String toString(){
		try {
			return IndexName+From+Size+ClfName+"\n\n"+eval.toClassDetailsString()+eval.toSummaryString()+eval.toMatrixString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// 推荐使用
	public String toJSON(){
		StringBuffer text = new StringBuffer();
		text.append("{\"index\":\""+IndexName+"\",\"from\":"+From+",\"size\":"+Size+",\"clfName\":\""+ClfName+"\",\"EvaluationResult\":{");
		text.append(this.EvaluationToJSON(eval)+"}}");
//		System.out.println(text.toString());
		int index = -1;
		return text.toString();
	}
	
	public String EvaluationToJSON(Evaluation eval){
		String s1 = this.EvaluationClassDetailsStringToJSON(eval);
		String s2 = this.EvaluationSummaryAccuracyToJSON(eval);
		String s3 = this.EvaluationConfusionMatrixToJSON(eval);
		
		return s1+","+s2+","+s3;
	}
	
	// 1、转化Detailed Accuracy为json格式
 	public String EvaluationClassDetailsStringToJSON(Evaluation eval){
 		StringBuilder sb = new StringBuilder();
		sb.append("\"DetailedAccuracyByClass\": {");
		String s = null;
		try {
			s = eval.toClassDetailsString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 获得类别名和类别个数
		int N;
		String[] className;
		String[] line = s.split("\n");
		N = line.length-4;
		className = new String[N];
		for(int i=3; i<line.length-1; i++){
			String[] temp = line[i].split(" ");
			className[i-3] = temp[temp.length-1];
		}
		
		
		if(s.contains("TP Rate")){
			sb.append("\"TPRate\":{");
			for(int i=0; i<N; i++){
				double temp = eval.truePositiveRate(i);
				if(temp==1){
					sb.append( "\""+className[i]+"\":1," );
				}
				else{
					sb.append( "\""+className[i]+"\":"+String.format("%.3f", eval.truePositiveRate(i))+"," );
				}
			}
			if(eval.weightedTruePositiveRate()==1){
				sb.append("\"WeightedAvg\":1}," );
			}
			else{
				sb.append("\"WeightedAvg\":"+String.format("%.3f", eval.weightedTruePositiveRate())+"}," );
			}
		}
		if(s.contains("FP Rate")){
			sb.append("\"FPRate\":{");
			for(int i=0; i<N; i++){
				double temp = eval.falsePositiveRate(i);
				if(temp==0){
					sb.append( "\""+className[i]+"\":0," );
				}
				else{
					sb.append( "\""+className[i]+"\":"+String.format("%.3f", temp)+"," );
				}
			}
			if(eval.weightedFalsePositiveRate()==0){
				sb.append("\"WeightedAvg\":0}," );
			}
			else{
				sb.append("\"WeightedAvg\":"+String.format("%.3f", eval.weightedFalsePositiveRate())+"}," );
			}
		}
		if(s.contains("Precision")){
			sb.append("\"Precision\":{");
			for(int i=0; i<N; i++){
				double temp = eval.precision(i);
				if(temp==1){
					sb.append( "\""+className[i]+"\":1," );
				}
				else{
					sb.append( "\""+className[i]+"\":"+String.format("%.3f", eval.precision(i))+"," );
				}
			}
			if(eval.weightedPrecision()==1){
				sb.append("\"WeightedAvg\":1}," );
			}
			else{
				sb.append("\"WeightedAvg\":"+String.format("%.3f", eval.weightedPrecision())+"}," );
			}
		}
		if(s.contains("Recall")){
			sb.append("\"Recall\":{");
			for(int i=0; i<N; i++){
				double temp = eval.recall(i);
				if(temp==1){
					sb.append( "\""+className[i]+"\":1," );
				}
				else{
					sb.append( "\""+className[i]+"\":"+String.format("%.3f", eval.recall(i))+"," );
				}
			}
			if(eval.weightedRecall()==1){
				sb.append("\"WeightedAvg\":1}," );
			}
			else{
				sb.append("\"WeightedAvg\":"+String.format("%.3f", eval.weightedRecall())+"}," );
			}
		}
		if(s.contains("F-Measure")){
			sb.append("\"FMeasure\":{");
			for(int i=0; i<N; i++){
				double temp = eval.fMeasure(i);
				if(temp==1){
					sb.append( "\""+className[i]+"\":1," );
				}
				else{
					sb.append( "\""+className[i]+"\":"+String.format("%.3f", eval.fMeasure(i))+"," );
				}
			}
			if(eval.weightedFMeasure()==1){
				sb.append("\"WeightedAvg\":1}," );
			}
			else{
				sb.append("\"WeightedAvg\":"+String.format("%.3f", eval.weightedFMeasure())+"}," );
			}
		}
		if(s.contains("MCC")){
			sb.append("\"MCC\":{");
			for(int i=0; i<N; i++){
				double temp = eval.matthewsCorrelationCoefficient(i);
				if(temp==1){
					sb.append( "\""+className[i]+"\":1," );
				}
				else{
					sb.append( "\""+className[i]+"\":"+String.format("%.3f", eval.matthewsCorrelationCoefficient(i))+"," );
				}
			}
			if(eval.weightedMatthewsCorrelation()==1){
				sb.append("\"WeightedAvg\":1}," );
			}
			else{
				sb.append("\"WeightedAvg\":"+String.format("%.3f", eval.weightedMatthewsCorrelation())+"}," );
			}
		}
		if(s.contains("ROC Area")){
			sb.append("\"ROCArea\":{");
			for(int i=0; i<N; i++){
				double temp = eval.areaUnderROC(i);
				if(temp==1){
					sb.append( "\""+className[i]+"\":1," );
				}
				else{
					sb.append( "\""+className[i]+"\":"+String.format("%.3f", eval.areaUnderROC(i))+"," );
				}
			}
			if(eval.weightedAreaUnderROC()==1){
				sb.append("\"WeightedAvg\":1}," );
			}
			else{
				sb.append("\"WeightedAvg\":"+String.format("%.3f", eval.weightedAreaUnderROC())+"}," );
			}
		}
		if(s.contains("PRC Area")){
			sb.append("\"PRCArea\":{");
			for(int i=0; i<N; i++){
				double temp = eval.areaUnderPRC(i);
				if(temp==1){
					sb.append( "\""+className[i]+"\":1," );
				}
				else{
					sb.append( "\""+className[i]+"\":"+String.format("%.3f", eval.areaUnderPRC(i))+"," );
				}
			}
			if(eval.weightedAreaUnderPRC()==1){
				sb.append("\"WeightedAvg\":1}," );
			}
			else{
				sb.append("\"WeightedAvg\":"+String.format("%.3f", eval.weightedAreaUnderPRC())+"}," );
			}
		}
		if(sb.charAt(sb.length()-1)==',') sb.deleteCharAt(sb.length()-1);
		sb.append("}");
//		System.out.println(sb.toString());
		return sb.toString();
	}

	// 2、转化Summary Accuracy为json格式
	public String EvaluationSummaryAccuracyToJSON(Evaluation eval){
		StringBuilder text = new StringBuilder();
		text.append("\"SummaryAccuracy\": {");
		String s = null;
		try {
			s = eval.toSummaryString("=== Summary Accuracy===", false);
//			System.out.println(s);
			
			if (s.contains("Correctly Classified Instances")) {
				text.append("\"CorrectlyClassifiedInstances\":");
				text.append(Utils.doubleToString(eval.correct(), 0, 4) + ",\"CorrectlyClassifiedInstancesRate\":"
				+ Utils.doubleToString(eval.pctCorrect()/100, 0, 4) + ",");
			}
			if (s.contains("Incorrectly Classified Instances")) {
				text.append("\"IncorrectlyClassifiedInstances\":");
				text.append(Utils.doubleToString(eval.incorrect(), 0, 4) + ",\"IncorrectlyClassifiedInstancesRate\":"
				+ Utils.doubleToString(eval.pctIncorrect()/100, 0, 4) + ",");
			}
			if (s.contains("Kappa statistic")) {
				text.append("\"Kappastatistic\":");
				text.append(Utils.doubleToString(eval.kappa(), 0, 4) + ",");
			}
			
			if (s.contains("Mean absolute error")) {
				text.append("\"Meanabsoluteerror\":");
				text.append(Utils.doubleToString(eval.meanAbsoluteError(), 0, 4) + ",");
			}
			if (s.contains("Root mean squared error")) {
				text.append("\"Rootmeansquarederror\":");
				text.append(Utils.doubleToString(eval.rootMeanSquaredError(), 0, 4)+ ",");
			}
			if (s.contains("Relative absolute error")) {
				text.append("\"Relativeabsoluteerror\":");
				text.append(Utils.doubleToString(eval.relativeAbsoluteError()/100, 0, 4)+ ",");
			}
			if (s.contains("Root relative squared error")) {
				text.append("\"Rootrelativesquarederror\":");
				text.append(Utils.doubleToString(eval.rootRelativeSquaredError()/100, 0, 4)+ ",");
			}
			if(s.contains("Total Number of Instances")) {
				int N = (int)(eval.correct()+eval.incorrect());
				text.append("\"TotalNumberofInstances\":");
				text.append(N + ",");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(text.charAt(text.length()-1)==',') text.deleteCharAt(text.length()-1);
		text.append("}");
//		System.out.println(text.toString());
		return text.toString();
	}

	// 3、转化Confusion Matrix为json格式
	public String EvaluationConfusionMatrixToJSON(Evaluation eval){
		StringBuilder text = new StringBuilder();
		text.append("\"ConfusionMatrix\": {");
		String s = null;
		try {
			s = eval.toMatrixString();
			double[][] matr = eval.confusionMatrix();
//			System.out.println(s);
			
			String[] line = s.split("\n");
			int N = line.length-3;
			
			String[] IDTop = new String[N]; // 记录a b c d e等等
			String strtop = line[2].substring(1, line[2].indexOf('<'));
			
			for(int i=0; i<N; i++){
				int j=0;
				while(j<strtop.length() && strtop.charAt(j)==' ') j++;
				int k=j;
				while(k<strtop.length() && strtop.charAt(k)!=' ') k++;
				IDTop[i] = strtop.substring(j, k);
				strtop = strtop.substring(k);
			}
			
			String[] IDRight = new String[N]; // 记录 a = 00 b = 11等等
			for(int i=3; i<3+N; i++){
				IDRight[i-3] = line[i].substring(line[i].indexOf('|')+2);
				IDRight[i-3] = IDRight[i-3].replaceAll(" ", "");
				IDRight[i-3] = IDRight[i-3].replaceAll("=", "Represents");
			}
			
			for(int i=0; i<N; i++){
				text.append("\""+IDRight[i]+"\":{");
				for(int j=0; j<N; j++){
					text.append("\""+IDTop[j]+"\":"+matr[i][j]+",");
				}
				if(text.charAt(text.length()-1)==',') text.deleteCharAt(text.length()-1);
				text.append("},");
			}
			if(text.charAt(text.length()-1)==',') text.deleteCharAt(text.length()-1);
			text.append("}");
		}catch(Exception e){
			e.printStackTrace();
		}
//		System.out.println(text.toString());
		return text.toString();
	}
	
	public static void main(String[] args){
		StringBuilder text = new StringBuilder();
		text.append("\"ConfusionMatrix\": {");
		String s = "=== Confusion Matrix ===\n\n  a  b  c  d  e   <-- classified as\n 39  0  1  0  0 |  a = 00\n  0  1  0  1  0 |  b = 11\n  0  0  2  0  0 |  c = 20\n  1  1  0  2  0 |  d = 10\n  0  0  0  0  2 |  e = 21\n";
		try {
			double[][] matr = {{39,  0,  1,  0,  0},
					{0,  1,  0,  1,  0},
					{0,  0,  2,  0,  0},
					{1,  1,  0,  2,  0},
					{0,  0,  0,  0,  2}};
			System.out.println(s);
			
			String[] line = s.split("\n");
			int N = line.length-3;
			
			String[] IDTop = new String[N]; // 记录a b c d e等等
			String strtop = line[2].substring(1, line[2].indexOf('<'));
			
			for(int i=0; i<N; i++){
				int j=0;
				while(j<strtop.length() && strtop.charAt(j)==' ') j++;
				int k=j;
				while(k<strtop.length() && strtop.charAt(k)!=' ') k++;
				IDTop[i] = strtop.substring(j, k);
				strtop = strtop.substring(k);
			}
			
			String[] IDRight = new String[N]; // 记录 a = 00 b = 11等等
			for(int i=3; i<3+N; i++){
				IDRight[i-3] = line[i].substring(line[i].indexOf('|')+2);
				IDRight[i-3] = IDRight[i-3].replaceAll(" ", "");
				IDRight[i-3] = IDRight[i-3].replaceAll("=", "Represents");
			}
			
			for(int i=0; i<N; i++){
				text.append("\""+IDRight[i]+"\":{");
				for(int j=0; j<N; j++){
					text.append("\""+IDTop[j]+"\":"+matr[i][j]+",");
				}
				if(text.charAt(text.length()-1)==',') text.deleteCharAt(text.length()-1);
				text.append("},");
			}
			if(text.charAt(text.length()-1)==',') text.deleteCharAt(text.length()-1);
			text.append("}");
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(text.toString());
	}
	
	
}