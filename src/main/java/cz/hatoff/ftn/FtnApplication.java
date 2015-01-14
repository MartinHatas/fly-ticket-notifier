package cz.hatoff.ftn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class FtnApplication {

    private static final Logger logger = LogManager.getLogger(FtnApplication.class);

    private int maxPrize;
    private Date fromDate, toDate;
    private int minDays, maxDays;
    private int maxChanges;
    private String phoneNumber;



    public static void main(String[] args) {
        logger.info("Starting Inspire Searching server service.");
        final FtnApplication app = new FtnApplication();
        start(app, args);
    }

    private static void start(String[] args) throws Exception {
        logger.info("Starting Inspire Searching server service.");
        final FtnApplication app = new FtnApplication();
        start(app, args);
    }

    private static void stop(String[] args) throws Exception {
        logger.info("Stopping Inspire Searching server service.");
        System.exit(0);
    }

    private static void start(FtnApplication app, String[] args) {
    }
}
