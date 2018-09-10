package fourdsight.demo.urltrackerapi.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Sercan CELENK created by IntelliJ
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UrlNameRequest implements Serializable {
    private String url;
}
