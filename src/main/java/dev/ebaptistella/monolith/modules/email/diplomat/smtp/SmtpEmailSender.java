package dev.ebaptistella.monolith.modules.email.diplomat.smtp;

import dev.ebaptistella.monolith.modules.email.diplomat.EmailSender;
import dev.ebaptistella.monolith.modules.email.models.EmailDispatch;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test & !auth-e2e & !local-auth-e2e")
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    @Observed(name = "email.send", contextualName = "smtp send")
    public void send(EmailDispatch dispatch) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(dispatch.to());
        message.setSubject(dispatch.subject());
        message.setText(dispatch.body());
        mailSender.send(message);
        log.info("Email sent dispatchId={} to={}", dispatch.dispatchId(), dispatch.to());
    }
}
