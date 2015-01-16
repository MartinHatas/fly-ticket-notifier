package cz.hatoff.ftn.shorten;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BitDoUrlShortenProvider implements ShortenUrlProvider {

    private static final Logger logger = LogManager.getLogger(BitDoUrlShortenProvider.class);

    private static final String SHORTEN_PROVIDER_HOST = "bit.do";
    private static final String SHORTEN_PROVIDER_PATH = "/mod_perl/url-shortener.pl";

    private static final String KEY_URL_BASE = "url_base";
    private static final String KEY_URL_HASH = "url_hash";


    @Override
    public String shortenUrl(String url) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = createHttpPostRequest(url);
            logger.info("Going to shorten url on bit.do.");
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                logger.info("Got response with status: " + response.getStatusLine());
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = entity.getContent();
                    try {
                        String shortUrl = null;
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> jsonMap = mapper.readValue(inputStream, Map.class);
                        shortUrl = String.format("%s%s", jsonMap.get(KEY_URL_BASE), jsonMap.get(KEY_URL_HASH)).replace("http://", "");
                        logger.info(String.format("Short url is '%s'", shortUrl));
                        return shortUrl;
                    } catch (IOException e) {
                        throw e;
                    } finally {
                        IOUtils.closeQuietly(inputStream);
                    }
                }
                throw new IllegalArgumentException("Http response does not contains any data.");
            } finally {
                IOUtils.closeQuietly(response);
            }
        } finally {
            IOUtils.closeQuietly(httpClient);
        }
    }

    private HttpPost createHttpPostRequest(String url) throws Exception {
        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost(SHORTEN_PROVIDER_HOST)
                .setPath(SHORTEN_PROVIDER_PATH)
                .build();

        HttpPost httpPost = new HttpPost(uri);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("action", "shorten"));
        nvps.add(new BasicNameValuePair("url", url));
        nvps.add(new BasicNameValuePair("url2", "site2"));
        nvps.add(new BasicNameValuePair("url_hash:", ""));
        nvps.add(new BasicNameValuePair("url_stats_is_private", "0"));
        nvps.add(new BasicNameValuePair("permasession", "1421411939|bv9nr0be29"));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.addHeader("X-Requested-With", "XMLHttpRequest");
        httpPost.addHeader("Accept", "*/*");

        return httpPost;
    }
}
