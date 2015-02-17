package cz.hatoff.ftn.model;


public class FlyTicket {

    private String destination;
    private String date;
    private String prize;
    private String length;
    private String fullUrl;
    private String shortUrl;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
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
        String format = String.format("%s %s %s", shortUrl, prize, destination);
        return format.substring(0, Math.min(format.length(), 37));
    }

    @Override
    public String toString() {
        return "FlyTicket{" +
                "destination='" + destination + '\'' +
                ", date='" + date + '\'' +
                ", prize='" + prize + '\'' +
                ", length='" + length + '\'' +
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
        if (length != null ? !length.equals(flyTicket.length) : flyTicket.length != null) return false;
        if (prize != null ? !prize.equals(flyTicket.prize) : flyTicket.prize != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = destination != null ? destination.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (prize != null ? prize.hashCode() : 0);
        result = 31 * result + (length != null ? length.hashCode() : 0);
        return result;
    }
}
