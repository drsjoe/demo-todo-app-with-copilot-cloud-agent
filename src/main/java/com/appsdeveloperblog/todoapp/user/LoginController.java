package com.appsdeveloperblog.todoapp.user;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

	@GetMapping("/login")
	public String showLoginPage(@RequestParam(name = "error", required = false) String error,
			@RequestParam(name = "logout", required = false) String logout, Model model) {
		model.addAttribute("loginError", error != null);
		model.addAttribute("logoutSuccess", logout != null);
		return "login";
	}

	@GetMapping("/tbd")
	public String showTbdPage(Principal principal, Model model) {
		model.addAttribute("email", principal.getName());
		return "tbd";
	}

}
