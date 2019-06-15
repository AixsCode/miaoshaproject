package com.miaoshaproject.controller;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aixscode.github.io
 * @date 2019/1/3 20:39
 */
public class BaseController {

    public static final  String CONTENT_TYPE_FORMED="application/x-www-form-urlencoded";


    //定义exceptionhandler解决未被controller层吸收的exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception ex)
    {
        Map<String,Object> responseData=new HashMap<>();
        if (ex instanceof BussinessException)
        {
            BussinessException bussinessException=(BussinessException)ex;
            responseData.put("errCode",bussinessException.getErrorCode());
            responseData.put("errMsg",bussinessException.getErrorMsg());
        }
        else
        {
            responseData.put("errCode",EmBusinessError.UNKNOWN_ERROR);
            responseData.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrorMsg());

        }
        return  CommonReturnType.create(responseData,"fail");
    }
}
