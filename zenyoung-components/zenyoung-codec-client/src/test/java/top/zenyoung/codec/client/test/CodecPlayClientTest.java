package top.zenyoung.codec.client.test;


import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import top.zenyoung.codec.client.CodecPlayClient;
import top.zenyoung.codec.client.CodecPlayClientDefault;
import top.zenyoung.codec.client.vo.PlayTicket;

@Slf4j
@RunWith(SpringRunner.class)
public class CodecPlayClientTest {

    private CodecPlayClient playClient;

    @Before
    public void beforeInit() {
        this.playClient = CodecPlayClientDefault.getInstance("https://cvod.chaosw.com", "webplay", "adec4dea9478");
    }

    @Test
    public void playUrlTest() {
        final String url = this.playClient.getPlayUrl(new PlayTicket() {

            @Override
            public String getToken() {
                return "eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJiOWZmNzhmNS1jZTBmLTQzZDUtOWUzZC1lOWU2ZTEwNWFlMzYiLCJpZCI6ImIwYTQxY2QwYWNiMjMwMWYyMTJiZGJjMDI5ZGNlZjc2IiwiZXh0cyI6bnVsbCwiYWNjb3VudCI6IndlYnBsYXkiLCJyb2xlcyI6bnVsbCwiaWF0IjoxNjA3NDA2MTMxLCJuYmYiOjE2MDc0MDYxMzEsImV4cCI6MTYwNzQxMDYzNn0.Pp8O71n6WaArAU5vgFMH64iKpmUAlnAm4s1iyt2BkkGJzdsXq7o-a4Aq6Kl8-QYl1y363hmFRG5wAqjMXQXFZw";
            }

            @Override
            public Long getExpire() {
                return 1604814799000L;
            }
        }, "other");
        log.info("play-url=> {}", url);
    }

}
