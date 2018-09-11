package ding.util.DrawUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.elasticsearch.common.xcontent.XContentBuilder;

/**
 * MutliIteratorPlane->SingleIteratorPlane->HyperPlane
 * 整个迭代过程的所有超平面点
 * @author xiaochenchen
 *
 */
public class MutliIteratorPlane {
	private ArrayList<double[]> midResultList;
	private String[] header;
	private double[] rangeOfData = {4, 8, 1, 5, 0.2}; // 初始化rangeOfData
	private String[][] data;
	public LinkedList<SingleIteratorPlane> singleIteratorPlaneList;
	
	private boolean m_debug = true;
	private String writePath = "F:\\ESresult\\result.txt";
	
	public MutliIteratorPlane(ArrayList<double[]> midResultList, 
			String[] header, double[] rangeOfData, String[][] data){
		this.midResultList = midResultList;
		this.header = header;
		this.rangeOfData = rangeOfData;
		this.data = data;
		this.singleIteratorPlaneList = new LinkedList<SingleIteratorPlane>();
		this.init();
	}
	
	private void init(){
		for(int iterator=0; iterator<this.midResultList.size(); iterator++){
			// 减少迭代次数多时的数据
			if(this.m_debug){
				iterator = this.midResultList.size()-1;
				System.out.println("debug状态：iteration="+iterator+" w="+Arrays.toString(this.midResultList.get(iterator)));
			}
			else{
				if(this.singleIteratorPlaneList.size()>=100){
					if(iterator%10!=0) continue;
				}
				else if(this.singleIteratorPlaneList.size()>=10){
					if(iterator%5!=0) continue;
				}
			}
			SingleIteratorPlane singleIteratorPlane = new SingleIteratorPlane(this.midResultList.get(iterator), this.rangeOfData);
			this.singleIteratorPlaneList.add(singleIteratorPlane);
		}
		if(this.m_debug){
			this.writeDebug();
		}
	}
	
	// just for test
	public MutliIteratorPlane(ArrayList<double[]> midResultList){
		this.midResultList = midResultList;
		this.singleIteratorPlaneList = new LinkedList<SingleIteratorPlane>();
		for(int i=0; i<this.midResultList.size(); i++){
			SingleIteratorPlane temp = new SingleIteratorPlane(this.midResultList.get(i), this.rangeOfData);
			this.singleIteratorPlaneList.add(temp);
		}
		if(this.m_debug){
			this.writeDebug();
		}
	}
	
	public void writeBuilder(XContentBuilder builder){
		try{
			// 1、循环所有迭代的超平面组
			builder.startObject();
			for(int iterator=0; iterator<this.singleIteratorPlaneList.size(); iterator++){
				// 2、获取每次迭代的超平面组
				SingleIteratorPlane singleIteratorPlane = this.singleIteratorPlaneList.get(iterator);
				builder.startArray("iterator"+iterator);
				for(int planei=0; planei<singleIteratorPlane.plane.length; planei++){
					HyperPlane plane = singleIteratorPlane.plane[planei];
					LinkedList<double[]> pointList = plane.pointList;
					
					for(double[] point:pointList){
						builder.startObject();
						for(int j=0; j<point.length; j++){
							builder.field(header[j], point[j]);
						}
						builder.field(header[header.length-1], "hyperPlane"+planei);
						builder.endObject();
					}
				}
				builder.endArray();
			}
			builder.endObject();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void writeDebug(){
		try(FileWriter fw = new FileWriter(this.writePath)){
			if(this.data!=null){
				System.out.println("debug：MutliIteratorPlane: 1、将所有样本点写入文件"+this.writePath);
				for(int i=0; i<data.length; i++){
					fw.write("["+data[i][0]);
					for(int j=1; j<data[i].length-1; j++){
						fw.write(","+data[i][j]);
					}
					fw.write("],\r\n");
				}
			}
			
			System.out.println("debug：MutliIteratorPlane: 2、将平面点写入文件"+this.writePath);
			for(int iterator=0; iterator<this.singleIteratorPlaneList.size(); iterator++){
				// 获取每次迭代的超平面组
				System.out.println("写入第"+iterator+"次迭代的超平面组");
				SingleIteratorPlane singleIteratorPlane = this.singleIteratorPlaneList.get(iterator);
				for(int planei=0; planei<singleIteratorPlane.plane.length; planei++){
					System.out.println(iterator+" 平面"+planei);
					HyperPlane plane = singleIteratorPlane.plane[planei];
					LinkedList<double[]> pointList = plane.pointList;
					
					for(double[] point:pointList){
						fw.write("["+point[0]);
						for(int j=1; j<point.length; j++){
							fw.write(","+point[j]);
						}
						fw.write("],\r\n");
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
//		String[] samplePoint = {"0.98,0.9,0.9,Iris-1",
//				"0.95,0.98,0.98,Iris-1",
//				"0.85,1.1,1.1,Iris-1",
//				"1.1,0.85,0.85,Iris-1",
//				"1.3,0.65,0.65,Iris-1",
//				"1.1,0.95,0.95,Iris-2",
//				"0.95,1.1,1.1,Iris-2",
//				"1.05,1.05,1.05,Iris-2",
//				"1.2,0.85,0.85,Iris-2",
//				"0.7,1.35,1.35,Iris-2"};
//		String writePath = "F:\\ESresult\\result.txt";
//		try(FileWriter fw = new FileWriter(writePath)){
//			for(String p:samplePoint){
//				String[] point = p.split(",");
//				fw.write("["+point[0]);
//				for(int j=1; j<point.length-1; j++){
//					fw.write(","+point[j]);
//				}
//				fw.write("],\r\n");
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		double[] w = {127.76439641495335, 89.54908665478524, -18.95420808410341, -22.806578351603946, -47.20261346075323, -47.84118410633091, 43.01732264973667, 56.16471623582172};
		ArrayList<double[]> midResultList = new ArrayList<double[]>();
		midResultList.add(w);
		MutliIteratorPlane ob = new MutliIteratorPlane(midResultList);
		
	}
}