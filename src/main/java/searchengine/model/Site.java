package searchengine.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Site")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StatusType status;

    @Column(name = "status_time")
    private LocalDateTime time;
    @Column(name = "last_error")
    private String text;
    private String url;
    @Column(name = "name")
    private String nameSite;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

//    public StatusType getStatus() {
//        return status;
//    }
//
//    public void setStatus(StatusType status) {
//        this.status = status;
//    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNameSite() {
        return nameSite;
    }

    public void setNameSite(String nameSite) {
        this.nameSite = nameSite;
    }
}
