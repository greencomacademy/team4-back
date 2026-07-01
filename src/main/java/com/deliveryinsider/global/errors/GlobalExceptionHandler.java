package com.deliveryinsider.global.errors;


import com.deliveryinsider.global.errors.custom.*;
import com.deliveryinsider.global.responses.GlobalRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NotRegisteredException.class)
    public ResponseEntity<GlobalRes<String>> notRegisteredHandle(NotRegisteredException e){
        return ResponseEntity.status(401).body(
            GlobalRes.<String>builder()
                .code("E01")
                .message("로그인 에러")
                .data(e.getMessage())
                .build()
        );
    }
    @ExceptionHandler(NotRegisteredStoreException.class)
    public ResponseEntity<GlobalRes<String>> notRegisteredStoreException(NotRegisteredStoreException e){
        return ResponseEntity.status(404).body(
            GlobalRes.<String>builder()
                .code("E31")
                .message("매장 등록 문제")
                .data(e.getMessage())
                .build()
        );
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GlobalRes<String>> authenticationHandle(AuthenticationException e){
        return ResponseEntity.status(401).body(
            GlobalRes.<String>builder()
                .code("E02")
                .message("UNAUTHENTICATED_ERROR")
                .data("로그인이 필요한 서비스 입니다.")
                .build()
        );
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GlobalRes<String>> accessDeniedHandle(AccessDeniedException e){
        return ResponseEntity.status(403).body(
            GlobalRes.<String>builder()
                .code("E03")
                .message("UNAUTHORIZED_ERROR")
                .data("권한이 없습니다.")
                .build()
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<GlobalRes<String>> InvalidTokenHandle(InvalidTokenException e){
        return ResponseEntity.status(401).body(
            GlobalRes.<String>builder()
                .code("E04")
                .message("토큰 이상")
                .data(e.getMessage())
                .build()
        );
    }
    @ExceptionHandler(DeletedRecordException.class)
    public ResponseEntity<GlobalRes<String>> deletedRecordExceptionHandle(DeletedRecordException e){
        return ResponseEntity.status(404).body(
            GlobalRes.<String>builder()
                .code("E10")
                .message("DELETED_RECORD_ERROR")
                .data(e.getMessage())
                .build()
        );
    }
    @ExceptionHandler(DuplicatedRecordException.class)
    public ResponseEntity<GlobalRes<String>> duplicatedRecordHandle(DuplicatedRecordException e){
        return ResponseEntity.status(409).body(
            GlobalRes.<String>builder()
                .code("E11")
                .message("DUPLICATED_RECORD_ERROR")
                .data(e.getMessage())
                .build()
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalRes<Map<String, String>>> methodArgumentNotValidHandle(
        MethodArgumentNotValidException e
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            String message = fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : "유효하지 않은 값입니다.";

            errors.putIfAbsent(fieldError.getField(), message);
        }

        for (ObjectError globalError : e.getBindingResult().getGlobalErrors()) {
            String message = globalError.getDefaultMessage() != null
                ? globalError.getDefaultMessage()
                : "요청 조건이 올바르지 않습니다.";

            errors.putIfAbsent("request", message);
        }

        return ResponseEntity.status(400).body(
            GlobalRes.<Map<String, String>>builder()
                .code("E21")
                .message("요청 파라미터에 이상이 있습니다.")
                .data(errors)
                .build()
        );
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GlobalRes<String>> methodArgumentTypeMismatchHandle(MethodArgumentTypeMismatchException e){
        return ResponseEntity.status(400).body(
            GlobalRes.<String>builder()
                .code("E21")
                .message("요청 파라미터에 이상이 있습니다.")
                .data(String.format("%s : 필드를 확인해 주세요!", e.getName()))
                .build()
        );
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<GlobalRes<String>> badRequestHandle(BadRequestException e) {
        return ResponseEntity.status(400).body(
            GlobalRes.<String>builder()
                .code("E22")
                .message("잘못된 요청입니다.")
                .data(e.getMessage())
                .build()
        );
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<GlobalRes<String>> invalidOrderStatusHandle(
        InvalidOrderStatusException e
    ) {
        return ResponseEntity.status(400).body(
            GlobalRes.<String>builder()
                .code("E23")
                .message("주문 상태 변경 오류")
                .data(e.getMessage())
                .build()
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<GlobalRes<String>> notFoundHandle(NotFoundException e) {
        return ResponseEntity.status(404).body(
            GlobalRes.<String>builder()
                .code("E30")
                .message("데이터를 찾을 수 없습니다.")
                .data(e.getMessage())
                .build()
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<GlobalRes<String>> forbiddenHandle(ForbiddenException e) {
        return ResponseEntity.status(403).body(
            GlobalRes.<String>builder()
                .code("E03")
                .message("UNAUTHORIZED_ERROR")
                .data(e.getMessage())
                .build()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalRes<String>> httpMessageNotReadableHandle(
        HttpMessageNotReadableException e
    ) {
        return ResponseEntity.status(400).body(
            GlobalRes.<String>builder()
                .code("E20")
                .message("요청 본문 형식 오류")
                .data("JSON 형식과 각 필드의 값을 확인해 주세요.")
                .build()
        );
    }

        @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GlobalRes<String>> noResourceFoundHandle(
        NoResourceFoundException e
    ) {
        return ResponseEntity.status(404).body(
            GlobalRes.<String>builder()
                .code("E30")
                .message("요청한 API를 찾을 수 없습니다.")
                .data(e.getResourcePath())
                .build()
        );
    }
    
    
    
    @ExceptionHandler(FileManagedException.class)
    public ResponseEntity<GlobalRes<String>> fileManagedExceptionHandle(FileManagedException e){
        log.error(
                "파일업로드 에러: {}\n{}"
                , e.getMessage()
                , Arrays.toString(e.getStackTrace()));
        return ResponseEntity.status(500).body(
            GlobalRes.<String>builder()
                .code("E40")
                .message("파일 업로드 실패")
                .data(e.getMessage())
                .build()
        );
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<GlobalRes<String>> sqlHandle(SQLException e){
        log.error("DB에러: ", e);
        return ResponseEntity.status(500).body(
            GlobalRes.<String>builder()
                .code("E80")
                .message("DB 에러")
                .data("현재 서비스 이용이 불가합니다. 잠시후 다시 시도해 주세요.")
                .build()
        );
    }
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<GlobalRes<String>> DatabaseInsertHandle(DataAccessException e){
        log.error("DB 인서트에러: ", e);
        return ResponseEntity.status(500).body(
            GlobalRes.<String>builder()
                .code("E90")
                .message("DB 인서트 에러")
                .data("현재 서비스 이용이 불가합니다. 잠시후 다시 시도해 주세요.")
                .build()
        );
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalRes<String>> othersHandle(Exception e){
        log.error("시스템에러: ", e);
        return ResponseEntity.status(500).body(
            GlobalRes.<String>builder()
                .code("E99")
                .message("시스템 에러")
                .data("현재 서비스 이용이 불가합니다. 잠시후 다시 시도해 주세요.")
                .build()
        );
    }
}
