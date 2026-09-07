package com.appsdeveloperblog.todoapp.user;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

	private static final String REGISTRATION_FORM_MODEL_ATTRIBUTE = "registrationRequest";
	private static final String REGISTRATION_VIEW = "register";

	private final UserService userService;

	public RegistrationController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/register")
	public String showRegistrationForm(@RequestParam(name = "success", required = false) String success, Model model) {
		if (!model.containsAttribute(REGISTRATION_FORM_MODEL_ATTRIBUTE)) {
			model.addAttribute(REGISTRATION_FORM_MODEL_ATTRIBUTE, new RegistrationRequest("", "", "", "", ""));
		}
		model.addAttribute("registrationSuccess", success != null);
		return REGISTRATION_VIEW;
	}

	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute(REGISTRATION_FORM_MODEL_ATTRIBUTE) RegistrationRequest registrationRequest,
			BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			return REGISTRATION_VIEW;
		}

		try {
			userService.registerUser(registrationRequest);
		}
		catch (EmailAlreadyExistsException ex) {
			bindingResult.addError(new FieldError(REGISTRATION_FORM_MODEL_ATTRIBUTE, "email", ex.getMessage()));
			return REGISTRATION_VIEW;
		}

		return "redirect:/register?success";
	}

}
