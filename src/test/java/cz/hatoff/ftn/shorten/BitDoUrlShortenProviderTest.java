package cz.hatoff.ftn.shorten;

public class BitDoUrlShortenProviderTest {

    @org.junit.Test
    public void testShortenUrl() throws Exception {

        ShortenUrlProvider shortenUrlProvider = new BitDoUrlShortenProvider();
        String shortUrl = shortenUrlProvider.shortenUrl("http://www.azair.cz/azfin.php?searchtype=flexi&tp=0&isOneway=return&srcAirport=Praha+%5BPRG%5D&srcFreeAirport=&srcTypedText=&srcFreeTypedText=&srcMC=&dstAirport=Kamkoliv+%5BXXX%5D&anywhere=true&dstTypedText=Kamkoliv&dstFreeTypedText=&dstMC=&depmonth=201501&depdate=2015-01-16&arrmonth=201502&arrdate=2015-02-01&minDaysStay=2&maxDaysStay=10&dep0=true&dep1=true&dep2=true&dep3=true&dep4=true&dep5=true&dep6=true&arr0=true&arr1=true&arr2=true&arr3=true&arr4=true&arr5=true&arr6=true&samedep=true&samearr=true&minHourStay=0%3A45&maxHourStay=23%3A20&minHourOutbound=0%3A00&maxHourOutbound=24%3A00&minHourInbound=0%3A00&maxHourInbound=24%3A00&autoprice=true&adults=1&children=0&infants=0&maxChng=2&currency=CZK&indexSubmit=Hledat");

    }
}