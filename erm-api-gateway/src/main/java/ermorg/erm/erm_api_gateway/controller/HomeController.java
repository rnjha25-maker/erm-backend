package ermorg.erm.erm_api_gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
	@RequestMapping(value = "/{path:[^\\\\.]*}")
	public String redirect() {
		return "forward:/index.html";
	}
}
