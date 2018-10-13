package com.taste.elasticsearch_taste.rest;

import static com.taste.elasticsearch_taste.action.LoggerUtils.emitErrorResponse;
import static org.elasticsearch.rest.RestRequest.Method.POST;

import java.io.IOException;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.search.RestSearchAction;
import org.elasticsearch.search.SearchHits;

import com.taste.elasticsearch_taste.action.TasteEventAction;
import com.taste.elasticsearch_taste.common.TasteEventRequestBuilder;
import com.taste.elasticsearch_taste.common.TasteEventResponse;

import ding.util.ESUtil.ActionParameter;
import ding.util.OpUtil.Operate;


public class TasteEventRestAction extends BaseRestHandler{
	public ActionParameter parameter;
	
	@Inject
	public TasteEventRestAction(final Settings settings,final RestController restController) {
		super(settings);
		restController.registerHandler(RestRequest.Method.GET, "/_taste/{action}", this);
		restController.registerHandler(RestRequest.Method.GET, "/_taste", this);
	}
	
	@Override
	protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
		if (request.method() == POST && !request.hasContent()) {
            return channel -> emitErrorResponse(channel, logger, new IllegalArgumentException("Request body was expected for a POST request."));
        }
        String action = request.param("action");
        if (action != null) {
        	logger.info("do something action");
        	return createDoSomethingResponse(request, client);
        } else {  
        	logger.info("do Message action -- do anything without parameter( not used till now )");
            return createMessageResponse(request);  
        }
	}
	
	
	// 一、对应URL为 /_taste/{action}
	private RestChannelConsumer createDoSomethingResponse(RestRequest request, NodeClient client){
		this.parameter= new ActionParameter(request);
		System.out.println("createDoSomethingResponse: "+this.parameter.OperateCode+", "+this.parameter.Index+", "+this.parameter.From+", "+this.parameter.Size);
		if(this.parameter.OperateCode.equals(ActionParameter.DoNothing_Code)){
			logger.info("DoNothing -- Analyze & Predict");
			return channel -> {
	            XContentBuilder builder = channel.newBuilder();  
	            builder.startObject();
	            builder.field("res","DoNothing -- Analyze & Predict");
	            builder.endObject();  
	            channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));  
	        };
		}
		else{
			// 1、通过parameter进行数据查询，构造自定义的request请求
	    	final TasteEventRequestBuilder actionBuilder=new TasteEventRequestBuilder(client);
	    	SearchRequestBuilder requestBuilder = client.prepareSearch(parameter.Index).setQuery(QueryBuilders.matchAllQuery())
	    			.setFrom(parameter.From).setSize(parameter.Size).setExplain(true);
	    	SearchRequest searchRequest=new SearchRequest();
			try {
				RestSearchAction.parseSearchRequest(searchRequest, request, null);
			} catch (IOException e1) {
				logger.debug("Failed to emit response.", e1);
				e1.printStackTrace();
			}
			actionBuilder.setSearchRequest(requestBuilder);
			return channel -> client.execute(TasteEventAction.INSTANSE, actionBuilder.request(),new ActionListener<TasteEventResponse>() {
				@Override
				public void onResponse(TasteEventResponse response) {
					try{
						// 2、获得查询数据
						SearchResponse myresponse=response.getSearchResponse();
						SearchHits ESHits = myresponse.getHits();
						
						// 3、调用机器学习功能
						XContentBuilder builder = channel.newBuilder();
						new Operate(ESHits, parameter, builder);
						
						//response.toXContent(builder, request); // 此处可以写个覆盖函数
						channel.sendResponse( new BytesRestResponse(RestStatus.OK, builder));
					}catch(Exception e){
						logger.debug("Failed to emit response.", e);
	                    onFailure(e);
					}
				}
				
				@Override
				public void onFailure(Exception e) {
					emitErrorResponse(channel, logger, e);
				}
				
			});
		}
	}	
	
	
	// 2、对应URL为 /_taste
	private RestChannelConsumer createMessageResponse(RestRequest request) {
		
		return channel -> {  
            Message message = new Message();
            XContentBuilder builder = channel.newBuilder();  
            builder.startObject();
            message.toXContent(builder, request);
            builder.endObject();  
            channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));  
        };  
    }
	
	class Message implements ToXContent {  
        @Override  
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {  
            return builder.field("res", "This is my first plugin      Run without error");  
        }  
    }
	
	
}
