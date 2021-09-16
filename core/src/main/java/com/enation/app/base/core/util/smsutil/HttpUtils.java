package com.enation.app.base.core.util.smsutil;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HttpUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    
    private static final int DEFAULT_CONNECT_TIME_OUT = 10000;
    private static final Builder DEFAULT_BUILDER = RequestConfig.custom();
    private static final RequestConfig DEFAULT_REQUEST_CONFIG;

    static {
        DEFAULT_BUILDER.setConnectTimeout(DEFAULT_CONNECT_TIME_OUT);
        DEFAULT_REQUEST_CONFIG = DEFAULT_BUILDER.build();
    }
    public static String sendPost(String path, String param) {
        logger.info("-----------url：" + path);
        logger.info("-----------param：" + param);

        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(path);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setConnectTimeout(60 * 1000);
            //读取超时设置
            conn.setReadTimeout(60 * 1000);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.info("发送POST请求出现异常！" + e);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        logger.info("-----------应答结果：{}", result);
        return result;

    }
    /**
     * 发送POST请求
     *
     * @param url
     *            目的地址
     * @param parameters
     *            请求参数，Map类型。
     * @return 远程响应结果
     */
    public static String sendPost(String url, Map<String, String> parameters) {
        String result = "";// 返回的结果
        BufferedReader in = null;// 读取响应输入流
        PrintWriter out = null;
        StringBuffer sb = new StringBuffer();// 处理请求参数
        String params = "";// 编码之后的参数
        try {
            // 编码请求参数
            if (parameters.size() == 1) {
                for (String name : parameters.keySet()) {
                    if (StringUtils.isNotBlank(parameters.get(name))) {
                        sb.append(name).append("=").append(URLEncoder.encode(parameters.get(name), "utf-8"));
                    }
                }
                params = sb.toString();
            } else {
                for (String name : parameters.keySet()) {
                    if (StringUtils.isNotBlank(parameters.get(name))) {
                        sb.append(name).append("=").append(URLEncoder.encode(parameters.get(name), "utf-8"))
                                .append("&");
                    }

                }
                String temp_params = sb.toString();
                params = temp_params.substring(0, temp_params.length() - 1);
            }
            // 创建URL对象
            URL connURL = new URL(url);
            // 打开URL连接
            HttpURLConnection httpConn = (HttpURLConnection) connURL.openConnection();
            // 设置通用属性
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
            httpConn.setRequestProperty("Content-type", "application/json;charset=UTF-8");
            // 设置POST方式
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setUseCaches(false);

            httpConn.setConnectTimeout(30000);
            httpConn.setReadTimeout(30000);
            // 获取HttpURLConnection对象对应的输出流
            out = new PrintWriter(httpConn.getOutputStream());
            // 发送请求参数
            out.write(params);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应，设置编码方式
            in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "utf-8"));
            String line;
            // 读取返回的内容
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        return result;
    }
    /**
     * 发送POST请求
     *
     * @param url
     *            目的地址
     *            请求参数，Map类型。
     * @return 远程响应结果
     */
    public static String sendStrPost(String url, String params) {

        String result = "";// 返回的结果
        BufferedReader in = null;// 读取响应输入流
        PrintWriter out = null;
        // String params = "";// 编码之后的参数
        try {
            // 创建URL对象
            URL connURL = new URL(url);
            // 打开URL连接
            HttpURLConnection httpConn = (HttpURLConnection) connURL.openConnection();
            // 设置通用属性
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
            httpConn.setRequestProperty("Content-type", "application/json;charset=UTF-8");
            // 设置POST方式
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            // 获取HttpURLConnection对象对应的输出流
            out = new PrintWriter(httpConn.getOutputStream());
            // 发送请求参数
            out.write(params);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应，设置编码方式
            in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
            String line;
            // 读取返回的内容
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        return result;
    }

    public static String sendPost(String path, String param, Logger logger) {
        logger.info("-----------url：" + path);
        logger.info("-----------param：" + param);

        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(path);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setConnectTimeout(60 * 1000);
            //读取超时设置
            conn.setReadTimeout(60 * 1000);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.info("发送POST请求出现异常！" + e);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        logger.info("-----------应答结果：{}", result);
        return result;
    }

    public static String sendGet(String path, String param, Logger logger) {
        logger.info("-----------url：" + path);
        logger.info("-----------param：" + param);
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = path + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(60 * 1000);
            //读取超时设置
            connection.setReadTimeout(60 * 1000);
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.info("发送GET请求出现异常！" + e);
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage());
            }
        }
        logger.info("-----------应答结果：{}", result);
        return result;
    }

    public String sendGet(String path, String param) {
        logger.info("-----------url：" + path);
        logger.info("-----------param：" + param);
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = path + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(60 * 1000);
            //读取超时设置
            connection.setReadTimeout(60 * 1000);
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.info("发送GET请求出现异常！" + e);
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage());
            }
        }
        logger.info("-----------应答结果：{}", result);
        return result;
    }
	

    public static String doPost(String url, Map<String, String> map) throws Exception{
        return doPost(url, mapForPost(map));
    }

    private static  String mapForPost(Map<String, String> map) {
        String result = "";
        boolean firstParams = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!firstParams) {
                result += "&";
            } else {
                firstParams = false;
            }
            try {
                result += entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage());
                //throw new BizException("组请求报文异常");
            }
        }
        return result;
    }

    public static String doPost(String url, String content) throws Exception{
        /**
         * 发送请求
         */
        logger.info("postUrl :{} postContent:{}", url, content);
        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Charset", "utf-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(content.length()));
            //httpURLConnection.setRequestProperty("Content-type", "application/json;charset=UTF-8");
            /**
             * 链接超时
             * 读取超时
             */
            httpURLConnection.setConnectTimeout(60 * 1000);
            httpURLConnection.setReadTimeout(60 * 1000);

            outputStream = httpURLConnection.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(content);
            outputStreamWriter.flush();

            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            return resultBuffer.toString();
        } catch(SocketTimeoutException se){
        	logger.info(se.getMessage());
            //throw new BizException("读取超时");
        }catch (Exception ex) {
            logger.info(ex.getMessage());
            //throw new BizException("请求报文发送异常");
        } finally {
            try {
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ex) {
                logger.info(ex.getMessage());
                //throw new BizException("请求报文发送异常");
            }
        }
        return null;
    }

    /**
	 * http post请求
	 * 
	 * @param url
	 *            请求地址
	 * @param map
	 *            参数map
	 * @return 返回处理结果，如果为空表示请求失败
	 */
	public static String httpByPost(String url, Map<String, String> map) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = null;
		try {
			logger.info("请求参数："+map.toString());
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			if (map != null && !map.isEmpty()) {
				Object[] keyArray = map.keySet().toArray();
				int size = keyArray.length;
				for (int i = 0; i < size; i++) {
					String key = keyArray[i] + "";
					String value = map.get(key);
					if(StringUtils.isBlank(value)){
						value="";
					}
					formparams.add(new BasicNameValuePair(key, value));
				}

			}
			httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			HttpResponse rsp = httpclient.execute(httpPost);
			if (HttpStatus.SC_OK != rsp.getStatusLine().getStatusCode()) {
				return null;
			}
			String str = EntityUtils.toString(rsp.getEntity(), "UTF-8");
			EntityUtils.consume(rsp.getEntity());
			return str;
		} catch (Exception e) {
			logger.error("[httpByPost method]http post request error for url:" + url,e);
		} finally {
			httpPost.abort();
			httpclient.getConnectionManager().shutdown();
		}
		return null;
	}

    public static String requestPostJSON(String url, Map<String, Object> param, int connectTimeOut) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-type", "application/json;charset=UTF-8");
        setRequestConfig(post, connectTimeOut);

        Gson gson = new Gson();

        HttpEntity stringEntity = new StringEntity(gson.toJson(param), "utf-8");
        post.setEntity(stringEntity);
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        String str = EntityUtils.toString(entity, "utf-8");
        client.getConnectionManager().shutdown();
        return str;
    }
    
    private static void setRequestConfig(HttpRequestBase requestBase, int connectTimeOut) {
        if (connectTimeOut == DEFAULT_CONNECT_TIME_OUT) {
            requestBase.setConfig(DEFAULT_REQUEST_CONFIG);
        } else {
            Builder custom = RequestConfig.custom();
            custom.setConnectTimeout(connectTimeOut);
            custom.setSocketTimeout(connectTimeOut);
            custom.setConnectionRequestTimeout(connectTimeOut);
            requestBase.setConfig(custom.build());
        }

    }
}
