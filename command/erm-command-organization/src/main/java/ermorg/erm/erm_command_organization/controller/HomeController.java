package ermorg.erm.erm_command_organization.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
    @RequestMapping(value = "/{path:[^\\\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }
}
