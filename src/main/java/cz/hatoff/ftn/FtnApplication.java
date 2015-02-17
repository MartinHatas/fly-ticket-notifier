package cz.hatoff.ftn;

import cz.hatoff.ftn.checker.TicketChecker;
import cz.hatoff.ftn.config.ConfigurationKey;
import cz.hatoff.ftn.config.ConfigurationLoader;
import cz.hatoff.ftn.model.FlyTicket;
import cz.hatoff.ftn.sender.SmsSender;
import cz.hatoff.ftn.shorten.BitDoUrlShortenProvider;
import cz.hatoff.ftn.shorten.ShortenUrlProvider;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FtnApplication {

    private static final File sentTicketFile = new File("senttickets");

    private static final Logger logger = LogManager.getLogger(FtnApplication.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private Configuration configuration;

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
        initSendLogFile();

        final Runnable runCheckTickets = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat(TicketChecker.DATE_FORMAT);
                try {
                    List<FlyTicket> flyTickets = getFlyTicketsFromWeb(dateFormat);
                    if (!flyTickets.isEmpty()) {
                        filterAlreadySentTickets(flyTickets);
                        shortenUrl(flyTickets);
                        sendSmsWithTickets(flyTickets);
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

            private synchronized void filterAlreadySentTickets(List<FlyTicket> flyTickets) {
                Scanner scanner = null;
                try {
                    scanner = new Scanner(sentTicketFile);
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        Iterator<cz.hatoff.ftn.model.FlyTicket> iterator = flyTickets.iterator();
                        while (iterator.hasNext()) {
                            cz.hatoff.ftn.model.FlyTicket next = iterator.next();
                            if (line.contains(next.toString())) {
                                iterator.remove();
                                break;
                            }
                        }
                    }

                } catch (FileNotFoundException e) {
                    logger.error("File not found.", e);
                    initSendLogFile();
                    filterAlreadySentTickets(flyTickets);
                } finally {
                    IOUtils.closeQuietly(scanner);
                }


                try {
                    FileUtils.writeLines(sentTicketFile, "UTF-8", flyTickets, true);
                } catch (IOException e) {
                    logger.error("Failed to write new results to file.");
                }

                logger.info(String.format("Found '%d' new fly tickets.", flyTickets.size()));
            }
        };

        long delay = configuration.getLong(ConfigurationKey.CHECKING_TIME_MINUTES);
        logger.info(String.format("Creating scheduled task which will be checking fly ticket periodically every '%d' minutes.", delay));
        final ScheduledFuture<?> runCheckTicketsHandle = scheduler.scheduleWithFixedDelay(runCheckTickets, 0, delay, TimeUnit.MINUTES);
    }

    private void initSendLogFile() {
        if (!sentTicketFile.exists()) {
            try {
                boolean newFile = sentTicketFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create sent ticket file.", e);
            }
        }
        if (!sentTicketFile.canRead() || !sentTicketFile.canWrite()) {
            throw new RuntimeException("Cannot read or write to sent ticket file '" + sentTicketFile.getAbsolutePath() + "'.");
        }
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
