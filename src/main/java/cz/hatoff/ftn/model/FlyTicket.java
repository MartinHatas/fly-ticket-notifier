package cz.hatoff.ftn.model;


import java.text.Normalizer;

public class FlyTicket {

    private String destination;
    private String date;
    private String prize;
    private String fullUrl;
    private String shortUrl;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = stripAccents(destination);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = stripAccents(date);
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String toSMS(){
        String format = String.format("%s %s;%s;%s", shortUrl, prize, date.substring(0, 5), destination);
        return format.substring(0, Math.min(format.length(), 37));
    }

    public static String stripAccents(String text) {
        String result = text == null ? null :
                Normalizer.normalize(text, Normalizer.Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return result;
    }


    @Override
    public String toString() {
        return "FlyTicket{" +
                "destination='" + destination + '\'' +
                ", date='" + date + '\'' +
                ", prize='" + prize + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlyTicket flyTicket = (FlyTicket) o;

        if (date != null ? !date.equals(flyTicket.date) : flyTicket.date != null) return false;
        if (destination != null ? !destination.equals(flyTicket.destination) : flyTicket.destination != null)
            return false;
        if (prize != null ? !prize.equals(flyTicket.prize) : flyTicket.prize != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = destination != null ? destination.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (prize != null ? prize.hashCode() : 0);
        return result;
    }
}
