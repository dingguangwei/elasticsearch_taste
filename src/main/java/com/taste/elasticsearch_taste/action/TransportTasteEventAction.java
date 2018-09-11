package com.taste.elasticsearch_taste.action;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.TransportSearchAction;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.transport.TransportRequestHandler;
import org.elasticsearch.transport.TransportService;

import com.taste.elasticsearch_taste.common.TasteEventRequest;
import com.taste.elasticsearch_taste.common.TasteEventResponse;

public class TransportTasteEventAction extends TransportAction<TasteEventRequest, TasteEventResponse> {
	
	//因为要实现查询，所以要用到ES原有的TransportSearchAction,将它注入进来以便使用
	private final TransportSearchAction searchAction;   
	@Inject
	public TransportTasteEventAction(Settings settings, String actionName, ThreadPool threadPool,
			ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
			TransportService transportService,TransportSearchAction searchAction,NamedXContentRegistry xContentRegistry) {
		super(settings, TasteEventAction.NAME, threadPool,
                actionFilters,
                indexNameExpressionResolver,
                transportService.getTaskManager());
		this.searchAction=searchAction;
		// TODO Auto-generated constructor stub
		
		
		//这个函数是将 ***Action与Transport***Action绑定在一起，其实是与TransportHandler绑定在一起。
		transportService.registerRequestHandler(
                TasteEventAction.NAME,
                TasteEventRequest::new,
                ThreadPool.Names.SAME,
                new TransportHandler());
		
	}
	
    //这个函数是具体的逻辑实现函数
	
	@Override
	protected void doExecute(TasteEventRequest request, ActionListener<TasteEventResponse> listener) {
		// TODO Auto-generated method stub
	    
		//执行查询，异步编程
		searchAction.execute(request.getSearchRequest(), new ActionListener<SearchResponse>(){

			@Override
			public void onResponse(SearchResponse response) {
//				SearchHits ESHits = response.getHits();
//				System.out.println(ESHits.toString());
				// TODO Auto-generated method stub
				 listener.onResponse(new TasteEventResponse(response));
			}

			@Override
			public void onFailure(Exception e) {
				// TODO Auto-generated method stub
				listener.onFailure(e);
			}
			
		});
	}
	
	private final class TransportHandler implements TransportRequestHandler<TasteEventRequest> {
        @Override
        public void messageReceived(final TasteEventRequest request, final TransportChannel channel) throws Exception {
            execute(request, new ActionListener<TasteEventResponse>() {
                @Override
                public void onResponse(TasteEventResponse response) {
                    try {
                        channel.sendResponse(response);
                    } catch (Exception e) {
                        onFailure(e);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    try {
                        channel.sendResponse(e);
                    } catch (Exception e1) {
                        logger.warn("Failed to send error response for action ["
                                + TasteEventAction.NAME + "] and request [" + request + "]", e1);
                    }
                }
            });
        }
    }

}
