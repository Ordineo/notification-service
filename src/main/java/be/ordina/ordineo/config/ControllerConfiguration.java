package be.ordina.ordineo.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletException;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ControllerConfiguration {


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value= HttpStatus.BAD_REQUEST,reason = "Invalid data sent to server")
    public void constraintsNotValid(){}

    @ExceptionHandler(org.springframework.transaction.TransactionSystemException.class)
    @ResponseStatus(value=HttpStatus.BAD_REQUEST,reason = "Invalid data sent to server")
    public void updateConstraintsNotValid(){}

}