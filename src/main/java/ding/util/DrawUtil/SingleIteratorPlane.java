package ding.util.DrawUtil;

/**
 * 单次迭代产生的多个超平面
 * @author xiaochenchen
 *
 */
public class SingleIteratorPlane{
	private double[] singleIterationMidResult;
	private double[] rangeOfData;
	private final int wSize = 4;
	public HyperPlane[] plane;
	
	public SingleIteratorPlane(double[] singleIterationMidResult, double[] rangeOfData){
		this.singleIterationMidResult = singleIterationMidResult;
		this.rangeOfData = rangeOfData;
		this.init();
	}
	
	private void init(){
		int N = this.singleIterationMidResult.length/this.wSize;
		try{
			if(this.singleIterationMidResult.length%this.wSize!=0) throw new Exception();
		}catch(Exception e){
			e.printStackTrace();
		}
		this.plane = new HyperPlane[N];
		for(int iter=0; iter<N; iter++){
			double[] w = new double[this.wSize];
			/*注意：一行midResult的分布如下
			 * _____________________________________
			 * |[-130, -174, 44, 5, -30, -1, -7, 34]|
			 * |____________________________________|
			 * |    			classA     classB	|
			 * |a				44			5		|
			 * |b				-30			-1		|
			 * |c				-7			34		|
			 * |Intercept		130			-174	|
			 * |____________________________________|
			 * 
			 * 每一个plane中需要 w[0]+w[1]*x+w[2]*y+w[3]*z=0
			 * */
			
			for(int i=0; i<w.length; i++){
				w[i] = this.singleIterationMidResult[iter+i*N];
			}
			this.plane[iter] = new HyperPlane(w, this.rangeOfData);
		}
	}
	
}