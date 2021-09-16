package com.enation.app.base.core.util.sqpayutil;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@code HttpClient}为HTTP请求客户端，负责将报文发送至指定URL。
 *
 * <p>
 * {@code HttpClient}提供了丰富的http/https请求功能，根据实际应用场景可以向服务器发送多种请求实体。
 * </p>
 *
 * <p>
 * 通过注解@Autowired可以注入实例对象。
 * </p>
 *
 * <p>
 * 使用时根据实际情况在配置文件配置:
 * <dl>
 * <dd>连接池最大连接数：httpClient.maxConnTotal，未配置默认50</dd>
 * <dd>每个路由最大连接数：httpClient.maxConnPerRoute，未配置默认为最大连接数的一半</dd>
 * <dd>超时时间(单位秒)：httpClient.timeout，未配置默认15秒</dd>
 * <dd>重发次数：httpClient.retryCount，未配置默认0次</dd>
 * </dl>
 * </p>
 *
 * <blockquote>参数说明如下：每个路由最大连接数：httpClient.maxConnPerRoute 如配置为2时，意味着请求到http://www.baidu.com的最大连接数只能为2，即使连接池还有1000个可用连接！
 * </blockquote>
 *
 * <p>
 * <strong>设计思路：</strong> HTTP请求客户端组件主要封装{@code Apache HttpClient}，为接入着提供定制化的 {@code HttpClient}实例并提供向服务器发送各种格式请求的方法。
 * ​HTTP客户端组件需要满足发送{@code http}和 {@code https}请求的能力，在实现过程中需要向注册器注册{@code http}工厂和{@code https}工厂。
 * ​鉴于不同服务器对请求参数编码不同，每个方法需要提供接收编码格式的参数，如果未要求编码格式，默认采用{@code UTF-8}。
 * </p>
 */
public class HttpClient {

    private static final String CHARSET_UTF_8 = "UTF-8";

    private static final String CONTENT_FORM_TYPE = "application/x-www-form-urlencoded";

    private static final String CONTENT_TYPE = "application/json;charset=utf-8";

    /**
     * 连接池最大连接数
     **/
    @Value("${httpClient.maxConnTotal:200}")
    private int maxConnTotal;

    /**
     * 每个路由最大连接数
     **/
    @Value("${httpClient.maxConnPerRoute:0}")
    private int maxConnPerRoute;

    /**
     * 超时时间，秒
     **/
//    @Value("${httpClient.timeout:15}")
    private int timeout = 13;

    /**
     * 重发次数
     **/
//    @Value("${httpClient.retryCount:0}")
    private int retryCount = 2;

    /**
     * 连接客户端
     **/
    private CloseableHttpClient closeableHttpClient;

    /**
     * 重试的连接客户端
     **/
    private CloseableHttpClient closeableRetryHttpClient;

