package fourdsight.demo.urltrackerapi.rest;

import fourdsight.demo.urltrackerapi.util.LoggerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Sercan CELENK created by IntelliJ
 */

public abstract class AbstractController implements LoggerSupport {

    protected List<String> getUrlsFromFile(MultipartHttpServletRequest request) {
        Map<String, MultipartFile> map = request.getMultiFileMap().toSingleValueMap();
        MultipartFile file = null;

        List<String> urls = new ArrayList<>();

        if (!org.springframework.util.CollectionUtils.isEmpty(map)) {
            for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
                file = entry.getValue();
            }
        }

        BufferedReader br;
        List<String> result = new ArrayList<>();
        try {

            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                urls.add(line);
            }

        } catch (IOException e) {
            getLogger().error(e.getMessage(), e);
        }

        return urls;
    }
}
