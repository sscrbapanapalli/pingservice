package com.cmacgm;

import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServlet;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 * Servlet implementation class SMPTemail
 */
public class SendMail extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public Boolean SendMail(String subject, String toMail, String text) {

		Boolean mailsuccess = false;
		try {

			String toAdd = toMail;

			Boolean flag = true;
			Session session;
			Properties props = new Properties();
			props.load(SendMail.class.getClassLoader().getResourceAsStream("config.properties"));
			props.put("mail.transport.protocol", props.get("mail.transport.protocol"));
			props.put("mail.smtp.host", props.get("mail.smtp.host"));
			String fromAdd = (String) props.get("mail.fromAddress");

			// tls enabled
			props.put("mail.smtp.starttls.enable", props.get("mail.smtp.starttls.enable"));
			props.put("mail.smtp.port", props.get("mail.smtp.port"));

			MailSSLSocketFactory sf = null;
			try {
				sf = new MailSSLSocketFactory();
			} catch (GeneralSecurityException e1) {
				e1.printStackTrace();
			}
			sf.setTrustAllHosts(true);
			props.put("mail.smtp.ssl.socketFactory", props.get("mail.smtp.ssl.socketFactory"));
			session = Session.getInstance(props, null);
			
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(fromAdd));
			message.addRecipient(RecipientType.TO, new InternetAddress(toAdd));
			message.setSubject(subject);
			message.setContent(text, "text/html");			
			Transport.send(message);
			
			mailsuccess = true;
			flag = true;
			if (flag == false) {
				mailsuccess = false;

			}
		} catch (Exception e) {
			e.printStackTrace();
			mailsuccess = false;
		}
		return mailsuccess;
	}

}
