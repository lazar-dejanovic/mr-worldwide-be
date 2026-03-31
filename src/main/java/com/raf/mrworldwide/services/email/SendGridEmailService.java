package com.raf.mrworldwide.services.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.ASM;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Personalization;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;

import static com.raf.mrworldwide.services.email.SendgridConstants.SendgridTemplateId.MAIL_SEND;
import static com.raf.mrworldwide.services.email.SendgridConstants.SendgridTemplateId.TEXT_HTML;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Default {@link EmailService} implementation.
 * Uses SendGrid as service provider.
 *
 */
@Service
public class SendGridEmailService implements EmailService {

	private static final Logger log = getLogger(SendGridEmailService.class);

	private SendGrid sendGrid;
    @Value("${email.enabled:false}")
    private boolean enableEmailService;
    @Value("${sendgrid.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        log.info("Email Service Enabled: {}", enableEmailService);
		sendGrid = new SendGrid(apiKey);
    }

    @Override
    public void sendEmail(Email toSend) {
        if (!enableEmailService) return;
        Mail mail = new Mail();
        mail.setFrom(new com.sendgrid.helpers.mail.objects.Email(toSend.getFrom()));
        mail.setSubject(toSend.getSubject());

        if (toSend.getHtmlContent() != null) {
            Content content = new Content();
            content.setType(TEXT_HTML);
            content.setValue(toSend.getHtmlContent());
            mail.addContent(content);
        }
        if (toSend.getTemplateId() != null) {
            mail.setTemplateId(toSend.getTemplateId());
        }

        Personalization personalization = new Personalization();
        toSend.getTo().forEach(to -> personalization.addTo(new com.sendgrid.helpers.mail.objects.Email(to)));
        if (!CollectionUtils.isEmpty(toSend.getCc())) {
            toSend.getCc().forEach(cc -> personalization.addCc(new com.sendgrid.helpers.mail.objects.Email(cc)));
        }

        if (toSend.getTemplateParameters() != null) {
            toSend.getTemplateParameters().keySet().forEach(key ->
					personalization.addDynamicTemplateData(key, toSend.getTemplateParameters().get(key)));
        }

        mail.addPersonalization(personalization);

        if (toSend.getAttachments() != null && !toSend.getAttachments().isEmpty()) {
            toSend.getAttachments().forEach(attachment -> {
                String attach = attachment.getAttachment();
                Attachments attachments = new Attachments();
                attachments.setContent(attach);
                attachments.setType(attachment.getType());
                attachments.setFilename(attachment.getAttachmentFileName());
                mail.addAttachments(attachments);
            });
        }

        if (toSend.getGroupId() != null) {
            ASM asm = new ASM();
            asm.setGroupId(toSend.getGroupId());
            if (toSend.getGroupIdsToDisplay() != null) {
                asm.setGroupsToDisplay(toSend.getGroupIdsToDisplay());
            }
            mail.setASM(asm);
        }

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint(MAIL_SEND);
        try {
            // SendGrid api throws IoException in case of a response with
            // the status code >=300, so there is no need to inspect the response.
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            log.debug("Response status code: {}", response.getStatusCode());
            log.debug("Response body: {}", response.getBody());
            log.debug("Response headers: {}", response.getHeaders());
            log.debug("Sent mail from {}", toSend.getFrom());
            log.debug("Sent mail to {} {}", toSend.getTo(), toSend.getCc());
        } catch (IOException e) {
            log.error("Error sending mail", e);
        }

    }

}
