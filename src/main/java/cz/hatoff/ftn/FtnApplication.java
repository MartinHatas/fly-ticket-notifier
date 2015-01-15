package cz.hatoff.ftn;

import cz.hatoff.ftn.checker.TicketChecker;
import cz.hatoff.ftn.config.ConfigurationKey;
import cz.hatoff.ftn.config.ConfigurationLoader;
import cz.hatoff.ftn.model.FlyTicket;
import cz.hatoff.ftn.sender.SmsSender;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FtnApplication {

    private static final Logger logger = LogManager.getLogger(FtnApplication.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Configuration configuration;

    public static void main(String[] args) {
        logger.info("Starting FLY TICKETS NOTIFIER server as standalone server.");
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

        final Runnable runCheckTickets = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat(TicketChecker.DATE_FORMAT);
                try {

                    List<FlyTicket> flyTickets = new TicketChecker(
                            Integer.parseInt(configuration.getString(ConfigurationKey.PRIZE_MAXIMAL)),
                            dateFormat.parse(configuration.getString(ConfigurationKey.DATE_DEPARTURE)),
                            dateFormat.parse(configuration.getString(ConfigurationKey.DATE_RETURN)),
                            Integer.parseInt(configuration.getString(ConfigurationKey.DAYS_MIN)),
                            Integer.parseInt(configuration.getString(ConfigurationKey.DAYS_MAX)),
                            Integer.parseInt(configuration.getString(ConfigurationKey.CHANGES_MAX))
                    ).checkTickets();

                    new SmsSender(configuration.getStringArray(ConfigurationKey.PHONE_NUMBERS)).sendSMS(flyTickets);
                } catch (Exception e) {
                    logger.info("Error occurred while checking for tickets or sending SMS messages.", e);
                }
            }
        };

        final ScheduledFuture<?> runCheckTicketsHandle = scheduler.scheduleWithFixedDelay(runCheckTickets, 0, 15, TimeUnit.MINUTES);
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
