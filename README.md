#### zuul网关服务请求防重复提交使用说明
**1.实现原理**

  * 通过自定义ZuulFilter过滤器实现request请求前对防重复提交的标志缓存保存，
设置重复提交标志过期时间，过期后自动删除防重复提交标志。

  * 防重复提交标志包括请求的param参数以及body参数+用户的账户信息Authorization+
请求路由url。通过MD5加密的形式保存。配置不需要防重复提交的url,过滤器会跳过登录
配置"oauth/token"的请求，用户信息需添加到request.Header参数"Authorization"。

  * 默认重复提交标志5S后失效。

  * 其中防重复提交标志信息需要借助redis数据库来保存。

**2.引入mpc-norepeat-1.0.0-SNAPSHOT.jar包依赖**

在pom.xml文件中加入jar包依赖代码：

    <dependency>
         <groupId>com.neusoft.mpc</groupId>
         <artifactId>mpc-norepeat</artifactId>
         <version>1.0.0-SNAPSHOT</version>
    </dependency>

**3.通过配置注入防重复提交过滤器实例**
    
   zuul网关服务注入自定义过滤器的方式有两种：直接在启动类里添加注入的实例代码、添加网关配置类注入
自定义过滤器实例(ps:如果springboot项目启动类在com.neusoft.mpc同一个包名下，不需要通过注入的
方式实例化Bean)

  * 直接在zuul网关服务启动类代码注入pre、post两个自定义过滤器NoRepeatRequestFilter.class、
NoRepeatRequestAfterFilter.class。

    
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

  * 添加自定义网关过滤器配置类CustomerZuulFilterConfig.class。

    
    @Configuration
    public class CustomerZuulFilterConfig {
 
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
    

**4.需要配置项目接口访问通过zuul网关服务**

  * 启动类上添加@EnableZuulProxy注解

  * 配置文件添加zuul路由网关：(norepeat名称可以自定义)

    
    zuul.routes.norepeat.path=/api/**
    zuul.routes.norepeat.url=http://localhost:8080/    