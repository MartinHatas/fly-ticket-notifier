package cz.hatoff.ftn;

import cz.hatoff.ftn.checker.TicketChecker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class FtnApplication {

    private static final Logger logger = LogManager.getLogger(FtnApplication.class);

    public static void main(String[] args) {
        logger.info("Starting FLY TICKETS NOTIFIER server as standalone server.");
        final FtnApplication app = new FtnApplication();
        start(app, args);
    }

    private static void start(String[] args) {
        logger.info("Starting FLY TICKETS NOTIFIER server service.");
        final FtnApplication app = new FtnApplication();
        start(app, args);
    }

    private static void stop(String[] args) {
        logger.info("Starting FLY TICKETS NOTIFIER server service.");
        System.exit(0);
    }

    private static void start(FtnApplication app, String[] args) {
        try {
            List<FlyTicket> flyTickets = new TicketChecker().checkTickets();
            new SmsSender().sendSMS(flyTickets);
        } catch (Exception e) {
            logger.info("Error", e);
        }
    }
}
