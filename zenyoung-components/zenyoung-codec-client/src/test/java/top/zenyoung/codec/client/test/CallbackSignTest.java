package top.zenyoung.codec.client.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import top.zenyoung.codec.client.CodecUploadClient;
import top.zenyoung.codec.client.CodecUploadClientDefault;
import top.zenyoung.codec.client.vo.CallbackCodecUrl;
import top.zenyoung.codec.client.vo.CallbackResutReq;
import top.zenyoung.codec.client.vo.CallbackResutResp;
import top.zenyoung.codec.client.vo.UploadAuthorize;
import top.zenyoung.web.vo.EnumData;

import java.util.ArrayList;

@Slf4j
@RunWith(SpringRunner.class)
public class CallbackSignTest {

    @Test
    public void callbackSignTest() {
        final CallbackResutReq req = new CallbackResutReq();
        req.setId("b16b52ba-1e15-444e-a111-91d4a6c99889");
        req.setAccount("upload001");
        req.setBizId("d1bf355ce9464c5582dbd42019d1d00a");
        req.setUniqueCode("83f74f2bc493c8d5d659327317ea298a");
        req.setStamp(1606873929127L);
        req.setStatus(EnumData.of(41, "发布上传成功"));
        req.setUrls(new ArrayList<CallbackCodecUrl>(3) {
            {
                add(new CallbackCodecUrl("http://vod.dev-ipower.top/csw-system/202011/hr4/1941/d1bf355ce9464c5582dbd42019d1d00a/b16b52ba-1e15-444e-a111-91d4a6c99889/09b672551fedccb58481d909d610ada2.m3u8", "101", 198593795L, 2526200L));
                add(new CallbackCodecUrl("http://vod.dev-ipower.top/csw-system/202011/hr4/1941/d1bf355ce9464c5582dbd42019d1d00a/b16b52ba-1e15-444e-a111-91d4a6c99889/f9c0ad357a4df4be266ec0ebadc33c52.m3u8", "102", 198593795L, 2526200L));
                add(new CallbackCodecUrl("http://vod.dev-ipower.top/csw-system/202011/hr4/1941/d1bf355ce9464c5582dbd42019d1d00a/b16b52ba-1e15-444e-a111-91d4a6c99889/873755b0b18b4a0996afd4bed57af571.jpg", "201", 103345L, null));
            }
        });
        req.setMsg("发布上传成功");
        req.setSign("038f04ec9e8edb2393693d6891e116c0ee57ce1f");
        //
        final CodecUploadClient client = CodecUploadClientDefault.getInstance("http://upload.codec.dev-ipower.top", "upload001", "1234567890");
        final CallbackResutResp resp = client.callbackHandler(req, ret -> log.info("signTest-ret=> {}", ret));
        log.info("signTest-resp=> {}", resp);
    }

    @Test
    public void createAuthorizeTest() {
        final CodecUploadClient client = CodecUploadClientDefault.getInstance("http://upload.codec.dev-ipower.top", "upload007", "77777770");
        final UploadAuthorize auth = client.createAuthorize("5e5d7fa5-a56f-4249-b87c-990ce516d396", "06a19cd186de470aa92be0ec18f81a16", "cs/1957/06a19cd186de470aa92be0ec18f81a16/", "101", "102", "201");
        log.info("auth=> {}", auth);
    }
}
