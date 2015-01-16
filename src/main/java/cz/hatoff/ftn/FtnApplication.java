package cz.hatoff.ftn;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cz.hatoff.ftn.checker.TicketChecker;
import cz.hatoff.ftn.config.ConfigurationKey;
import cz.hatoff.ftn.config.ConfigurationLoader;
import cz.hatoff.ftn.model.FlyTicket;
import cz.hatoff.ftn.sender.SmsSender;
import cz.hatoff.ftn.shorten.BitDoUrlShortenProvider;
import cz.hatoff.ftn.shorten.ShortenUrlProvider;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FtnApplication {

    private static final Logger logger = LogManager.getLogger(FtnApplication.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private Configuration configuration;

    Cache<Integer, FlyTicket> cache;

    public static void main(String[] args) {
        logger.info("Starting FLY TICKETS NOTIFIER server as standalone application.");
        new FtnApplication().startInternal(args);
    }

    private static void start(String[] args) {
        logger.info("Starting FLY TICKETS NOTIFIER server service.");
        new FtnApplication().startInternal(args);
    }

    private static void stop(String[] args) {
        logger.info("Starting FLY TICKETS NOTIFIER server service.");
        System.exit(0);
    }

    private void startInternal(String[] args) {
        loadConfiguration();
        initCache();

        final Runnable runCheckTickets = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat(TicketChecker.DATE_FORMAT);
                try {
                    List<FlyTicket> flyTickets = getFlyTicketsFromWeb(dateFormat);
                    if (!flyTickets.isEmpty()) {
                        List<FlyTicket> newFlyTickets = filterAlreadySentTickets(flyTickets);
                        shortenUrl(newFlyTickets);
                        sendSmsWithTickets(newFlyTickets);
                    } else {
                        logger.info("Does not found any fly tickets corresponding to given criteria.");
                    }
                } catch (Exception e) {
                    logger.info("Error occurred while checking for tickets or sending SMS messages.", e);
                }
            }

            private void shortenUrl(List<FlyTicket> newFlyTickets) {
                ShortenUrlProvider shortenUrlProvider = new BitDoUrlShortenProvider();
                for (FlyTicket newFlyTicket : newFlyTickets) {
                    try {
                        newFlyTicket.setShortUrl(shortenUrlProvider.shortenUrl(newFlyTicket.getFullUrl()));
                    } catch (Exception e) {
                        logger.error(String.format("Failed shorten url of fly ticket '%s'", newFlyTicket.toString()), e);
                    }
                }
            }

            private void sendSmsWithTickets(List<FlyTicket> newFlyTickets) throws Exception {
                new SmsSender(configuration.getStringArray(ConfigurationKey.PHONE_NUMBERS)).sendSMS(newFlyTickets);
            }

            private List<FlyTicket> getFlyTicketsFromWeb(SimpleDateFormat dateFormat) throws Exception {
                return new TicketChecker(
                                        Integer.parseInt(configuration.getString(ConfigurationKey.PRIZE_MAXIMAL)),
                                        dateFormat.parse(configuration.getString(ConfigurationKey.DATE_DEPARTURE)),
                                        dateFormat.parse(configuration.getString(ConfigurationKey.DATE_RETURN)),
                                        Integer.parseInt(configuration.getString(ConfigurationKey.DAYS_MIN)),
                                        Integer.parseInt(configuration.getString(ConfigurationKey.DAYS_MAX)),
                                        Integer.parseInt(configuration.getString(ConfigurationKey.CHANGES_MAX))
                                ).checkTickets();
            }

            private List<FlyTicket> filterAlreadySentTickets(List<FlyTicket> flyTickets) {
                List<FlyTicket> newFlyTickets = new ArrayList<FlyTicket>();
                for (FlyTicket flyTicket : flyTickets) {
                    if (cache.getIfPresent(flyTicket.hashCode()) == null) {
                        newFlyTickets.add(flyTicket);
                    }
                    cache.put(flyTicket.hashCode(), flyTicket);
                }
                logger.info(String.format("From '%d' suitable fly tickets are '%d' new.", flyTickets.size(), newFlyTickets.size()));
                return newFlyTickets;
            }
        };

        long delay = configuration.getLong(ConfigurationKey.CHECKING_TIME_MINUTES);
        logger.info(String.format("Creating scheduled task which will be checking fly ticket periodically every '%d' minutes.", delay));
        final ScheduledFuture<?> runCheckTicketsHandle = scheduler.scheduleWithFixedDelay(runCheckTickets, 0, delay, TimeUnit.MINUTES);
    }

    private void initCache() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(configuration.getLong(ConfigurationKey.CHECKING_TIME_MINUTES) + 5L, TimeUnit.MINUTES)
                .build();
    }

    private void loadConfiguration() {
        try {
            configuration = new ConfigurationLoader().loadConfiguration();
        } catch (Exception e) {
            logger.fatal("Failed to load configuration. Going to stop application.", e);
            stop(new String[]{});
        }
    }


}
