package main.com.test.es;

import java.time.LocalDateTime;

public class Index {

    private LocalDateTime dateTime;

    private String source;


    public Index(LocalDateTime dateTime, String source) {
        this.dateTime = dateTime;
        this.source = source;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
