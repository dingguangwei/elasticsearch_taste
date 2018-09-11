package ding.util.DrawUtil;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.logging.Logger;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

/**
 * 使用jfree在java中作图
 * @author xiaochenchen
 *
 */
public class drawScatterAndLine2D implements Runnable{
	public static Logger log = Logger.getLogger(drawScatterAndLine2D.class.toGenericString());
	public double[][][] data;
	public String[] attrName;
	public int[] flag;
	public String chartName;
	
	public int xmin=-1,xmax=2;
	
	public static final int Scatter_Code = 0;
	public static final int Line_Code = 1;
	public static final int CoffNum = 3;	// 一条二维直线参数的个数
	public static final int TIME = 5000;		// 动态演示间隔时间
	
	XYDataset xyDataSet;
	
	// 动态加载
	public boolean dynamic = false;
	public int scatterSetNum = -1;		// 点集类别数
	public XYSeries[] lineSeries;		// 每次可以变换数据的直线对象
	public int lineSeriesNum = -1; 		// 每次运行时展示的直线条数（一般来说，一条直线参数为3个，通过lineCoefficient每行参数个数，可以判断直线条数
	
	/**
	 * 构造函数
	 * @param data			散点数据+直线端点坐标
	 * @param attrName		每一个数据名称
	 * @param flag			散点/直线 = 0/1
	 * @param chartName		表格名
	 */
	public drawScatterAndLine2D(double[][][] data, String[] attrName, int[] flag, String chartName){
		this.data = data;
		this.attrName = attrName;
		this.flag = flag;
		this.chartName = chartName;
	}
	
	/**
	 * 构造函数
	 * @param data				散点数据
	 * @param attrName			散点+直线数据名称
	 * @param lineCoefficient	直线系数
	 * @param chartName			
	 */
	public drawScatterAndLine2D(double[][][] scatterData, String[] attrName, double[][] lineCoefficient, String chartName, boolean dynamic){
		if(lineCoefficient.length>0) 
			this.lineSeriesNum = lineCoefficient[0].length/CoffNum;
		this.data = new double[scatterData.length+lineCoefficient.length*this.lineSeriesNum][][];
		this.flag = new int[data.length];
		for(int i=0; i<scatterData.length; i++){
			this.data[i] = scatterData[i];
			flag[i] = Scatter_Code;
		}
		
		int index = scatterData.length;
		for(int j=0; j<lineCoefficient.length; j++){
			// 第j轮迭代得到的直线组
			for(int k=0; k<this.lineSeriesNum; k++, index++){
				// 该直线组中的第k条直线
				double[] w = new double[CoffNum];
				for(int ii=0; ii<w.length; ii++){
					w[ii] = lineCoefficient[j][ii*this.lineSeriesNum+k]; // 根据lineCoefficient的参数分布，第一条直线（0）的参数索引为0*2+0，1*2+0，2*2+0
				}
				this.data[index] = this.getLineEndPointFromCoefficient(w);
				flag[index] = Line_Code;
			}
		}
		this.attrName = attrName;
		this.chartName = chartName;
		this.dynamic = dynamic;
	}
	
	/**
	 * 根据直线系数，得到直线的端点
	 * @param w w[0]+w[1]*x+w[2]*y=0
	 * @return
	 */
	private double[][] getLineEndPointFromCoefficient(double[] w){
//		System.out.println(Arrays.toString(w));
		double[][] ldata = new double[2][2];
		ldata[0][0] = xmin;
		ldata[0][1] = -(w[0]+w[1]*ldata[0][0])/w[2];
		ldata[1][0] = xmax;
		ldata[1][1] = -(w[0]+w[1]*ldata[1][0])/w[2];
		return ldata;
	}
	
	// 将数据数组转化为点XYDataset
	private XYDataset getxyDataset(double[][][] data, String[] attrName){
		XYSeries[] xyseries = new XYSeries[data.length];
		for(int i=0; i<xyseries.length; i++){
			xyseries[i] = new XYSeries(attrName[i]);
			for(int j=0; j<data[i].length; j++){
				double[] temp = data[i][j];
				xyseries[i].add(temp[0], temp[1]);
			}
		}
		
		// 当使用动态draw时，xyseries数组只有最后一位代表直线
		if(this.dynamic){
			this.lineSeries = new XYSeries[this.lineSeriesNum];
			for(int i=this.lineSeriesNum-1, j=xyseries.length-1; i>=0; i--, j--){
				this.lineSeries[i] = xyseries[j];
			}
		}
		
		XYSeriesCollection xyseriescollection = new XYSeriesCollection();
		for(int i=0; i<xyseries.length; i++){
			xyseriescollection.addSeries(xyseries[i]);
		}
		return xyseriescollection;
	}

