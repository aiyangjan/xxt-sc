package com.xxt.sc.common.web;

import com.xxt.sc.common.trace.TraceId;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 为每个请求注入 traceId，并回写到响应头，便于前后端联调与问题定位。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String incoming = request.getHeader(TraceId.HEADER);
        String traceId = (incoming == null || incoming.isEmpty())
                ? TraceId.generate()
                : incoming;
        TraceId.set(traceId);
        try {
            response.setHeader(TraceId.HEADER, traceId);
            chain.doFilter(request, response);
        } finally {
            TraceId.clear();
        }
    }
}
