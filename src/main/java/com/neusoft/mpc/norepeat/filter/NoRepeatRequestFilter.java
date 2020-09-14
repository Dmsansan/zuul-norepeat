package com.neusoft.mpc.norepeat.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.neusoft.mpc.norepeat.entity.ApiResult;
import com.neusoft.mpc.norepeat.entity.ResultCode;
import com.neusoft.mpc.norepeat.entity.FilterTokenType;
import com.neusoft.mpc.norepeat.redis.Md5Utils;
import com.neusoft.mpc.norepeat.redis.RedisUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author siss
 * @date 2020-09-14
 */
public class NoRepeatRequestFilter extends ZuulFilter {

    /**
     *  log日志
      */
    private static final Logger LOGGER = LoggerFactory.getLogger(NoRepeatRequestFilter.class);

    /**
     *  配置从request请求中获取用户唯一信息 默认cookie, header
      */
    private String tokenType;

    /**
     *  配置不通过防重复提交的url地址
      */
    private List<String> notFilterUrls;

    public NoRepeatRequestFilter(String tokenType, List<String> notFilterUrls) {
        this.tokenType = tokenType;
        this.notFilterUrls = notFilterUrls;
    }

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return -5;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        // zuul网关代理security权限判断服务
        final RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        // 未配置用户信息获取方式
        if(!FilterTokenType.COOKIE.getTokenType().equals(tokenType) && !FilterTokenType.HEADER.getTokenType().equals(tokenType)){
            ApiResult apiResult = ApiResult.failure(ResultCode.ERROR, "未配置用户信息获取方式！");

            ctx.getResponse().setHeader("Content-Type", "text/html;charset=UTF-8");
            ctx.getResponse().setCharacterEncoding("utf-8");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(200);
            ctx.setResponseBody(JSON.toJSONString(apiResult));
            ctx.set("isSuccess", false);
            LOGGER.info("未配置用户信息获取方式：cookie->JSSESIONID header->TOKENID");
            return null;
        }

        // uri是否防重复提交过滤
        if (shouldNotFilterUrl(ctx.getRequest().getRequestURI(), notFilterUrls)){
            String url = request.getRemoteAddr() + request.getServletPath();
            try {
                String params = null;
                // 表单中获取参数
                Map<String, String[]> parameterMap = request.getParameterMap();
                if (!parameterMap.isEmpty()) {
                    params = JSON.toJSONString(parameterMap);
                }
                // body中获取参数
                if (!ctx.isChunkedRequestBody()) {
                    ServletInputStream inp = ctx.getRequest().getInputStream();
                    byte[] bytes = new byte[0];
                    bytes = new byte[inp.available()];
                    inp.read(bytes);
                    String postParam = new String(bytes);

                    if (inp != null) {
                        if (StringUtils.isEmpty(params)) {
                            params = postParam;
                        } else {
                            params += postParam;
                        }
                    }
                }

                String userToken = "";
                // 获取request请求中cookie保存的用户信息
                if("cookie".equals(tokenType)){
                    Cookie[] cookies = request.getCookies();
                    for (Cookie cookie : cookies) {
                        switch(cookie.getName()){
                            case "JSEESIONID":
                                userToken = cookie.getValue();
                                break;
                            default:
                                break;
                        }
                    }
                }

                // 从header中获取用户信息
                if("header".equals(tokenType)){
                    userToken = request.getHeader("TOKENID");
                }

                // 为空添加入Redis，不为空   返回错误信息重复提交
                String md5Key = Md5Utils.md5Encrypt32Lower(userToken + url + params);
                if (StringUtils.isEmpty((String) redisUtil.get(md5Key))) {
                    // 默认5s后可以再次请求同一接口
                    boolean set = redisUtil.set(md5Key, md5Key, 5L);
                    if (!set) {
                        LOGGER.debug("redis重复提交标志数据保存失败");
                    }
                } else {
                    ApiResult apiResult = ApiResult.failure(ResultCode.ERROR, "请求太频繁，请勿重复提交！");

                    ctx.getResponse().setHeader("Content-Type", "text/html;charset=UTF-8");
                    ctx.getResponse().setCharacterEncoding("utf-8");
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(200);
                    ctx.setResponseBody(JSON.toJSONString(apiResult));
                    ctx.set("isSuccess", false);
                    LOGGER.info("请求过于频繁");
                    LOGGER.info("客户地址:{}  请求地址：{} 请求方式 {}", request.getRemoteHost(), request.getRequestURL().toString(), request.getMethod());
                    LOGGER.info("params:{} ", params);
                    return null;
                }
            } catch (Exception e) {
                LOGGER.warn("{}重复请求校验出错：{}", request.getRequestURL(), e.getMessage());
            }

        }
        return null;
    }

    /**
     * uri是否防重复提交判断
     * @param uri
     * @param notFilterUrls
     * @return
     */
    public boolean shouldNotFilterUrl(String uri, List<String> notFilterUrls){
        boolean tag = true;
        if(!notFilterUrls.isEmpty()){
            for (String url : notFilterUrls){
                if(uri.contains(url)){
                    tag = false;
                    break;
                }
            }
        }
        return tag;
    }
}
