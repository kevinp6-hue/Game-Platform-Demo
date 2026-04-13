package Kevin.Peyton.Game.Platform.Demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

	@GetMapping("/")
	public String home() {
		return "OK";
	}
}