    /**
     * 构造方法
     */
    public HttpClient() {
    }

    ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {
        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            Args.notNull(response, "HTTP response");
            final HeaderElementIterator it = new BasicHeaderElementIterator(
                    response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                final HeaderElement he = it.nextElement();
                final String param = he.getName();
                final String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch (final NumberFormatException ignore) {
                    }
                }
            }
            return 1;
        }
    };

    @PostConstruct
    public void init() throws Exception {
        // 超时时间,单位秒
        int httpReqTimeOut = timeout * 1000;
        /**
         * maxConnPerRoute为每个路由的最大连接数，如:maxConnPerRoute=2时， 请求到www.baidu.com的最大连接数只能为2，即使连接池还有1000个可用连接！
         */
        if (maxConnPerRoute == 0) {
            maxConnPerRoute = maxConnTotal / 2;
        }
        SSLContext sslContext = SSLContext.getInstance("TLS");
        // 初始化SSL上下文
        sslContext.init(null, new TrustManager[]{tm}, null);
        // SSL套接字连接工厂,NoopHostnameVerifier为信任所有服务器
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        // 注册http套接字工厂和https套接字工厂
        Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslsf).build();
        // 连接池管理器
        PoolingHttpClientConnectionManager pcm = new PoolingHttpClientConnectionManager(r);
        // 连接池最大连接数
        pcm.setMaxTotal(maxConnTotal);
        // 每个路由最大连接数
        pcm.setDefaultMaxPerRoute(maxConnPerRoute);
        /**
         * 请求参数配置 connectionRequestTimeout: 从连接池中获取连接的超时时间，超过该时间未拿到可用连接，
         * 会抛出org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
         * connectTimeout: 连接上服务器(握手成功)的时间，超出该时间抛出connect timeout socketTimeout: 服务器返回数据(response)的时间，超过该时间抛出read
         * timeout
         */
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(httpReqTimeOut)
                .setConnectTimeout(httpReqTimeOut).setSocketTimeout(httpReqTimeOut).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        /**
         * 构造closeableHttpClient对象
         */
        closeableHttpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(pcm).setKeepAliveStrategy(keepAliveStrategy).build();

        /**
         *造有重试机制的closeableHttpClient对象
         */
        closeableRetryHttpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(pcm)
                .setRetryHandler(retryHandler).setKeepAliveStrategy(keepAliveStrategy).build();
    }


    /**
     * post 数据，数据以key、value存放在Map中
     *
     * @param url      http请求地址
     * @param paramMap 请求参数
     * @return 请求响应消息字符串
     * @throws IOException
     * @throws ParseException
     * @throws ClientProtocolException
     * @throws Exception
     */
    public String postMap(String url, Map<String, String> paramMap) throws ClientProtocolException, ParseException,
            IOException {
        return postMap(url, paramMap, CHARSET_UTF_8, false);
    }

    /**
     * 有重试机制的post请求
     *
     * @param url
     * @param paramMap
     * @return
     * @throws ClientProtocolException
     * @throws ParseException
     * @throws IOException
     */
    public String retryPostMap(String url, Map<String, String> paramMap) throws ClientProtocolException, ParseException,
            IOException {
        return postMap(url, paramMap, CHARSET_UTF_8, true);
    }

    /**
     * post 数据，数据以key、value存放在Map中
     *
     * @param url         http请求地址
     * @param paramMap    请求参数
     * @param charsetName 编码格式
     * @return 请求响应消息字符串
     * @throws IOException
     * @throws ParseException
     * @throws ClientProtocolException
     * @throws Exception
     */
    public String postMap(String url, Map<String, String> paramMap, String charsetName, boolean retryFlag) throws ClientProtocolException,
            ParseException, IOException {
        // post请求
        HttpPost post = new HttpPost(url);
        // 设置参数
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        // 对参数进行编码
        post.setEntity(new UrlEncodedFormEntity(formParams, Charset.forName(charsetName)));
        return httpRequest(post, charsetName, retryFlag);
    }

    /**
     * http 请求
     *
     * @param request
     * @return 请求响应消息字符串
     * @throws
     * @throws IOException
     * @throws ParseException
     */
    private String httpRequest(HttpUriRequest request, String charsetName, boolean retryFlag) throws
            ParseException, IOException {
        String responseText = null;
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        try {
            if (retryFlag) {
                response = closeableRetryHttpClient.execute(request);
            } else {
                response = closeableHttpClient.execute(request);
            }
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                entity = response.getEntity();
                if (entity != null) {
                    // 将返回实体转换为字符串
                    responseText = EntityUtils.toString(entity, Charset.forName(charsetName));
                }
            } else {
                // 放弃连接
                request.abort();
            }
        } catch (ClientProtocolException e) {
            throw e;
        } catch (ParseException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            if (entity != null) {
                try {
                    // 释放资源可用触发连接放回连接池
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                    throw e;
                }
            }
        }
        return responseText;
    }

    /**
     * 根据约定往商户发送报文，报文属性为xmldata
     *
     * @param url     发送报文地址
     * @param xmldata 发送报文的xml String
     * @return 请求响应消息字符串
     * @throws IOException
     * @deprecated 使用{@link #postMap(String, Map)}
     */
    @Deprecated
    public String sendMerchantMsg(String url, String xmldata) throws IOException {
        return sendMerchantMsg(url, xmldata, CHARSET_UTF_8);
    }

    /**
     * 根据约定往商户发送报文，报文属性为xmldata
     *
     * @param url         发送报文地址
     * @param xmldata     发送报文的xml String
     * @param charsetName 编码格式
     * @param charsetName 编码格式
     * @return 请求响应消息字符串
     * @throws IOException
     * @deprecated 使用{@link #postMap(String, Map, String)}
     */
    @Deprecated
    public String sendMerchantMsg(String url, String xmldata, String charsetName) throws IOException {
        // post请求
        HttpPost post = new HttpPost(url);

        // 设置参数
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("xmldata", xmldata));

        // 对参数进行编码
        post.setEntity(new UrlEncodedFormEntity(params, Charset.forName(charsetName)));

        // 发起请求
        String responseText = httpRequest(post, charsetName, false);
        return responseText;
    }

    /**
     * 根据与省端约定发送报文，报文格式为xmlhead和xmlbody
     *
     * @param url     报文地址
     * @param xmlHead 报文头
     * @param xmlBody 报文体
     * @return 请求响应消息字符串
     * @throws IOException
     * @throws ParseException
     * @throws ClientProtocolException
     */
    public String sendCrmMsg(String url, String xmlHead, String xmlBody) throws ClientProtocolException,
            ParseException, IOException {
        return sendCrmMsg(url, xmlHead, xmlBody, CHARSET_UTF_8);
    }

    /**
     * 根据与省端约定发送报文，报文格式为xmlhead和xmlbody
     *
     * @param url         报文地址
     * @param xmlHead     报文头
     * @param xmlBody     报文体
     * @param charsetName 编码格式
     * @return 请求响应消息字符串
     * @throws IOException
     * @throws ParseException
     * @throws ClientProtocolException
     */
    public String sendCrmMsg(String url, String xmlHead, String xmlBody, String charsetName)
            throws ClientProtocolException, ParseException, IOException {
        StringBody head = null;
        StringBody body = null;

        head = new StringBody(xmlHead, ContentType.create("text/plain", Charset.forName(charsetName)));
        body = new StringBody(xmlBody, ContentType.create("text/plain", Charset.forName(charsetName)));

        HttpEntity entity = MultipartEntityBuilder.create().addPart("xmlhead", head).addPart("xmlbody", body)
                .setCharset(Charset.forName(charsetName)).build();

        HttpPost httpost = new HttpPost(url);
        httpost.setEntity(entity);

        return httpRequest(httpost, charsetName, false);
    }

    /**
     * 发送字符串消息到指定url
     *
     * @param url 消息接收的url
     * @param msg 请求消息
     * @return 请求响应消息字符串
     * @throws IOException
     * @throws ParseException
     * @throws ClientProtocolException
     */
    public String sendStringMsg(String url, String msg) throws ClientProtocolException, ParseException, IOException {
        return sendStringMsg(url, msg, CHARSET_UTF_8, null);
    }

    /**
     * 发送字符串消息到指定url
     *
     * @param url         消息接收的url
     * @param msg         请求消息
     * @param charsetName 编码格式
     * @param contentType ContentType格式
     * @return 请求响应消息字符串
     * @throws ClientProtocolException
     * @throws ParseException
     * @throws IOException
     */
    public String sendStringMsg(String url, String msg, String charsetName, String contentType)
            throws ClientProtocolException, ParseException, IOException {
        // 字符串Entity
        StringEntity entity = null;
        entity = new StringEntity(msg, Charset.forName(charsetName));
        //
        if (!StringUtils.isBlank(contentType)) {
            entity.setContentType(contentType);
        }

        // http post请求
        HttpPost httpost = new HttpPost(url);
        httpost.setEntity(entity);
        return httpRequest(httpost, charsetName, false);
    }

    /**
     * @param url 消息接收的url
     * @param msg 请求消息 消息为key=value&key2=val2形式
     * @return
     * @throws ClientProtocolException
     * @throws ParseException
     * @throws IOException
     */
    public String sendFormMsg(String url, String msg) throws ClientProtocolException, ParseException, IOException {
        return sendStringMsg(url, msg, CHARSET_UTF_8, CONTENT_FORM_TYPE);
    }

    public String sendStrMsg(String url, String msg) throws ClientProtocolException, ParseException, IOException {
        return sendStringMsg(url, msg, CHARSET_UTF_8, CONTENT_TYPE);
    }

    public String sendRemouldMsg(String url, String msg) throws ClientProtocolException, ParseException, IOException {
        return sendStringRemould(url, msg, CHARSET_UTF_8, CONTENT_TYPE);
    }

    public String sendStringRemould(String url, String msg, String charsetName, String contentType)
            throws ClientProtocolException, ParseException, IOException {
        // 字符串Entity
        StringEntity entity = new StringEntity(msg, Charset.forName(charsetName));
        entity.setContentType("application/x-www-form-urlencoded;charset=UTF-8");
        //
        if (!StringUtils.isBlank(contentType)) {
            entity.setContentType(contentType);
        }

        // http post请求
        HttpPost httpost = new HttpPost(url);
        httpost.setEntity(entity);
        httpost.setHeader("accept", "*/*");
        httpost.setHeader("connection", "Keep-Alive");
        httpost.setHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        httpost.setHeader("Accept-Charset", "UTF-8");
        httpost.setHeader("contentType", "UTF-8");
        httpost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        return httpRequest(httpost, charsetName, false);
    }

    /**
     * 重发处理器
     */
    private HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            // 打印警告信息
            if (executionCount >= retryCount) {
                // Do not retry if over max retry count
                return false;
            }
            if (exception instanceof ConnectTimeoutException) {
                // Connection refused
                return true;
            }
            if (exception instanceof UnknownHostException) {
                // Unknown host
                return true;
            }
            if (exception instanceof InterruptedIOException) {
                // Timeout
                return true;
            }
            if (exception instanceof SSLException) {
                // SSL handshake exception
                return false;
            }
            if (exception instanceof NoHttpResponseException) {
                // NoHttpResponse exception,切记只能在可多次重试的情况下使用
                return true;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
                // 必须是幂等性的才能进行重发
                return true;
            }
            return false;
        }
    };

    /**
     * 信任管理器
     */
    private static X509TrustManager tm = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

}
