package cn.st.sso.client.filter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import cn.st.sso.client.entity.User;
import cn.st.util.CookieUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * desciption:SSO filter
 * </p>
 * 
 * @author coolearth
 * @date 2015年8月19日
 */
public class SSOFilter implements Filter {
    private static final String TOKEN = "token";

    private String loginUrl;

    private String validateUrl;

    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub
        loginUrl = filterConfig.getInitParameter("loginUrl");
        validateUrl = filterConfig.getInitParameter("validateUrl");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // TODO Auto-generated method stub
        // 1、获取token

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rep = (HttpServletResponse) response;
        String token = CookieUtil.readCookie(req, TOKEN);
        String redirectUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString();
        if (!StringUtils.isBlank(queryString)) {
            redirectUrl += "?" + queryString;
        }
        // 2、判断token是否存在
        // 3、若存在，则跳转到登录页
        if (StringUtils.isBlank(token)) {
            rep.sendRedirect(loginUrl + "?redirectUrl=" + URLEncoder.encode(redirectUrl, "UTF-8"));
        } else {
            // 4、若存在，验证token有效性
            URL url = new URL(validateUrl + "?" + TOKEN + "=" + token);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            InputStream is = urlConnection.getInputStream();
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String ret = new String(buffer);
            // 5、若token无效，则跳转到登录页
            if (ret.length() == 0) {
                rep.sendRedirect(loginUrl + "?redirectUrl="
                        + URLEncoder.encode(redirectUrl, "UTF-8"));
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                User user = objectMapper.readValue(ret, User.class);
                request.setAttribute("user", user);
                // 6、若token有效则进入下一流程
                chain.doFilter(request, response);
            }

        }


    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

}
