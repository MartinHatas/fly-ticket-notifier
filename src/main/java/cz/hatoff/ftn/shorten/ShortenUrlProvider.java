package cz.hatoff.ftn.shorten;


public interface ShortenUrlProvider {

    public String shortenUrl(String url) throws Exception;
}
