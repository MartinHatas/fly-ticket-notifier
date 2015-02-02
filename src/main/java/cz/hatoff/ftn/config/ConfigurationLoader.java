package cz.hatoff.ftn.config;


import cz.hatoff.ftn.checker.TicketChecker;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConfigurationLoader {

    private static final String USER_CONFIG_FILE_NAME = "configuration.properties";
    private static final String DEFAULT_CONFIG_FILE_NAME = "default.properties";

    private static final Logger logger = LogManager.getLogger(ConfigurationLoader.class);

    public Configuration loadConfiguration() throws Exception{
        logger.info(String.format("Going to load configuration from file '%s'", USER_CONFIG_FILE_NAME));
        try {
            PropertiesConfiguration defaultConfiguration = new PropertiesConfiguration(DEFAULT_CONFIG_FILE_NAME);

            PropertiesConfiguration userConfiguration = new PropertiesConfiguration(USER_CONFIG_FILE_NAME);

//            TODO: reload configuration need validation of values
//            userConfiguration.setReloadingStrategy(new FileChangedReloadingStrategy());
//            userConfiguration.addConfigurationListener();

            CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
            compositeConfiguration.addConfiguration(userConfiguration);
            compositeConfiguration.addConfiguration(defaultConfiguration);

            checkMandatoryParameters(compositeConfiguration);
            checkDates(compositeConfiguration);
            checkDays(compositeConfiguration);


            logger.info("Running application with following settings:");
            Iterator<String> keys = compositeConfiguration.getKeys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = compositeConfiguration.getString(key);
                logger.info(String.format("%s=%s", key, value));
            }

            return compositeConfiguration;
        } catch (Exception e) {
            logger.fatal(String.format("Cannot load configuration file '%s' properly.", USER_CONFIG_FILE_NAME));
            throw new IllegalArgumentException("Failed to load configuration", e);
        }
    }

    private void checkDays(Configuration configuration) {
        int minDays = Integer.parseInt(configuration.getString(ConfigurationKey.DAYS_MIN));
        int maxDays = Integer.parseInt(configuration.getString(ConfigurationKey.DAYS_MAX));

        if (minDays >= maxDays) {
            logger.info(String.format("Detected that minimum days '%d' is greater or equals than maximum days '%d'. Using '%d' as maximum days.", minDays, maxDays, minDays + 1));
            configuration.setProperty(ConfigurationKey.DAYS_MAX, String.valueOf(minDays + 1));
        }
    }

    private void checkDates(Configuration configuration) throws ParseException {
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(TicketChecker.DATE_FORMAT);
        Date departureDate = dateFormat.parse(configuration.getString(ConfigurationKey.DATE_DEPARTURE));
        if (departureDate.before(today)) {
            logger.info(String.format("Detected departure date '%s' is in past. Going to use '%s' as departure date.", dateFormat.format(departureDate), dateFormat.format(today)));
            departureDate = today;
            configuration.setProperty(ConfigurationKey.DATE_DEPARTURE, dateFormat.format(today));
        }

        Date returnDate = dateFormat.parse(configuration.getString(ConfigurationKey.DATE_RETURN));
        if (returnDate.before(departureDate)){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(departureDate);
            calendar.add(Calendar.MONTH, 1);
            Date plusMontDate = calendar.getTime();
            logger.info(String.format("Detected that return date '%s' is before departure date '%s'. Going to use this date '%s' as return date.", dateFormat.format(returnDate), dateFormat.format(departureDate), dateFormat.format(plusMontDate)));
            returnDate = plusMontDate;
            configuration.setProperty(ConfigurationKey.DATE_RETURN, dateFormat.format(returnDate));
        }
    }

    private void checkMandatoryParameters(Configuration configuration) {
        String[] phoneNumbers = configuration.getStringArray(ConfigurationKey.PHONE_NUMBERS);
        if (phoneNumbers.length == 0) {
            throw new RuntimeException(String.format("Cannot found mandatory property '%s' in configuration file '%s'.", ConfigurationKey.PHONE_NUMBERS, USER_CONFIG_FILE_NAME));
        }
        String[] trimmedPhoneNumbers = new String[phoneNumbers.length];
        for(int i = 0; i < phoneNumbers.length; i++) {
            trimmedPhoneNumbers[i] = phoneNumbers[i].trim();
        }
        configuration.setProperty(ConfigurationKey.PHONE_NUMBERS, trimmedPhoneNumbers);
    }
}
