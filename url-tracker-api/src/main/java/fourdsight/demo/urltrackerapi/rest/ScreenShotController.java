package fourdsight.demo.urltrackerapi.rest;

import fourdsight.demo.urltrackerapi.model.UrlNameRequest;
import fourdsight.demo.urltrackerapi.service.ScreenshotService;
import fourdsight.demo.urltrackerapi.util.LoggerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Sercan CELENK created by IntelliJ
 */

@RestController
public class ScreenShotController extends AbstractController implements LoggerSupport {

    @Autowired
    ScreenshotService screenshotService;

    @PostMapping(value = "/take/screenshot/single")
    public ResponseEntity<?> takeSingleScreenshot(@RequestBody UrlNameRequest urlRequest) {
        return ResponseEntity.ok(screenshotService.takeScreenshot(urlRequest));
    }

    @PostMapping(value = "/take/screenshot/multiple/v1")
    public ResponseEntity<?> takeMultipleLimitedScreenshotsV1(MultipartHttpServletRequest request) {
        return ResponseEntity.ok(screenshotService.takeScreenshotMultiple(getUrlsFromFile(request)));
    }

    @GetMapping(value = "/screenshots/{threadId}")
    public ResponseEntity<?> getScreenshots(@PathVariable String threadId) {
        return ResponseEntity.ok(screenshotService.getScreenshots(threadId));
    }

    @GetMapping(value = "/screenshot/{name}", produces = MediaType.IMAGE_JPEG_VALUE)
    public void getImage(@PathVariable String name, HttpServletResponse response) throws IOException {

        ClassPathResource imgFile = new ClassPathResource("images/" + name);

        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(imgFile.getInputStream(), response.getOutputStream());
    }


}
