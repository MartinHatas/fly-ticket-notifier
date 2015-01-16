package cz.hatoff.ftn.sender;


import cz.hatoff.ftn.model.FlyTicket;
import org.apache.commons.io.IOUtils;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SmsSender {

    private static final Logger logger = LogManager.getLogger(SmsSender.class);

    private static final String SMS_WEB_HOST = "www.sms-o2.cz";
    private static final String SMS_WEB_PATH = "/index.php";

    private String[] phoneNumbers;


    public SmsSender(String[] phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public void sendSMS(List<FlyTicket> flyTickets) throws Exception {
        for (FlyTicket flyTicket : flyTickets) {
            sendSMS(flyTicket);
            Thread.sleep(5000);
        }
    }

    private void sendSMS(FlyTicket flyTicket) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            for (String phoneNumber : phoneNumbers) {
                HttpPost httpPost = createHttpPostRequest(flyTicket, phoneNumber);
                logger.info("Going to send SMS to number " + phoneNumber + " with fly ticket " + flyTicket.toString());
                CloseableHttpResponse response = httpClient.execute(httpPost);
                try {
                    logger.info("Got response with status: " + response.getStatusLine());
                } finally {
                    IOUtils.closeQuietly(response);
                }
            }
        } finally {
            IOUtils.closeQuietly(httpClient);
        }
    }

    private HttpPost createHttpPostRequest(FlyTicket flyTicket, String phoneNumber) throws Exception {
        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost(SMS_WEB_HOST)
                .setPath(SMS_WEB_PATH)
                .build();

        HttpPost httpPost = new HttpPost(uri);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("operator", "auto"));
        nvps.add(new BasicNameValuePair("number", phoneNumber));
        nvps.add(new BasicNameValuePair("message", flyTicket.toSMS()));
        nvps.add(new BasicNameValuePair("submit", "Odeslat"));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

        return httpPost;
    }


}
