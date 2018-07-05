package com.liu.springbootliu.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.springbootliu.jwt.Audience;
import com.liu.springbootliu.jwt.JwtHelper;
import com.liu.springbootliu.utils.ResultMsg;
import com.liu.springbootliu.utils.ResultStatusCode;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * jwt的验证类
 */
public class HTTPBearerAuthorizeAttribute implements Filter {
    @Autowired
    private Audience audienceEntity;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,filterConfig.getServletContext());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ResultMsg resultMsg;
        HttpServletRequest httpRequest= (HttpServletRequest) servletRequest;
        String auth = httpRequest.getHeader("Authorization");
        if ((auth != null)&&(auth.length()>7)){
            String HeadStr=auth.substring(0,6).toLowerCase();
            //之前在生成token entity 通过json http body返回给客户端的时候带了一个Token_type的属性值就是"bearer"
            if (HeadStr.compareTo("bearer")==0){
                auth = auth.substring(7,auth.length());//拿到jwt字串
                //对jwt字串进行解析得到Claims载体信息,这里并没有具体看里面到底有什么，只看能不能正常对验证签名拿到返回对象。
                try{
                    if (JwtHelper.parseJWT(auth,audienceEntity.getBase64Secret())!=null){
                        filterChain.doFilter(servletRequest,servletResponse);
                        return;
                    }
                }catch (ExpiredJwtException ex){
                    //接住JWT的超时异常,返回Token超时信息
                    HttpServletResponse httpResponse= (HttpServletResponse) servletResponse;
                    httpResponse.setCharacterEncoding("UTF-8");
                    httpResponse.setContentType("application/json;charset=utf-8");
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.setHeader("Authorization","expires");
                    ObjectMapper mapper=new ObjectMapper();
                    resultMsg = new ResultMsg(ResultStatusCode.EXPIRES_TOKEN.getErrcode(),ResultStatusCode.EXPIRES_TOKEN.getErrmsg(),null);
                    httpResponse.getWriter().write(mapper.writeValueAsString(resultMsg));
                    return;
                }catch (JwtHelper.ErrorCodeException  ex){//捕获userCode不一至的异常
                    //接住userCode不一至的异常,返回信息
                    HttpServletResponse httpResponse= (HttpServletResponse) servletResponse;
                    httpResponse.setCharacterEncoding("UTF-8");
                    httpResponse.setContentType("application/json;charset=utf-8");
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.setHeader("Authorization","userCodeError");
                    ObjectMapper mapper=new ObjectMapper();
                    resultMsg = new ResultMsg(ResultStatusCode.USERCODE_ERR.getErrcode(),ResultStatusCode.USERCODE_ERR.getErrmsg(),null);
                    httpResponse.getWriter().write(mapper.writeValueAsString(resultMsg));
                    return;
                }

            }
        }
        //返回API验证错误消息
        HttpServletResponse httpResponse= (HttpServletResponse) servletResponse;
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setContentType("application/json;charset=utf-8");

        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ObjectMapper mapper=new ObjectMapper();
        resultMsg = new ResultMsg(ResultStatusCode.INVALID_TOKEN.getErrcode(),ResultStatusCode.INVALID_TOKEN.getErrmsg(),null);
        httpResponse.getWriter().write(mapper.writeValueAsString(resultMsg));
        return;
    }

    @Override
    public void destroy() {

    }
}
