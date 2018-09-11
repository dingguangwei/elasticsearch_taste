package com.taste.elasticsearch_taste.common;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

import com.taste.elasticsearch_taste.action.TasteEventAction;

public class TasteEventRequestBuilder extends ActionRequestBuilder<TasteEventRequest, TasteEventResponse, TasteEventRequestBuilder> {

	public TasteEventRequestBuilder(ElasticsearchClient client){
		super(client, TasteEventAction.INSTANSE, new TasteEventRequest());
	}
    public TasteEventRequestBuilder setSearchRequest(SearchRequestBuilder builder){
    	 super.request.setSearchRequest(builder);
    	 return this;
    }
	public TasteEventRequestBuilder setSearchRequest(SearchRequest request){
		 super.request.setSearchRequest(request);
		 return this;
	}
	public TasteEventRequestBuilder setPrefix(String string){
		request.setPrefix(string);
		return this;
	}
	
}
