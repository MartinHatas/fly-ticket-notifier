package cz.hatoff.ftn;

import cz.hatoff.ftn.checker.TicketChecker;
import cz.hatoff.ftn.config.ConfigurationKey;
import cz.hatoff.ftn.config.ConfigurationLoader;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FtnApplication {


    private static final Logger logger = LogManager.getLogger(FtnApplication.class);

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

        try {
            List<FlyTicket> flyTickets = new TicketChecker().checkTickets();
          //  new SmsSender().sendSMS(flyTickets);
        } catch (Exception e) {
            logger.info("Error", e);
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
