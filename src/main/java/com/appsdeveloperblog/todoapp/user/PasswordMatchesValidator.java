package com.appsdeveloperblog.todoapp.user;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegistrationRequest> {

	@Override
	public boolean isValid(RegistrationRequest registrationRequest, ConstraintValidatorContext context) {
		if (registrationRequest == null) {
			return true;
		}

		String password = registrationRequest.password();
		String confirmPassword = registrationRequest.confirmPassword();

		if (password == null || confirmPassword == null) {
			return true;
		}

		if (password.equals(confirmPassword)) {
			return true;
		}

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
				.addPropertyNode("confirmPassword")
				.addConstraintViolation();
		return false;
	}

}
