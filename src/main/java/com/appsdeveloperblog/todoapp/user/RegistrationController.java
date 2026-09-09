package com.appsdeveloperblog.todoapp.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

	private static final String REGISTRATION_FORM_MODEL_ATTRIBUTE = "registrationRequest";
	private static final String REGISTRATION_VIEW = "register";

	private final UserService userService;
	private final UserAuthenticationService userAuthenticationService;

	public RegistrationController(UserService userService, UserAuthenticationService userAuthenticationService) {
		this.userService = userService;
		this.userAuthenticationService = userAuthenticationService;
	}

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		if (!model.containsAttribute(REGISTRATION_FORM_MODEL_ATTRIBUTE)) {
			model.addAttribute(REGISTRATION_FORM_MODEL_ATTRIBUTE, new RegistrationRequest("", "", "", "", ""));
		}
		return REGISTRATION_VIEW;
	}

	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute(REGISTRATION_FORM_MODEL_ATTRIBUTE) RegistrationRequest registrationRequest,
			BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) {
		if (bindingResult.hasErrors()) {
			return REGISTRATION_VIEW;
		}

		try {
			userService.registerUser(registrationRequest);
			userAuthenticationService.authenticateAndLogin(registrationRequest.email(), registrationRequest.password(),
					request, response);
		}
		catch (EmailAlreadyExistsException ex) {
			bindingResult.addError(new FieldError(REGISTRATION_FORM_MODEL_ATTRIBUTE, "email", ex.getMessage()));
			return REGISTRATION_VIEW;
		}

		return "redirect:/tbd";
	}

}
