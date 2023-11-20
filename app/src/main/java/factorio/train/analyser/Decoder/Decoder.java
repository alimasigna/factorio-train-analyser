package factorio.train.analyser.Decoder;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Decoder{

    public String input = "0eNqV191ugkAQBeB3mWtInF1ggVdpmsafjd1EVwNoagzv3kWMadNpOHOJyucss4eBO20OF3/uQhyovVPYnmJP7dud+rCP68P02XA7e2opDP5IGcX1cTrq1uFAY0Yh7vwXtTy+Z+TjEIbg5/MfB7ePeDlufJd+8Dpze+mufpc/gIzOpz6dc4rTHyXHZHSjNmeT6F3o/Hb+rhqzP6LBRF7JpBVI+yL7IWn7z+EfNOcZbX6bTjAL1GQrmiyYJbb0vJjFalms4CpLscpSMJ3WdMt11rDpRFOqs9Ga5XKd064D0UZEpUKZtagFKgVzZOZorkbJwIPzbDWLTKFkZKVUKlZUKqVSiopTXdwa2AO1si4n1tUolUZSzErbL7FhBpwNU0smpPh9lYxEGmVSUmWL08FodzmLO8vgu/x5Y+QSWDG+65+DBlLxFDDjKjwZ8gZH8WA4HNXmhMXbgF3pmcXaLDwNcrzh1igfgSAUDo7ChFOkWDycIUWX4AThu9LC+cHjY/EnK0WT1PERp1Whjk8aV+l95PHG0v54wcno6rt+LrfmwjXGudrW1lbj+A1gl0k4";

    public String decode() throws UnsupportedEncodingException, DataFormatException{
        byte[] decodedBytes = Base64.getDecoder().decode(input.substring(1));
        
        
     Inflater decompresser = new Inflater();
     decompresser.setInput(decodedBytes, 0, decodedBytes.length);
     byte[] result = new byte[10000];
     int resultLength = decompresser.inflate(result);
     decompresser.end();

     String outputString = new String(result, 0, resultLength, "UTF-8");

     return outputString;
        
    }
}