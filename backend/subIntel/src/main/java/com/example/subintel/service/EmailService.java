package com.example.subintel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.subintel.model.SubscriptionModel;
import com.example.subintel.model.UserModel;

@Service
public class EmailService {
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
	private JavaMailSender javaMailSender;

	public EmailService(JavaMailSender javaMailSender) {
		super();
		this.javaMailSender = javaMailSender;
	}

	public void sendSimpleMessage(String to, String subject, String text) {
		try {
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
			simpleMailMessage.setTo(to);
			simpleMailMessage.setSubject(subject);
			simpleMailMessage.setText(text);

			javaMailSender.send(simpleMailMessage);
			logger.info("Email sent successfully to {}", to);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error sending email to {}: {}", to, e.getMessage());
		}
	}

	public void sendSubscriptionReminder(UserModel user, SubscriptionModel subscription) {
		String to = user.getEmail();
		String subject = "Upcoming Subscription Payment Reminder: " + subscription.getMerchantName();
		String text = String.format(
				"Hi %s,\n\n" + "This is a reminder that your subscription for '%s' is due soon.\n\n"
						+ "  - Amount: $%.2f\n" + "  - Due Date: %s\n\n" + "Thank you for using SubIntel!",
				user.getFirstName(), subscription.getMerchantName(), Math.abs(subscription.getEstimatedAmount()),
				subscription.getNextDueDate().toString());

		sendSimpleMessage(to, subject, text);
	}

	public void sendPasswordResetEmail(UserModel user, String token) {
		String to = user.getEmail();
		String subject = "Your Password Reset Request for SubIntel";
		String resetUrl = "http://localhost:5173/reset-password?token=" + token;
		String text = String.format("Hi %s,\n\n"
				+ "You requested to reset your password. Click the link below to set a new one:\n\n" + "%s\n\n"
				+ "If you did not request this, please ignore this email.\n\n" + "Thanks,\nThe SubIntel Team",
				user.getFirstName(), resetUrl);

		sendSimpleMessage(to, subject, text);
	}
}
