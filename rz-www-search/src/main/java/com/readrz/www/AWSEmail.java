package com.readrz.www;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class AWSEmail {
	
	private static final String smtpHost = "email-smtp.us-east-1.amazonaws.com";
	private static final String smtpPort = "25";
	private static final String smtpUser = "AKIAJTG7ZSDHEYA7VIXQ";
	private static final String smtpPass = "AmgFSw2nee09R9ewIFQoIf90vvFXlcsSN6u62ouBgUcF";
	private static final String smtpFrom = "admin@readrz.com";
	private static final String smtpEncoding = "UTF-8";

	public static final void sendOnePlain(String toAddress, String subject, String text) throws AddressException, MessagingException {
		
        // Get a Properties object
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", smtpHost);
        props.setProperty("mail.smtp.port", smtpPort);
        props.setProperty("mail.smtp.user", smtpUser);
        props.setProperty("mail.smtp.password", smtpPass);
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable","true");

        Session session = Session.getDefaultInstance(props, null);
        MimeMessage message = new MimeMessage(session);
        
        message.setFrom(new InternetAddress(smtpFrom));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
        message.setSubject(subject);
        message.setText(text, smtpEncoding);

        Transport transport = session.getTransport("smtp");
        try {
	        transport.connect(smtpHost, smtpUser, smtpPass);
	        transport.sendMessage(message, message.getAllRecipients());
        } finally {
        	transport.close();
        }
	}
}
