package fourdsight.demo.urltrackerapi.util;


import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Sercan CELENK created by IntelliJ
 */

public class ScreenShotTakeHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenShotTakeHelper.class);


    public static String getScreenShot(String url, String screenshotName) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setJavascriptEnabled(true);
        desiredCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "/tmp/phantomjs");
        WebDriver driver = null;
        String fileName = screenshotName + ".png";
        try {
            driver = new PhantomJSDriver(desiredCapabilities);
            driver.get(url);
            byte[] screenshotByte = ((PhantomJSDriver) driver).getScreenshotAs(OutputType.BYTES);
            String filePath = getResourcesFolder() + "images/" + fileName;
            FileUtils.writeByteArrayToFile(new File(filePath), screenshotByte);
            LOGGER.info(filePath);
            driver.quit();
            return fileName;
        } catch (Exception ex) {
            LOGGER.error("Error occurred while taking screenshot. Url {}", url, ex);
        }

        return "";
    }

    public static String getResourcesFolder() {
//        ClassLoader loader = ClassLoader.getSystemClassLoader();
//        String path = loader.getResource("").getPath();
//        return path;

        return "/tmp/";

    }
}
