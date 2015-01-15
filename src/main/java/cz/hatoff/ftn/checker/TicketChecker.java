package cz.hatoff.ftn.checker;


import cz.hatoff.ftn.model.FlyTicket;
import cz.hatoff.ftn.FtnApplication;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TicketChecker {

    private static final Logger logger = LogManager.getLogger(FtnApplication.class);

    private static final String AZAIR_SEARCH_HOST = "www.azair.cz";
    private static final String AZAIR_SEARCH_PATH = "/azfin.php";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String MONTH_FORMAT = "yyyy-MM-dd";


    private int maxPrize;
    private Date departureDate , returnDate;
    private int minDays, maxDays;
    private int maxChanges ;

    public TicketChecker(int maxPrize, Date departureDate, Date returnDate, int minDays, int maxDays, int maxChanges) {
        this.maxPrize = maxPrize;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.minDays = minDays;
        this.maxDays = maxDays;
        this.maxChanges = maxChanges;
    }

    public List<FlyTicket> checkTickets() throws Exception{
        logger.info(String.format("Going to check fly tickets."));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet httpget = createHttpGetRequest();
            logger.info("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpClient.execute(httpget);
            try {
                logger.info("Got response with staus: " + response.getStatusLine());
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    InputStream inputStream = entity.getContent();
                    try {
                        List<String> lines = IOUtils.readLines(inputStream);
                        List<FlyTicket> flyTickets = new TicketParser().parseFlyTickets(lines);
                        logger.info("Totally fetched " + flyTickets.size() + " fly tickets.");
                        Iterator<FlyTicket> flyTicketIterator = flyTickets.iterator();
                        while (flyTicketIterator.hasNext()) {
                            FlyTicket flyTicket = flyTicketIterator.next();
                            if (Integer.parseInt(flyTicket.getPrize()) > maxPrize) {
                                flyTicketIterator.remove();
                            }
                        }
                        logger.info("Found " + flyTickets.size() + " suitable fly tickets.");

                        return flyTickets;

                    } catch (IOException e) {
                        throw e;
                    } finally {
                        IOUtils.closeQuietly(inputStream);
                    }
                } else {
                    logger.warn("Response does not contains any data.");
                }
            } catch (IOException e) {
                throw e;
            } finally {
                IOUtils.closeQuietly(response);
            }
        } finally {
            IOUtils.closeQuietly(httpClient);
        }
        return new ArrayList<FlyTicket>();
    }

    private HttpGet createHttpGetRequest() throws URISyntaxException {

        SimpleDateFormat monthFormat = new SimpleDateFormat(MONTH_FORMAT);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost(AZAIR_SEARCH_HOST)
                .setPath(AZAIR_SEARCH_PATH)
                .setParameter("searchtype", "flexi")
                .setParameter("tp", "0")
                .setParameter("isOneway", "return")
                .setParameter("srcAirport", "Praha [PRG]")
                .setParameter("srcFreeAirport", "")
                .setParameter("srcTypedText", "")
                .setParameter("srcFreeTypedText", "")
                .setParameter("srcMC", "")
                .setParameter("dstAirport", "Kamkoliv [XXX]")
                .setParameter("anywhere", "true")
                .setParameter("dstTypedText", "Kamkoliv")
                .setParameter("dstFreeTypedText", "")
                .setParameter("dstMC", "")
                .setParameter("depmonth", monthFormat.format(departureDate))
                .setParameter("depdate", dateFormat.format(departureDate))
                .setParameter("arrmonth", monthFormat.format(returnDate))
                .setParameter("arrdate", dateFormat.format(returnDate))
                .setParameter("minDaysStay", String.valueOf(minDays))
                .setParameter("maxDaysStay", String.valueOf(maxDays))
                .setParameter("dep0", "true")
                .setParameter("dep1", "true")
                .setParameter("dep2", "true")
                .setParameter("dep3", "true")
                .setParameter("dep4", "true")
                .setParameter("dep5", "true")
                .setParameter("dep6", "true")
                .setParameter("arr0", "true")
                .setParameter("arr1", "true")
                .setParameter("arr2", "true")
                .setParameter("arr3", "true")
                .setParameter("arr4", "true")
                .setParameter("arr5", "true")
                .setParameter("arr6", "true")
                .setParameter("samedep", "true")
                .setParameter("samearr", "true")
                .setParameter("minHourStay", "0:45")
                .setParameter("maxHourStay", "23:20")
                .setParameter("minHourOutbound", "0:00")
                .setParameter("maxHourOutbound", "24:00")
                .setParameter("minHourInbound", "0:00")
                .setParameter("maxHourInbound", "24:00")
                .setParameter("autoprice", "true")
                .setParameter("adults", "1")
                .setParameter("children", "0")
                .setParameter("infants", "0")
                .setParameter("maxChng", "3")
                .setParameter("currency", "CZK")
                .setParameter("indexSubmit", "Hledat")
                .build();

        HttpGet httpGet = new HttpGet(uri);

        httpGet.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpGet.addHeader("Accept-Language", "cs-CZ,cs;q=0.8,en;q=0.6");
        httpGet.addHeader("Host", "www.azair.cz");
        httpGet.addHeader("Referer", "http://www.azair.cz/");
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");

        return httpGet;
    }


}
