package ding.util.DrawUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class HyperPlane {
	private double[] w;
	private double xmin;
	private double xmax;
	private double ymin;
	private double ymax;
	private double step;
	public LinkedList<double[]> pointList;
	
	private boolean m_debug = false;
	private String writePath = "F:\\ESresult\\result.txt";
	
	public HyperPlane(double[] w, double[] rangeOfData){
		this.w = w;
		this.xmin = rangeOfData[0];
		this.xmax = rangeOfData[1];
		this.ymin = rangeOfData[2];
		this.ymax = rangeOfData[3];
		this.step = rangeOfData[4];
		this.pointList = new LinkedList<double[]>();
		try{
			if(w.length!=4 || xmin>=xmax || ymin>=ymax) throw new Exception();
		}catch(Exception e){
			e.printStackTrace();
		}
		this.init();
	}
	
	// 将平面参数转化为点
	public void init(){
		if(w[3]!=0){
			for(double x=xmin; x<=xmax; x+=step){
				for(double y=ymin; y<=ymax; y+=step){
					double[] p = new double[3];
					double z = -(w[0]+w[1]*x+w[2]*y)/w[3];
					p[0] = x; p[1] = y; p[2] = z;
					for(int i=0; i<p.length; i++){
						String temp = String.format("%.3f", p[i]).replace(',', '.');
						p[i] = Double.parseDouble(temp);
					}
					pointList.add(p);
				}
			}
		}
		
		if(this.m_debug){
			System.out.println("Debug: HyperPlane 已将平面点写入"+this.writePath);
			this.record();
		}
	}
	
	public void record(){
		try(FileWriter fw = new FileWriter(this.writePath, true)){
			for(double[] point:pointList){
				fw.write("["+point[0]);
				for(int j=1; j<point.length; j++){
					fw.write(","+point[j]);
				}
				fw.write("],\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
