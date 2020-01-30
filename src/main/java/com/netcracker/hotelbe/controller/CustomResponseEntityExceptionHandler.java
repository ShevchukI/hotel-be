package com.netcracker.hotelbe.controller;



import com.google.common.base.Throwables;
import com.netcracker.hotelbe.exception.CustomResponseEntityException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LogManager.getLogger("CustomResponseEntityException");
    private static String NOT_FOUND_ENTITY_WITH_ID = "No entity with id = %s found";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ObjectError> fieldErrors = ex.getBindingResult().getAllErrors();
        List<String> errorMessages = fieldErrors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
        return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EntityNotFoundException.class})
    protected ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        final String message = String.format(NOT_FOUND_ENTITY_WITH_ID, ex.getMessage());
        LOGGER.warn(message);

        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {InvalidDataAccessApiUsageException.class})
    protected ResponseEntity<Object> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex){
        StringBuilder message = new StringBuilder();
        message.append(ex.getMessage().split("\\[")[0]).append("[").append(ex.getMessage().split("\\[")[1]);
        LOGGER.warn(message);

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UsernameNotFoundException.class})
    protected ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {RuntimeException.class})
    protected ResponseEntity<Object> handlePSQLException(RuntimeException runtimeException) {
        Throwable rootCause = Throwables.getRootCause(runtimeException);
        String logMessage;
        String responseMessage = runtimeException.getMessage();
        if (rootCause instanceof PSQLException) {
            logMessage = rootCause.getMessage();
            responseMessage = logMessage.split("Detail: ")[0];
            if (logger.isErrorEnabled()) {
                logger.error(logMessage);
            }
        }
        return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {CustomResponseEntityException.class})
    protected ResponseEntity CustomResponseEntityException(CustomResponseEntityException e){
        final String message = e.getMessage();
        e.printStackTrace();
        LOGGER.warn(message);

        ResponseEntity responseEntity;
        if(e.getHttpStatus()!=null){
            responseEntity = new ResponseEntity(message, e.getHttpStatus());
        } else {
            responseEntity = new ResponseEntity(message, HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }
}
