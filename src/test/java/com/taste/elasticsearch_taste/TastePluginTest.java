package com.taste.elasticsearch_taste;

import java.util.ArrayList;
import java.util.Collection;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.http.HttpTransportSettings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.transport.Netty4Plugin;

import com.taste.elasticsearch_taste.plugin.TastePlugin;

@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.SUITE, numDataNodes = 1, numClientNodes = 0, transportClientRatio = 0, supportsDedicatedMasters = false)
public class TastePluginTest extends ESIntegTestCase {
	protected String restBaseUrl;
    protected Client client;
    int randomPort = 9200;
    public TastePluginTest(){
    	
    }
    //static int nodeOrdinal=1;
	    @Override
	    protected Settings nodeSettings(int nodeOrdinal) {
	        return Settings.builder()
	                .put(super.nodeSettings(nodeOrdinal))
	                .put(NetworkModule.HTTP_ENABLED.getKey(), true)
	                .put(HttpTransportSettings.SETTING_HTTP_PORT.getKey(), randomPort)
	                .put("network.host", "127.0.0.1")
	                .build();
	    }
	    
	    @Override
	    protected Collection<Class<? extends Plugin>> nodePlugins() {
	      //return Collections.singletonList(TastePlugin.class);
	      Collection<Class<? extends Plugin>> al = new ArrayList<Class<? extends Plugin>>();
	     	al.add(Netty4Plugin.class);
	  	    al.add(TastePlugin.class);
	  	    return  al;
	    }
	    
	    @Override
	    protected Collection<Class<? extends Plugin>> transportClientPlugins() {
	        return nodePlugins();
	    }
	

}