	private XYDataset getxyDataset(List<double[][]> data, String[] attrName){
		double[][][] temp = new double[data.size()][][];
		for(int i=0; i<temp.length; i++)
			temp[i] = data.get(i);
		return this.getxyDataset(temp, attrName);
	}
	
	private JFreeChart createChart(XYDataset dataset, int[] mflag){
		JFreeChart jfreechart = ChartFactory.createXYLineChart(this.chartName, "X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);
		XYPlot xyplot = (XYPlot)jfreechart.getPlot();
		XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer();
		
		for(int i=0; i<mflag.length; i++){
			if(mflag[i]==0){
				xylineandshaperenderer.setSeriesLinesVisible(i, false);// 画散点图
			}
			else if(mflag[i]==1){
				xylineandshaperenderer.setSeriesShapesVisible(i, false);// 画折线图
			}
		}
		
		xylineandshaperenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyplot.setRenderer(xylineandshaperenderer);
		NumberAxis numberaxis = (NumberAxis)xyplot.getRangeAxis();
		numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		return jfreechart;
	}
	
	// dynamic将暂时只画出一条分类直线
	public void draw(){
		JFreeChart chart = null;
		if(!dynamic){
			this.xyDataSet = this.getxyDataset(this.data, this.attrName);
		}
		else{
			int count = 0;
			for(int item:this.flag){
				if(item==Scatter_Code)count++;
			}
			this.scatterSetNum = count;
			// 为了制造直线动态变化的效果
			List<double[][]> list = new ArrayList<double[][]>();
			for(int i=0; i<this.scatterSetNum; i++) list.add(data[i]);	// 散点
			for(int i=0; i<this.lineSeriesNum; i++) list.add(data[i+this.scatterSetNum]);							// 直线
			this.xyDataSet = this.getxyDataset(list, attrName);
		}
		chart = this.createChart(this.xyDataSet, this.flag);
		ChartFrame frame = new ChartFrame(this.chartName, chart, true);  
		frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowevent) {
				System.exit(0);
			}
		});
	}
	
	public void run(){
		int index = this.scatterSetNum+this.lineSeriesNum;
		while(index<this.data.length){
			try {
				Thread.sleep(TIME/index);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(int i=0; i<this.lineSeriesNum; i++, index++){
				this.lineSeries[i].delete(0, this.lineSeries[i].getItemCount()-1);
				for(int j=0; j<this.data[index][i].length; j++){
					this.lineSeries[i].add(this.data[index][j][0], this.data[index][j][1]);
					log.info(" ["+this.data[index][j][0]+", "+this.data[index][j][1]+"]");
				}
			}
		}
	}
	
	public String toString(){
		StringBuffer text = new StringBuffer();
		text.append("data:\n");
		for(int i=0; i<data.length; i++){
			text.append("data["+i+"]\n");
			for(int j=0; j<data[i].length; j++){
				text.append(Arrays.toString(data[i][j])+"\n");
			}
			text.append("______________________________________\n");
		}
		return text.toString();
	}
	
	public static void main(String[] args) {
		double[][] d1 = new double[5][2];
		double[][] d2 = new double[2][2];
		double[][] d3 = new double[10][2];
		
		System.out.println("d1");
		for(int i=0; i<d1.length; i++){
			for(int j=0; j<d1[0].length; j++){
				d1[i][j] = Math.random();
			}
			System.out.println(Arrays.toString(d1[i]));
		}
		
		System.out.println("d2");
		for(int i=0; i<d2.length; i++){
			for(int j=0; j<d2[0].length; j++){
				d2[i][j] = Math.random();
			}
			System.out.println(Arrays.toString(d2[i]));
		}
		
		System.out.println("d3");
		for(int i=0; i<d3.length; i++){
			for(int j=0; j<d3[0].length; j++){
				d3[i][j] = Math.random();
			}
			System.out.println(Arrays.toString(d3[i]));
		}
		
		double[][][] data = {d1, d2, d3};
		String[] attrName = {"Scatter1","Line", "Scatter2"};
		int[] flag = {0,1, 0}; // 分别代表画散点和直线（其实本质就是点雨点之间是否连线）
		String chartName = "Scatter+Line demo";
		drawScatterAndLine2D ob = new drawScatterAndLine2D(data, attrName, flag, chartName);
		ob.draw();
	}

}
