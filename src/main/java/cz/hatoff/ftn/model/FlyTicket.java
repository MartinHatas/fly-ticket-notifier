package cz.hatoff.ftn.model;


public class FlyTicket {

    private String destination;
    private String date;
    private String prize;
    private String length;

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

    @Override
    public String toString() {
        return "FlyTicket{" +
                "destination='" + destination + '\'' +
                ", date='" + date + '\'' +
                ", prize='" + prize + '\'' +
                ", length='" + length + '\'' +
                '}';
    }
}
