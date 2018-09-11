package com.taste.elasticsearch_taste.common;

import java.io.IOException;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class TasteEventRequest extends ActionRequest{
    private String prefix;      //这个没啥用
    private SearchRequest searchRequest;
	@Override
	public ActionRequestValidationException validate() {
		// TODO Auto-generated method stub
		return null;
	}
	public SearchRequest getSearchRequest() {
		return searchRequest;
	}
	public TasteEventRequest setSearchRequest(SearchRequest searchRequest) {
		this.searchRequest = searchRequest;
		return this;
	}
	public TasteEventRequest setSearchRequest(SearchRequestBuilder builder){
		return setSearchRequest(builder.request());
	}
	public String getPrefix() {
		return prefix;
	}
	public TasteEventRequest setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}
	//下面这俩函数有啥用暂时不知道
	@Override
	public void readFrom(StreamInput in) throws IOException {
		// TODO Auto-generated method stub
		super.readFrom(in);
		prefix=in.readString();
	}
	
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		// TODO Auto-generated method stub
		super.writeTo(out);
		out.writeString(prefix);
	}
      
}
