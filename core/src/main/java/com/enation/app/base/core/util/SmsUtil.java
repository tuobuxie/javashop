package com.enation.app.base.core.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.base.core.service.ISmsManager;
import com.enation.app.base.core.util.smsutil.CertInfo;
import com.enation.app.base.core.util.smsutil.CipherUtil;
import com.enation.app.base.core.util.smsutil.HttpUtils;
import com.enation.app.base.core.util.smsutil.Md5Utils;
import com.enation.app.base.core.util.smsutil.SignUtil;
import com.enation.app.base.core.util.smsutil.SmsMsg;
import com.enation.app.base.core.util.smsutil.SmsResult;
import com.enation.eop.SystemSetting;
import com.enation.framework.context.spring.SpringContextHolder;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.CurrencyUtil;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.StringUtil;
import com.enation.framework.util.TestUtil;
import com.enation.framework.util.Validator;

/**
 * 短信相关通用方法
 * @author Sylow
 * @version v1.0,2016年7月6日
 * @since v6.1
 */
public class SmsUtil {

	// 短信验证码session前缀
	private static final String SMS_CODE_PREFIX = "es_sms_";
	
	// 短信验证间隔时间session前缀
	private static final String INTERVAL_TIME_PREFIX = "es_interval_";
	
	// 发送时间间隔
	private static final double SEND_INTERVAL = 60d;
	
	// 商标前缀
	private static final String BRAND_PREFIX = "微易商城";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SmsUtil.class);
	
	//短信超时时间前缀
	private static final String SENDTIME_PREFIX = "es_sendtime";
	//短信过期时间
	private static final Long SMS_CODE_TIMEOUT = 120l;
	
    static String SIGN_ALGORITHMS = "SHA1WithRSA";
	
	/**
	 * 发送短信验证码
	 * @param mobile 手机号
	 * @param key 类型key枚举 {@link SmsTypeKeyEnum}
	 * @param isCheckRegister 是否判断已经注册  check用的
	 * @exception RuntimeException 发送短信程序出错异常
	 * @return 发送结果 Map<String, Object> 其中key=state_code值{0=发送失败，1=发送成功,2=发送限制(操作过快等等限制)},key=msg 值为{提示消息}
	 */
	public static Map<String, Object> sendMobileSms(String mobile, String key, int isCheckRegister) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			
			//防止 空值
			if (key == null || "".equals(key)) {
				
				// 默认为登录
				key = SmsTypeKeyEnum.LOGIN.toString();	
			}
			//如果手机号格式不对
			if (!Validator.isMobile(mobile) ) {
				result.put("state_code", 2);
				result.put("msg", "手机号码格式错误");
				return result;
			}
			
			// 判断是否允许可以发送
			if (!validIsCanSendSms(key)) {
				result.put("state_code", 2);
				result.put("msg", "您的操作过快，请休息一下");
				return result;
			}
			
			ISmsManager smsManager = SpringContextHolder.getBean("smsManager");
			
			//随机生成的动态码
			String dynamicCode = "" + (int)((Math.random() * 9 + 1) * 100000);
			
			//如果是测试模式，验证码为1111
			if(SystemSetting.getTest_mode()==1){
				dynamicCode="1111";
			}
			
			//动态码短信内容
			String smsContent = "" +  dynamicCode;
			
			// 1如果是登录
			if (key.equals(SmsTypeKeyEnum.LOGIN.toString())) {
				smsContent = "您的登录验证码为：" +  dynamicCode + ", 如非本人操作，请忽略本短信 【" + BRAND_PREFIX + "】";
				
				// 校验手机是否注册过  
				if (!validMobileIsRegister(mobile)) {
					result.put("state_code", 2);
					result.put("msg", "当前手机号没有绑定相关帐号");
					return result;
				}
				
			// 2如果是注册
			} else if (key.equals(SmsTypeKeyEnum.REGISTER.toString())) {
				smsContent = "您的注册验证码为：" +  dynamicCode + ", 如非本人操作，请忽略本短信 【" + BRAND_PREFIX + "】";
				
				// 校验手机是否注册过  
				if (validMobileIsRegister(mobile)) {
					result.put("state_code", 2);
					result.put("msg", "当前输入手机号码已绑定有帐号，可直接登录");
					return result;
				}
				
			// 3如果是找回密码
			} else if (key.equals(SmsTypeKeyEnum.BACKPASSWORD.toString())) {
				smsContent = "您正在尝试找回密码，验证码为：" +  dynamicCode + ", 如非本人操作，请忽略本短信 【" + BRAND_PREFIX + "】";
				
				// 校验手机是否注册过  
				if (!validMobileIsRegister(mobile)) {
					result.put("state_code", 2);
					result.put("msg", "当前手机号没有绑定相关帐号");
					return result;
				}
			
			// 4是绑定帐号
			} else if (key.equals(SmsTypeKeyEnum.BINDING.toString())) {
				smsContent = "您正在绑定手机号，验证码为：" +  dynamicCode + ", 如非本人操作，请忽略本短信 【" + BRAND_PREFIX + "】";
				
				// 校验手机是否注册过
				if (validMobileIsRegister(mobile)) {
					result.put("state_code", 2);
					result.put("msg", "当前输入手机号码已绑定有帐号，请解绑后再绑定");
					return result;
				}
				
			// 5是修改密码
			} else if (key.equals(SmsTypeKeyEnum.UPDATE_PASSWORD.toString())) {
				smsContent = "您正在修改密码，验证码为：" +  dynamicCode + ", 如非本人操作，请忽略本短信 【" + BRAND_PREFIX + "】";
				
				// 校验手机是否注册过
				if (!validMobileIsRegister(mobile)) {
					result.put("state_code", 2);
					result.put("msg", "没有找到该手机号绑定的账户");
					return result;
				}
			// 6是普通校验
			} else if (key.equals(SmsTypeKeyEnum.CHECK.toString())) {
				
				// 如果需要验证用户是否注册
				if (isCheckRegister == 1) {
					
					// 校验手机是否注册过
					if (!validMobileIsRegister(mobile)) {
						result.put("state_code", 2);
						result.put("msg", "没有找到该手机号绑定的账户");
						return result;
					}
				}
				
				smsContent = "您好，您的验证码为：" +  dynamicCode + ", 如非本人操作，如非本人操作，请忽略本短信 【" + BRAND_PREFIX + "】";
			}
			
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("code", dynamicCode);
			
			//smsManager.send(mobile, smsContent, param);
			//更换短信发送通道
