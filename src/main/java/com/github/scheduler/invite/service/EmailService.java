package com.github.scheduler.invite.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    // 초대코드 이메일로 전송
    @Async
    public void sendInviteEmails(List<String> emailList, String inviteCode, Long calendarId) {
        for (String email : emailList) {
            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

                messageHelper.setFrom("violetcarrot21@gmail.com");

                messageHelper.setTo(email);
                messageHelper.setSubject("공용 캘린더 초대 코드");

                // 이메일 내용 설정 (HTML 형식)
                String emailContent = "<h2>초대 코드 안내</h2>"
                        + "<p>아래의 초대 코드를 사용하여 캘린더에 가입할 수 있습니다.</p>"
                        + "<p><strong>초대 코드: " + inviteCode + "</strong></p>"
                        + "<p>아래 링크를 클릭하여 가입하세요:</p>"
                        + "<a href='https://example.com/join?inviteCode=" + inviteCode + "'>초대 코드로 가입하기</a>";

                messageHelper.setText(emailContent, true); // HTML 형식으로 전송

                javaMailSender.send(message);
                log.info("초대 코드 {}가 {}에게 이메일로 전송됨", inviteCode, email);

            } catch (Exception e) {
                log.error("이메일 전송 실패! 대상: {}, 오류 메시지: {}", email, e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("이메일 전송에 실패하였습니다.");
            }
        }
    }
}
