package com.raf.mrworldwide.services.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

import static com.raf.mrworldwide.services.email.EmailConstants.SUPPORT_EMAIL;

@Component
@Slf4j
public class EmailTemplateService {

	private final EmailService emailService;

	public EmailTemplateService(EmailService emailService) {
		this.emailService = emailService;
	}

	public void resetPassword(List<String> to, String name, String confirmationLink, String key) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("username", name);
		parameters.put("secretKey", key);
		parameters.put("confirmationLink", confirmationLink);

		sendEmail(SendgridConstants.SendgridTemplateId.FORGOT_PASSWORD, parameters, to);
	}

	private void sendEmail(String templateId, HashMap<String, Object> parameters, List<String> recipients) {
		Email email = new Email();
		email.setTemplateId(templateId);
		email.setFrom(SUPPORT_EMAIL);
		email.setTo(recipients);
		email.setTemplateParameters(parameters);

		emailService.sendEmail(email);
	}

}