//			SmsMsg smsMsg = new SmsMsg();
//			smsMsg.setClientid("b04rg1");
//            smsMsg.setPassword(Md5Utils.md5Hex("gy123456"));
//            smsContent = "53534522423423423514141451";
//            smsMsg.setContent(smsContent);
//            smsMsg.setSmstype("4");
//            smsMsg.setMobile("18603047640");
//            
//            String requestString = JSONObject.toJSONString(smsMsg);
//            //JSONObject jsonSms = JSONObject.parseObject(requestString);
//            //log.info("发送调用短信参数:{}", requestString);
//            String response = null;
//			try {
//				response = HttpUtils.doPost("http://Request.ucpaas.com/sms-partner/access/b04rg1/sendsms", requestString);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//            //String response = "{\"data\":[{\"code\":0,\"fee\":1,\"mobile\":\"18603047640\",\"msg\":\"成功\",\"sid\":\"57d6053a-e9d3-43a7-ac3c-c697ae0254b8\",\"uid\":\"\"}],\"total_fee\":1}";
//            //String response = "{\"data\":[{\"code\":-1,\"fee\":0,\"mobile\":\"18603047640\",\"msg\":\"鉴权失败（账号或密码错误）\",\"sid\":\"b6a66207-d764-411e-8080-1cb887a88aaa\",\"uid\":\"\"}],\"total_fee\":0}";
//            //log.info("收到发送短信返回response:{}", response);
//          
//            if (null != response) {
//                JSONObject jsonObject = JSONObject.parseObject(response);
//                Object dataArr = jsonObject.get("data");
//                Integer totalFee = Integer.parseInt(jsonObject.get("total_fee").toString());
//                List<SmsResult> listResult = JSON.parseArray(dataArr.toString(), SmsResult.class);
//                //state_code值{0=发送失败，1=发送成功,2=发送限制(操作过快等等限制)
//                for (SmsResult smsResult : listResult) {
//                    if (0 == smsResult.getCode()) {
//                        //成功
//                    	result.put("state_code", 1);
//            			result.put("msg", "发送成功");
//                    } else {
//                    	//失败
//                    	result.put("state_code", 0);
//            			result.put("msg", "发送失败");
//                    }
//                }
//            }
			//调用转接系统
			JSONObject object = new JSONObject();
            object.put("content", smsContent);
            object.put("mobile", mobile);
            object.put("smsType", "10");
            String data = JSON.toJSONString(object);
            boolean smsResult = false;
			try {
				smsResult = sendSms(data);
			} catch (Exception e) {
				result.put("state_code", 0);
	  			result.put("msg", "发送失败，请稍后再试！");
	  			return result;
			}
			
			if (smsResult) {
              //成功
	          	result.put("state_code", 1);
	  			result.put("msg", "发送成功");
	        } else {
	          	//失败
	          	result.put("state_code", 0);
	  			result.put("msg", "发送失败");
	        }
	
			HttpSession session = ThreadContextHolder.getSession();
			// session中的格式是  前缀+key+手机号  例子:  es_sms_login_13123456789
			String codeSessionKey = SMS_CODE_PREFIX + key + mobile;
			session.setAttribute(codeSessionKey, dynamicCode);
			session.setAttribute(INTERVAL_TIME_PREFIX + key, DateUtil.getDateline());
			session.setAttribute(SENDTIME_PREFIX+key+mobile, DateUtil.getDateline());			
			String ip = ThreadContextHolder.getHttpRequest().getServerName();
			LOGGER.info("已发送短信:内容:" + smsContent + ",手机号:" + mobile + ",ip:" + ip);			
		} catch(RuntimeException e) {
			TestUtil.print(e);
			result.put("state_code", 0);
			result.put("msg", "发送失败,短信系统出现异常");
		}
		return result;
	}
	
	public static boolean sendSms(String data) throws Exception {
		//log.info("组装业务数据:{}", data);
        /******************************** 商户私钥对业务数据签名 */
        String sign = SignUtil.signMsg(getMerchantCertInfo(), data, SIGN_ALGORITHMS,
                CipherUtil.CHARSET);
        //log.info("商户私钥签名业务数据:{}", sign);

        /******************************** GYF平台公钥加密业务数据 */
        String encryptDataJson = CipherUtil.encryptData(getGyfCertInfo(), data,
                CipherUtil.PKCS1Padding, CipherUtil.CHARSET);
        //log.info("GYF公钥加密业务数据:{}", encryptDataJson);

        /******************************** 提交数据到工易付平台 */
        Map<String, String> map = new HashMap();
        map.put("merchantNo", "102121000000");
        map.put("msgId", String.valueOf(System.currentTimeMillis()));
        map.put("data", encryptDataJson);
        map.put("sign", sign);
		String toUrl = "https://common.gyfpay.com/api/send_sms";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(toUrl);
        List<org.apache.http.NameValuePair> nvps = new
                ArrayList<NameValuePair>();
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, String> entry = (Map.Entry) iter.next();
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        String strResult = EntityUtils.toString(response.getEntity());
        httpClient.close();
        return callback(strResult);
		
	}

	/**
     * 处理GYF返回结果
     *
     * @param resultMsg
     * @return
     */
    public static boolean callback(String resultMsg) throws Exception {
        if(StringUtils.isEmpty(resultMsg)){
            
        }
        JSONObject jsonObject = (JSONObject) JSON.parse(resultMsg);
        String resultRsp = null;
        //正常返回有data域
        String sendState = jsonObject.getString("data");
        if (sendState != null) {
        	if(sendState.equals("true")) {
        		return true;
        	}
        } else {
            
        }
        
        return false;
       
    }
    /**
     * 获取商户私钥
     *
     * @throws Exception
     */
    public static CertInfo getMerchantCertInfo() throws Exception {
        String cert_path= StringUtil.getRootPath();
        String zjcertPath =cert_path+"/WEB-INF";
//        String classPath = new File(CertInfo.class.getResource("/")
//                .getFile()).getCanonicalPath();
        String merchantCertInfoFile = zjcertPath + "/zjcert/102121000000.pfx";
        
        String merchantCertInfoPwd = "gyf188";
        //log.info("获取商户私钥证书 {}", merchantCertInfoFile);
        CertInfo merchantCertInfo = new CertInfo();
        merchantCertInfo.readKeyFromPKCS12(merchantCertInfoFile, merchantCertInfoPwd);
        return merchantCertInfo;
    }

    /**
     * 获取GYF公钥钥
     *
     * @throws Exception
     */
    public static CertInfo getGyfCertInfo() throws Exception {
        String cert_path= StringUtil.getRootPath();
        String zjcertPath =cert_path+"/WEB-INF";
//        String classPath = new File(CertInfo.class.getResource("/")
//                .getFile()).getCanonicalPath();
        String gyfCertInfoFile = zjcertPath + "/zjcert/bridge.cer";
        
        
        //log.info("获取GYF公钥证书 {}", gyfCertInfoFile);
        CertInfo gyfCertInfo = new CertInfo();
        gyfCertInfo.readPublicKeyFromX509Certificate(gyfCertInfoFile);
        return gyfCertInfo;
    }
	
	/**
	 * 验证手机验证码是否正确
	 * @param validCode 验证码
	 * @param mobile 手机号
	 * @param key key 类型key枚举 {@link SmsTypeKeyEnum}
	 * @exception RuntimeException 手机号格式错误出错
	 * @return
	 */
	public static boolean validSmsCode(String validCode, String mobile, String key) {
		// 如果手机号格式不对
		if ( !Validator.isMobile(mobile) ) {
			throw new RuntimeException("手机号码格式错误");
		}
		
		//防止 空值
		if (key == null || "".equals(key)) {
			
			// 默认为登录
			key = SmsTypeKeyEnum.LOGIN.toString();	
		}
		
		// 如果验证码为空
		if (validCode == null || "".equals(validCode)) {
			return false;
		}
		String code = (String) ThreadContextHolder.getSession().getAttribute(SMS_CODE_PREFIX + key + mobile);
		
		// 验证码为空
		if (code == null) {
			return false;
		} else {
			
			// 忽略大小写 判断  不正确
			if (!code.equalsIgnoreCase(validCode)) {
				return false;
			}
		}
		
		//新增优化  auth zjp 2016-12-13
		//验证短信是否超时
		Long sendtime = (Long) ThreadContextHolder.getSession().getAttribute(SENDTIME_PREFIX + key + mobile);
		Long checktime = DateUtil.getDateline();
		//验证session但中是否存在当前注册用户的验证码
		if(sendtime==null){
			return false;
		};
		if((checktime-sendtime >= SMS_CODE_TIMEOUT)){
			throw new RuntimeException("验证码超时");
		}	
		//验证通过后  去除session信息
		ThreadContextHolder.getSession().removeAttribute(SMS_CODE_PREFIX + key + mobile);
		return true;
	}
	
	/**
	 * 验证手机号有没有注册
	 * @param mobile 手机号
	 * @exception RuntimeException 手机号格式错误出错
	 * @return boolean false=没有注册 true=注册了
	 */
	public static boolean validMobileIsRegister(String mobile){
		
		// 如果手机号格式不对
		if ( !Validator.isMobile(mobile) ) {
			throw new RuntimeException("手机号码格式错误");
		}
		
		IMemberManager memberManager = SpringContextHolder.getBean("memberManager");
		boolean isExists = memberManager.checkMobile(mobile) != 0;
		return isExists;
	}
	
	/**
	 * 验证是否可以发送信息(做倒计时判断，同一种类型加以校验)
	 * @param key 类型key枚举 {@link SmsTypeKeyEnum}
	 * @return true=允许发送 false=不允许
	 */
	private static boolean validIsCanSendSms(String key){
		
		HttpSession session = ThreadContextHolder.getSession();
		
		// 当前时间
		Long now = DateUtil.getDateline();
		
		// session加上指定前缀
		Long lastGenTime = (Long) session.getAttribute(INTERVAL_TIME_PREFIX + key);	
		
		//如果lastGenTime不存在，即是第一次发送，允许发送；
		//如果发送间隔已超出限定间隔时间，允许发送；
		if (lastGenTime == null || CurrencyUtil.sub(now, lastGenTime) >= SEND_INTERVAL) {
			return true;
		} else {
			return false;
		}
		
	}
	
}
