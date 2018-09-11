package com.taste.elasticsearch_taste.action;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;

public final class LoggerUtils {

    public static void emitErrorResponse(RestChannel channel,
                                  Logger logger,
                                  Exception e) {
        try {
            channel.sendResponse(new BytesRestResponse(channel, e));
        } catch (IOException e1) {
            logger.error("Failed to send failure response.", e1);
        }
    }
}
