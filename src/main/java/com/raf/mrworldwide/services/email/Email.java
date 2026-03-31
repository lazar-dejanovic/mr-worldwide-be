package com.raf.mrworldwide.services.email;

import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.raf.mrworldwide.services.email.EmailConstants.TXT_PLAIN;

@Data
public class Email {
    private Collection<String> to;
    private String from;
    private String subject;
    private String htmlContent;
    private Collection<String> cc;
    private List<Attachment> attachments;

    // template sending
    private String templateId;
    private Map<String, Object> templateParameters;

    private Integer groupId; // unsubscribe group
    private int[] groupIdsToDisplay;

    public void addAttachments(Attachment... attachments) {
        this.attachments = Arrays.asList(attachments);
	}

    @Data
    public static class Attachment {
		private String attachment;
        private byte[] attachmentBytes;
        private String attachmentType;
        private String attachmentFileName;

        public Attachment(String attachment, String attachmentFileName) {
            this.attachment = attachment;
            this.attachmentFileName = attachmentFileName;
        }

        public Attachment(byte[] attachmentBytes, String attachmentFileName) {
            this.attachmentBytes = attachmentBytes;
            this.attachmentFileName = attachmentFileName;
        }

        public String getAttachment() {
            return attachmentBytes != null ?
					Base64.getEncoder().encodeToString(attachmentBytes) :
					Base64.getEncoder().encodeToString(attachment.getBytes(StandardCharsets.UTF_8));
        }

        public String getType() {
            return attachmentType != null ? attachmentType : TXT_PLAIN;
        }
    }

}
