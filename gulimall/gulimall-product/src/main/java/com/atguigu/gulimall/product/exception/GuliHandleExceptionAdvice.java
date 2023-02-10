package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Author: YZG
 * Date: 2022/12/29 18:50
 * Description: 
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller") // == @RestController + ControllerAdvice
public class GuliHandleExceptionAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleValidaException(MethodArgumentNotValidException e) {
        HashMap<String, String> map = new HashMap<>();
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        errors.forEach(fieldError -> {
            map.put(fieldError.getField(),fieldError.getDefaultMessage());
        });
        log.error("错误信息:{},错误类:{}",e.getMessage(),e.getClass());
      return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(),BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data",map);
    }
}
