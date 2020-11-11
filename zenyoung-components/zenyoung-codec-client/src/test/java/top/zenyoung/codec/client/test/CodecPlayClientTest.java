package top.zenyoung.codec.client.test;


import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import top.zenyoung.codec.client.CodecUploadClient;
import top.zenyoung.codec.client.CodecUploadClientDefault;
import top.zenyoung.codec.client.vo.UploadAuthorize;

@Slf4j
@RunWith(SpringRunner.class)
public class CodecPlayClientTest {

    private CodecUploadClient uploadClient;

    @Before
    public void beforeInit() {
        this.uploadClient = CodecUploadClientDefault.getInstance("http://baidu.com", "acount", "passwd");
    }

    @Test
    public void testCreateAuthorize() {
        log.info("testCreateAuthorize....");
        final UploadAuthorize authorize = this.uploadClient.createAuthorize("bucket", "bizId", "dir", null);
        log.info("authorize=> {}", authorize);
    }
}
