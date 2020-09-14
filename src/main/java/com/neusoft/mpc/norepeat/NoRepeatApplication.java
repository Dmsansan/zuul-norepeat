package com.neusoft.mpc.norepeat;

import com.neusoft.mpc.norepeat.entity.FilterTokenType;
import com.neusoft.mpc.norepeat.filter.NoRepeatRequestAfterFilter;
import com.neusoft.mpc.norepeat.filter.NoRepeatRequestFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author siss
 * @date 2020/9/14  9:04
 */
@SpringBootApplication
@EnableZuulProxy
public class NoRepeatApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoRepeatApplication.class, args);
    }

    @Bean
    public NoRepeatRequestFilter noRepeatRequestFilter(){
        String type = FilterTokenType.HEADER.getTokenType();
        List<String> noFilterUrls = new ArrayList<>();

        return new NoRepeatRequestFilter(type, noFilterUrls);
    }

    @Bean
    public NoRepeatRequestAfterFilter noRepeatRequestAfterFilter(){
        return new NoRepeatRequestAfterFilter();
    }
}
