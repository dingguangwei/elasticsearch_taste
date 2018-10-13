package com.taste.elasticsearch_taste;

import java.io.*;
import java.net.InetSocketAddress;

import org.apache.http.HttpResponse;

import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.common.network.NetworkAddress;

//import org.elasticsearch.common.xcontent.XContentType;

public class TasteEventRestTest extends TastePluginTest {
	public void test_recommended_items_from_user() throws Exception {
//		final String index = "blog";
//		XContentType type = randomFrom(XContentType.values());
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			InetSocketAddress[] endpoint = cluster().httpAddresses();
			this.restBaseUrl = "http://" + NetworkAddress.format(endpoint[0]);

			String s1 = "/_taste/parameter?op=01&index=iris-2&from=0&size=20";
			
			// 训练
			String s2 = "/_taste/parameter?op=31&index=iris-2&from=0&size=20&algo=J48&cAttr=classify&mWpath=C:/Users/xiaochenchen/Desktop/当前工作/result/J48.model";
			
			// 预测
			String s3 = "/_taste/parameter?op=32&index=iris-2&from=0&size=20&cAttr=classify&mRpath=C:/Users/xiaochenchen/Desktop/当前工作/result/J48.model";
			
			HttpGet get=new HttpGet(restBaseUrl + s1);
			System.out.println("post请求已发送11111111111");
			HttpResponse response = httpClient.execute(get);
			System.out.println("post请求已发送");
			
			
			if(response.getStatusLine().getStatusCode()==200){//如果状态码为200,就是正常返回
				System.out.println("\n返回 JSON ：");
				//得到返回的字符串
				String result=EntityUtils.toString(response.getEntity());
				
				//System.out.println(result);
				try {
					String writePath = "C:\\Users\\xiaochenchen\\Desktop\\当前工作\\result\\result.txt";
					File file = new File(writePath);
					if(!file.exists()){
						file.createNewFile();
					}
					FileWriter fw = new FileWriter(file,false);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(result);
					bw.close(); 
					fw.close();
					System.out.println("结果已写入："+writePath+"\n");
			         
			    } catch (Exception e) {
			    	e.printStackTrace();
			    }
			}
			else{
				System.out.println("\n返回JSON出错！");
			}
			
		}
   }
}


