package cz.hatoff.ftn.checker;


import cz.hatoff.ftn.model.FlyTicket;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TicketParser {

    private final Pattern resultListPattern = Pattern.compile("<div class=\"list\" id=\"reslist\">");
    private final Pattern dateAndDestinationPattern = Pattern.compile(".*<span class=\"date\">.*(\\d{2}\\.\\d{2}\\.\\d{4}).*<span class=\"aeroDetail\">(.*?)</span>.*");
    private final Pattern prizePattern = Pattern.compile(".*<span class=\"sumPrice\">.*<span class=\"bp\">(\\d+).*");
    private final Pattern lengthPattern = Pattern.compile(".*<span class=\"bookmarkedMeta\">.*(\\d+).*");


    private enum SearchState{
        RESULT_LIST, DATE_AND_DESTINATION, PRIZE, LENGTH
    }

    private SearchState searchState = SearchState.RESULT_LIST;

    public List<FlyTicket> parseFlyTickets(List<String> lines) {
        List<FlyTicket> flyTickets = new ArrayList<FlyTicket>();

        FlyTicket flyTicket = null;
        for (String line : lines) {
            switch (searchState) {
                case RESULT_LIST: {
                    if (resultListPattern.matcher(line).matches()){
                        searchState = SearchState.DATE_AND_DESTINATION;
                    }
                    break;
                }
                case DATE_AND_DESTINATION: {
                    Matcher dateAndDestinationMatcher = dateAndDestinationPattern.matcher(line);
                    if (dateAndDestinationMatcher.matches()){
                        flyTicket = new FlyTicket();
                        flyTicket.setDate(dateAndDestinationMatcher.group(1));
                        flyTicket.setDestination(dateAndDestinationMatcher.group(2));
                        searchState = SearchState.PRIZE;
                    }
                    break;
                }
                case PRIZE: {
                    Matcher prizeMatcher = prizePattern.matcher(line);
                    if (prizeMatcher.matches()) {
                        flyTicket.setPrize(prizeMatcher.group(1));
                        searchState = SearchState.LENGTH;
                    }
                    break;
                }
                case LENGTH: {
                    Matcher lengthMatcher = lengthPattern.matcher(line);
                    if (lengthMatcher.matches()) {
                        flyTicket.setLength(lengthMatcher.group(1));
                        searchState = SearchState.DATE_AND_DESTINATION;
                        flyTickets.add(flyTicket);
                    }
                    break;
                }
            }
        }

        return flyTickets;
    }

}
