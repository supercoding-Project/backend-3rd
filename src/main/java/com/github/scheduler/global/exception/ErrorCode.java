package com.github.scheduler.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Auth 에러코드
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "허용되지 않은 사용자입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생하였습니다."),
    USER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    USER_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일을 찾을 수 없습니다."),
    USERNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    BINDING_RESULT_ERROR(HttpStatus.BAD_REQUEST, "데이터 유효성에 문제가 있습니다."),
    CHECK_EMAIL_OR_PASSWORD(HttpStatus.NOT_FOUND, "이메일 또는 비밀번호가 올바르지 않습니다."),
    NOT_EQUAL_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),
    VALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "유효한 Access Token 입니다."),
    NOT_FOUND_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "존재하지 않는 Refresh Token 입니다."),
    NOT_FOUND_COOKIE(HttpStatus.NOT_FOUND, "쿠키 값이 존재하지 않습니다. 다시 로그인 해주세요."),
    INCORRECT_REFRESH_TOKEN(HttpStatus.CONFLICT, "Refresh Token 이 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다. 다시 로그인 해주세요."),

    //calendar 에러코드
    UNAUTHORIZED_CALENDAR(HttpStatus.UNAUTHORIZED, "권한이 허용된 캘린더가 아닙니다."),
    NOT_FOUND_CALENDAR(HttpStatus.NOT_FOUND, "캘린더를 찾을 수 없습니다."),
    NOT_SHARED_CALENDAR(HttpStatus.BAD_REQUEST, "공용 캘린더가 아닙니다."),
    DUPLICATED_CALENDAR(HttpStatus.CONFLICT, "이미 가입된 캘린더입니다."),
    INVALID_CALENDAR_TYPE(HttpStatus.BAD_REQUEST, "잘못된 캘린더 타입 입니다."),
    INVALID_CALENDAR_ID(HttpStatus.BAD_REQUEST, "잘못된 캘린더 ID 입니다."),
    DUPLICATED_CALENDAR_NAME(HttpStatus.CONFLICT, "이미 존재하는 캘린더 이름입니다."),
    CANNOT_REMOVE_SELF(HttpStatus.BAD_REQUEST, "자기 자신은 삭제할 수 없습니다."),
    CANNOT_REMOVE_OWNER(HttpStatus.BAD_REQUEST, "OWNER는 삭제할 수 없습니다."),
    NOT_JOINED_CALENDAR(HttpStatus.BAD_REQUEST, "해당 유저는 이 캘린더에 참여하고 있지 않습니다."),

    // invite 에러코드
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 초대 코드입니다."),


    //schedule 에러코드
    NOT_AUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자 입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자 정보가 존재하지 않습니다."),
    DATE_FORMAT_INCORRECT(HttpStatus.BAD_REQUEST, "지원하지 않는 날짜 형식입니다."),
    TODO_NOT_SUPPORTED(HttpStatus.CONFLICT, "TODO 형식은 지원하지 않습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 일정을 찾을 수 없습니다."),
    TODO_NOT_FOUND(HttpStatus.NOT_FOUND,"할 일을 찾을 수 없습니다."),
    NOT_UPDATE(HttpStatus.CONFLICT, "다른 사용자가 동시에 수정 중입니다."),
    NOT_DELETE(HttpStatus.CONFLICT, "다른 사용자가 동시에 삭제 중입니다."),
    INVALID_MENTION_USER(HttpStatus.BAD_REQUEST, "해당 사용자는 캘린더의 구성원이 아닙니다."),


    // MyPage 에러코드
    NOT_FOUND_USERINFO(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."),
    DELETE_USERINFO(HttpStatus.NOT_FOUND,"삭제된 사용자 정보입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "수정된 정보가 없습니다. 계속 진행 하시겠습니까?"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST,"캘린더 ID 형식이 올바르지 않습니다." ),
    NOT_SAVE_FILE(HttpStatus.BAD_REQUEST, "파일 저장에 실패했습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 유효하지 않습니다."),

    // Chatting 에러코드
    DUPLICATED_CHATROOM(HttpStatus.CONFLICT, "채팅방이 이미 존재합니다."),
    DUPLICATED_CHATROOM_USER(HttpStatus.CONFLICT, "해당 유저는 이미 채팅방에 참여 했습니다."),
    NOT_FOUND_CHATROOM(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),

    // admin 에러코드
    ADMIN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "관리자 권한이 없습니다."),
    ADMIN_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    ADMIN_USER_UPDATE_FAIL(HttpStatus.BAD_REQUEST, "유저 정보를 수정할 수 없습니다."),
    ADMIN_USER_DELETE_FAIL(HttpStatus.BAD_REQUEST, "유저 정보를 삭제할 수 없습니다."),
    ADMIN_SCHEDULE_ALREADY_DELETED(HttpStatus.CONFLICT, "이미 삭제된 일정입니다."),
    ADMIN_SCHEDULE_NOT_PUBLIC(HttpStatus.BAD_REQUEST, "공용 일정만 수정/삭제할 수 있습니다."),
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 문의글을 찾을 수 없습니다."),
    INQUIRY_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INQUIRY_PRIVATE_POST(HttpStatus.FORBIDDEN, "비공개 글입니다. 비밀번호 확인이 필요합니다."),
    INQUIRY_NOT_OWNER(HttpStatus.FORBIDDEN, "본인의 문의글만 수정/삭제할 수 있습니다."),
    INQUIRY_ALREADY_ANSWERED(HttpStatus.CONFLICT, "이미 답변된 문의글입니다."),
    INQUIRY_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "답변을 찾을 수 없습니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공지사항을 찾을 수 없습니다."),
    FAQ_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 FAQ를 찾을 수 없습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
