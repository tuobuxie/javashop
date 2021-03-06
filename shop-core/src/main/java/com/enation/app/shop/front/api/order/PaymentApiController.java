package com.enation.app.shop.front.api.order;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enation.app.shop.core.payment.service.IPaymentPlugin;
import com.enation.framework.context.spring.SpringContextHolder;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.dom4j.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.enation.app.base.core.service.IRegionsManager;
import com.enation.app.base.core.util.sqpayutil.SqSignUtil;
import com.enation.app.shop.core.goods.utils.AmountUtils;
import com.enation.app.shop.core.goods.utils.h5utils.CertInfo;
import com.enation.app.shop.core.goods.utils.h5utils.CipherUtil;
import com.enation.app.shop.core.goods.utils.h5utils.HttpUtils;
import com.enation.app.shop.core.goods.utils.h5utils.SignUtil;
import com.enation.app.shop.core.member.model.MemberAddress;
import com.enation.app.shop.core.member.service.IMemberAddressManager;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.PayCfg;
import com.enation.app.shop.core.order.service.IOrderManager;
import com.enation.app.shop.core.order.service.IPaymentManager;
import com.enation.app.shop.core.order.service.impl.OrderManager;
import com.enation.framework.action.GridController;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;
import com.enation.framework.util.TestUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;


/**
 * ??????api
 * @author kingapex
 * 2013-9-4??????7:21:31
 * @author Sylow
 * @version v2.0,2016???2???20???
 * @since v6.0
 */
@Controller
@RequestMapping("/api/shop/payment")
public class PaymentApiController extends GridController  implements ApplicationContextAware{
	
    private WebApplicationContext context;
	
	@Autowired
	private IPaymentManager paymentManager;
	@Autowired
	private IOrderManager orderManager;
	@Autowired
	private IMemberAddressManager memberAddressManager;
	@Autowired
	private IRegionsManager regionsManager;

    static String SIGN_ALGORITHMS = "SHA1WithRSA";
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context=(WebApplicationContext) applicationContext;
		
	}


    
	/**
	 * ??????????????????????????????
	 * @return
	 */
//	@ResponseBody
//	@RequestMapping(value="/execute",produces=MediaType.TEXT_HTML_VALUE)
//	public String execute(){
//		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
//		
//		//??????id??????
//		Integer orderId=  StringUtil.toInt( request.getParameter("orderid") ,null);
//		if(orderId == null ){
//			return "????????????orderid??????";
//		}
//		
//		//????????????id??????
//		Integer paymentId=  StringUtil.toInt( request.getParameter("paymentid") ,null);
//		Order order = this.orderManager.get(orderId);
//		
//		if(order==null){
//			return "??????????????????";
//		}
//		
//		//??????????????????????????????id????????????????????????????????????
////		if(paymentId==null){
////			paymentId = order.getPayment_id(); 
////			if(paymentId == 0){
////				paymentId = 1;
////			}
////		}
////		
////		PayCfg payCfg = this.paymentManager.get(paymentId);
////		
////		IPaymentPlugin paymentPlugin = SpringContextHolder.getBean(payCfg.getType());
////		//String payhtml = paymentPlugin.onPay(payCfg, order);
////		String payhtml = "";
////		// ???????????????????????????????????????????????????
////		if (order.getPayment_id().intValue() != paymentId.intValue()) {
////			this.orderManager.updatePayMethod(orderId, paymentId, payCfg.getType(), payCfg.getName());
////		}
//		
//		String payhtml = "";
//		try {
//            /******************************** ???????????? */
//            //H5??????
//            JSONObject data = new JSONObject();
//            data.put("orderId", order.getOrder_id());
//            data.put("orderDate", DateUtils.formatDate(new Date(), "yyyyMMdd"));
//            data.put("orderNote", "??????");
//            data.put("orderRemark", "??????");
//            data.put("orderAmount", AmountUtils.getAmountToPenny(order.getOrder_amount().toString()));//?????????
//            data.put("notifyBgUrl", "https://sale.weiyifu123.com/sale-server/h5/callBackNotify");
//            data.put("mobile", order.getShip_mobile());
//            String dataStr = JSON.toJSONString(data);
//            //log.info("H5??????????????????:{},{}", orderInfo.getTransNo(), dataStr);
//
//            /******************************** ????????????????????????????????? */
//            String sign = SignUtil.signMsg(getMerchantCertInfo(), dataStr, SIGN_ALGORITHMS,
//                    CipherUtil.CHARSET);
//            /******************************** GYF?????????????????????????????? */
//            String encryptDataJson = CipherUtil.encryptData(getGyfCertInfo(), dataStr,
//                    CipherUtil.PKCS1Padding, CipherUtil.CHARSET);
//            /******************************** ?????????????????????????????? */
//            Map<String, String> map = new HashMap();
//            map.put("merchantNo", "10008");
//            map.put("msgId", order.getOrder_id().toString());
//            map.put("data", encryptDataJson);
//            map.put("sign", sign);
//            //log.info("??????H5????????????,{},{}", order.getOrder_id(), LogFormatUtil.formatBean(map));
//            String strResult = HttpUtils.sendPost("https://api.weiyifu88.com/api/payment", map);
//            return callback(strResult);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//
//	}
	
	/**
     * ??????????????????
     *
     * @throws Exception
     */
    public static CertInfo getMerchantCertInfo() throws Exception {
    	String cert_path= StringUtil.getRootPath();
        String h5certPath =cert_path+"/WEB-INF";
//        String classPath = new File(CertInfo.class.getResource("/")
//                .getFile()).getCanonicalPath();
        String merchantCertInfoFile = h5certPath + "/h5cert/10008.pfx";
        String merchantCertInfoPwd = "202006";
        CertInfo merchantCertInfo = new CertInfo();
        merchantCertInfo.readKeyFromPKCS12(merchantCertInfoFile, merchantCertInfoPwd);
        return merchantCertInfo;
    }

    /**
     * ??????GYF?????????
     *
     * @throws Exception
     */
    public static CertInfo getGyfCertInfo() throws Exception {
    	String cert_path= StringUtil.getRootPath();
        String h5certPath =cert_path+"/WEB-INF";
//        String classPath = new File(CertInfo.class.getResource("/")
//                .getFile()).getCanonicalPath();
        String gyfCertInfoFile = h5certPath + "/h5cert/gyf-pro.cer";
        CertInfo gyfCertInfo = new CertInfo();
        gyfCertInfo.readPublicKeyFromX509Certificate(gyfCertInfoFile);
        return gyfCertInfo;
    }
	


	

	/**
	 * ??????????????????????????????
	 * @param addrid ??????id ??????
	 * @return result result 1.??????.0.?????????
	 */
	@ResponseBody
	@RequestMapping(value="/check-support-cod")
	public JsonResult checkSupportCod(int addrid) {
		MemberAddress memberAddress = memberAddressManager.getAddress(addrid);
		try {
			if (regionsManager.get(memberAddress.getRegion_id()).getCod() == 1) {
				return JsonResultUtil.getSuccessJson("??????????????????");
			} else {
				return JsonResultUtil.getErrorJson("?????????????????????");
			}
		} catch (RuntimeException e) {  
			return JsonResultUtil.getErrorJson("?????????????????????");
		}
	}
	
	
	/**
	 * ??????????????????
	 * @param orderId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/alipay-qrcode",produces=MediaType.TEXT_HTML_VALUE)
	public String alipayQrcode(Integer orderId){
		
		Order order = this.orderManager.get(orderId);
		String qrimg = this.paymentManager.getPayQrCdoe(order,"alipayDirectPlugin");
		return qrimg;
	}
	
	/**
	 * ???????????????
	 * @param resp
	 * @param orderId
	 * @throws IOException
	 */
	@RequestMapping(value = "/wechat-qrcode", method = { RequestMethod.POST, RequestMethod.GET })
    public void weChatQrcode(HttpServletResponse resp, Integer orderId) throws IOException {
		
		Order order = this.orderManager.get(orderId);
		String url="";
		try {
			 url = this.paymentManager.getPayQrCdoe(order,"weixinPayPlugin");
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error(e.getMessage(), e);
		}
		
		
        if (url != null && !"".equals(url)) {
            ServletOutputStream stream = null;
            try {

                int width = 300;//???????????????
                int height = 300;//??????
                stream = resp.getOutputStream();
                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix m = writer.encode(url, BarcodeFormat.QR_CODE, height, width);
                MatrixToImageWriter.writeToStream(m, "png", stream);
            } catch (WriterException e) {
                e.printStackTrace();
            } finally {
                if (stream != null) {
                    stream.flush();
                    stream.close();
                }
            }
        }
    }
	
	/**
	 * ??????????????????????????????????????????
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/get-pay-status-for-wechat")
	public JsonResult getPayStatusForWechat(Integer orderId,String pluginId){
		
		Order order = this.orderManager.get(orderId);
		String str = "";
		try {
			str = this.paymentManager.getPayStatus(order,pluginId);;
		} catch (RuntimeException e) {
			TestUtil.print(e);
			return JsonResultUtil.getErrorJson("????????????????????????????????????,???????????????????????????");
		}
		if(str!=null && "SUCCESS".equals(str)){
			return JsonResultUtil.getSuccessJson("????????????");
		}
		return JsonResultUtil.getErrorJson("????????????");
	}
	
	/**
	 * ??????????????????????????????????????????
	 * @return
	 */
//	@ResponseBody
//	@RequestMapping(value="/getH5PayResult")
//	public JsonResult getH5PayState(Integer orderId,String pluginId){
//		
//		Order order = this.orderManager.get(orderId);
////		String str = "";
////		try {
////			str = this.paymentManager.getPayStatus(order,pluginId);;
////		} catch (RuntimeException e) {
////			TestUtil.print(e);
////			return JsonResultUtil.getErrorJson("????????????????????????????????????,???????????????????????????");
////		}
////		if(str!=null && "SUCCESS".equals(str)){
////			return JsonResultUtil.getSuccessJson("????????????");
////		}
//		return JsonResultUtil.getErrorJson("????????????");
//	}
	
	
	@ResponseBody
	@RequestMapping(value="/getH5PayResult",produces=MediaType.TEXT_HTML_VALUE)
	public void getH5PayResult(){
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		
		//??????id??????
		Integer orderId=  StringUtil.toInt( request.getParameter("orderid") ,null);
		if(orderId == null ){
			//return JsonResultUtil.getErrorJson("????????????orderid??????");
		}
		
		//????????????id??????
		Integer paymentId=  StringUtil.toInt( request.getParameter("paymentid") ,null);
		Order order = this.orderManager.get(orderId);
		
		if(order==null){
			//return JsonResultUtil.getErrorJson("??????????????????");
		}
		
		//??????H5????????????
		String payQueryResult = "";
		try {
            /******************************** ???????????? */
			JSONObject data = new JSONObject();
            data.put("orderDate", order.getSn().substring(0, 8));
            data.put("orderId", order.getSn());
            String dataStr = JSON.toJSONString(data);
            //log.info("??????????????????:{}", dataStr);
            /******************************** ????????????????????????????????? */
            String sign = SignUtil.signMsg(getMerchantCertInfo(), dataStr, SIGN_ALGORITHMS,
                    CipherUtil.CHARSET);
            /******************************** GYF?????????????????????????????? */
            String encryptDataJson = CipherUtil.encryptData(getGyfCertInfo(), dataStr,
                    CipherUtil.PKCS1Padding, CipherUtil.CHARSET);
            /******************************** ?????????????????????????????? */
            Map<String, String> map = new HashMap();
            map.put("merchantNo", "10008");
            map.put("msgId", order.getSn());
            map.put("data", encryptDataJson);
            map.put("sign", sign);
            String strResult = HttpUtils.sendPost("https://api.weiyifu88.com/api/query", map);
            //log.info("H5??????????????????:"+strResult);
            payQueryResult = callbackResult(strResult);
            if(payQueryResult!=null) {
            	JSONObject jsonObject = (JSONObject) JSON.parse(payQueryResult);
            	
            	if ("0000".equals(jsonObject.get("code"))) {
            		String queryResultData = jsonObject.getString("data");
            		JSONObject jsonData = (JSONObject) JSON.parse(queryResultData);
            		if(jsonData.get("transState").equals("0")) {
            			//??????
            			//return JsonResultUtil.getSuccessJson("????????????");
            			//??????????????????
            		}
            		
                    
                }
            	
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
			
		
	}
	

	@SuppressWarnings({ "unchecked", "unused" })
	public void getH5PayResultJob(){
		OrderManager orderManager = SpringContextHolder.getBean("orderManager");
		List<Order> orderList = orderManager.listPayOrders(0);
		if(orderList.size()==0){
	
		}else {
			//??????????????????
			int payState = 0;//0-?????????  1-????????????   2-?????????
			for (Order order : orderList) {
				payState = queryForPayResult(order.getSn());
				if(payState ==2) {
					orderManager.updatePayState(order.getSn(),order.getOrder_amount(),2,2);
				}
			}
			
		}

	}
	
	public int queryForPayResult(String sn) {
		//??????H5????????????
		int shopPayState = 0;//0-?????????  1-????????????   2-?????????
		String payQueryResult = "";
		try {
            /******************************** ???????????? */
			JSONObject data = new JSONObject();
            data.put("orderDate", sn.substring(0, 8));
            data.put("orderId", sn);
            String dataStr = JSON.toJSONString(data);
            //log.info("??????????????????:{}", dataStr);
            /******************************** ????????????????????????????????? */
            String sign = SignUtil.signMsg(getMerchantCertInfo(), dataStr, SIGN_ALGORITHMS,
                    CipherUtil.CHARSET);
            /******************************** GYF?????????????????????????????? */
            String encryptDataJson = CipherUtil.encryptData(getGyfCertInfo(), dataStr,
                    CipherUtil.PKCS1Padding, CipherUtil.CHARSET);
            /******************************** ?????????????????????????????? */
            Map<String, String> map = new HashMap();
            map.put("merchantNo", "10008");
            map.put("msgId", sn);
            map.put("data", encryptDataJson);
            map.put("sign", sign);
            String strResult = HttpUtils.sendPost("https://api.weiyifu88.com/api/query", map);
            //log.info("H5??????????????????:"+strResult);
            payQueryResult = callbackResult(strResult);
            //payQueryResult = "{\"code\":\"0000\",\"data\":{\"merchantFee\":13,\"merchantNo\":10006,\"orderAmount\":2600,\"orderDate\":20200620,\"orderId\":\"20200620092716799975\",\"resultCode\":\"0001\",\"resultNote\":\"??????\",\"transId\":\"12020062009271710000\",\"transState\":0},\"msg\":\"????????????\"}";
            //payQueryResult = "{\"code\":\"0004\",\"msg\":\"???????????????,???????????????\"}";
            if(payQueryResult!=null&&!payQueryResult.equals("")) {
            	JSONObject jsonObject = (JSONObject) JSON.parse(payQueryResult);
            	if ("0000".equals(jsonObject.get("code"))) {
            		String queryResultData = jsonObject.getString("data");
            		JSONObject jsonData = (JSONObject) JSON.parse(queryResultData);
            		if(jsonData.get("transState").equals(0)) {
            			shopPayState = 2;
            		}else {
            			shopPayState = 0;
            		}

                }
            	
            }
            
            
        } catch (Exception e) {
        	shopPayState = 0;
        }
		return shopPayState;
	}
	
	
	
	
	/**
     * ??????GYF????????????H5????????????
     *
     * @param resultMsg
     * @return
     */
    public static String callbackResult(String resultMsg) throws Exception {
    	if(StringUtils.isEmpty(resultMsg)){
            
        }
        JSONObject jsonObject = (JSONObject) JSON.parse(resultMsg);
        String resultRsp = null;
        //???????????????data???
        String dataObject = jsonObject.getString("data");
        if (dataObject != null) {
        	JSONObject dataJsonObject = (JSONObject) JSON.parse(dataObject);
            /******************************** ?????????????????? */
            String encryptedKey = dataJsonObject.getString("key");
            String encryptedData = dataJsonObject.getString("content");
            resultRsp = CipherUtil.decryptData(getMerchantCertInfo(), encryptedKey, encryptedData,
                    CipherUtil.PKCS1Padding, CipherUtil.CHARSET);
            //log.info("???????????????:{}", resultRsp);

            /****GYF?????????????????? */
            String sign = jsonObject.getString("sign");
            boolean verifySign = SignUtil.verifyMsg(getGyfCertInfo(), resultRsp, sign, null, null);
            //log.info("??????????????????:{}", verifySign);
        } else {
            
        }
        return resultRsp;
    }


    
	@SuppressWarnings({ "unchecked", "unused" })
	public void getSqQrPayResultJob(){
		OrderManager orderManager = SpringContextHolder.getBean("orderManager");
		List<Order> orderList = orderManager.listPayOrders(0);
		if(orderList.size()==0){
	
		}else {
			//??????????????????
			int payState = 0;//0-?????????  1-????????????   2-?????????
			for (Order order : orderList) {
				payState = queryForQrPayResult(order.getSn());
				if(payState ==2) {
					orderManager.updatePayState(order.getSn(),order.getOrder_amount(),2,2);
				}
			}
			
		}

	}

	public int queryForQrPayResult(String sn) {
		//???????????????????????????????????????
		int shopPayState = 0;//0-?????????  1-????????????   2-?????????
		String payQueryResult = "";
		try {
            /******************************** ???????????? */
//			JSONObject data = new JSONObject();
//            data.put("orderDate", sn.substring(0, 8));
//            data.put("orderId", sn);
//            String dataStr = JSON.toJSONString(data);
//            //log.info("??????????????????:{}", dataStr);
//            /******************************** ????????????????????????????????? */
//            String sign = SignUtil.signMsg(getMerchantCertInfo(), dataStr, SIGN_ALGORITHMS,
//                    CipherUtil.CHARSET);
//            /******************************** GYF?????????????????????????????? */
//            String encryptDataJson = CipherUtil.encryptData(getGyfCertInfo(), dataStr,
//                    CipherUtil.PKCS1Padding, CipherUtil.CHARSET);
//            /******************************** ?????????????????????????????? */
//            Map<String, String> map = new HashMap();
//            map.put("merchantNo", "10008");
//            map.put("msgId", sn);
//            map.put("data", encryptDataJson);
//            map.put("sign", sign);
			// ????????????
            Map<String, String> reqMap = new LinkedHashMap<>();
            // ????????????????????????
            reqMap.put("BillNo", sn);
            // ?????????
            reqMap.put("MerNo", "204403");//??????????????????????????????
            //??????
            reqMap.put("MD5Info", SqSignUtil.md5SignValue(reqMap, "fdbdxUz)"));
            String strResult = HttpUtils.sendPost("https://query.95epay.cn/searchInterfaceSingleFR.action", reqMap);
            //log.info("H5??????????????????:"+strResult);
            if(StringUtils.isBlank(strResult)) {
            	return 0;//????????????  
            }
            
            Map<String, String> xmlMap = parseXml(strResult);
            String message = xmlMap.get("message");
            String resultCode = xmlMap.get("message");
            String state = xmlMap.get("State");
            String isSettlement = xmlMap.get("IsSettlement");
            //???????????????0??????,1??????,2?????????,3??????,4??????????????? ???????????????0???????????????1????????????
            if ("1".equals(state) && "1".equals(isSettlement)) {
            	shopPayState = 2;
            } else if ("0".equals(state) || "9".equals(state)) {
            	shopPayState = 0;
            }
              
        } catch (Exception e) {
        	shopPayState = 0;
        }
		return shopPayState;
	}
	
	
	/**
     * ??????xml??????
     *
     * @param respMsg
     * @return
     */
    private Map<String, String> parseXml(String respMsg) {
        Map<String, String> xmlMap = new LinkedHashMap<>();
        try {
            Document document = DocumentHelper.parseText(respMsg);
            Element rootElement = document.getRootElement();
            Iterator it = rootElement.elementIterator();
            while (it.hasNext()) {
                Element node = (Element) it.next();
                if ("MerInfo".equals(node.getName())) {
                    Iterator attributeIterator = node.attributeIterator();
                    while (attributeIterator.hasNext()) {
                        Attribute attribute = (Attribute) attributeIterator.next();
                        if ("MerNo".equals(attribute.getName())) {
                            xmlMap.put("MerNo", attribute.getValue());
                        }
                        if ("BillNo".equals(attribute.getName())) {
                            xmlMap.put("BillNo", attribute.getValue());
                        }
                        if ("message".equals(attribute.getName())) {
                            xmlMap.put("message", attribute.getValue());
                        }
                        if ("ResultCode".equals(attribute.getName())) {
                            xmlMap.put("ResultCode", attribute.getValue());
                        }
                    }
                    Iterator nodeIterator = node.elementIterator();
                    while (nodeIterator.hasNext()) {
                        Element node1 = (Element) nodeIterator.next();
                        if ("OrderInfo".equals(node1.getName())) {
                            Iterator OrderInfoIterator = node1.attributeIterator();
                            while (OrderInfoIterator.hasNext()) {
                                Attribute attribute = (Attribute) OrderInfoIterator.next();
                                if ("OrderNo".equals(attribute.getName())) {
                                    xmlMap.put("OrderNo", attribute.getValue());
                                }
                                if ("Amount".equals(attribute.getName())) {
                                    xmlMap.put("Amount", attribute.getValue());
                                }
                                if ("Date".equals(attribute.getName())) {
                                    xmlMap.put("Date", attribute.getValue());
                                }
                                if ("State".equals(attribute.getName())) {
                                    xmlMap.put("State", attribute.getValue());
                                }
                                if ("remark".equals(attribute.getName())) {
                                    xmlMap.put("remark", attribute.getValue());
                                }
                                if ("MD5ResultInfo".equals(attribute.getName())) {
                                    xmlMap.put("MD5ResultInfo", attribute.getValue());
                                }
                                if ("IsSettlement".equals(attribute.getName())) {
                                    xmlMap.put("IsSettlement", attribute.getValue());
                                }
                            }
                        }
                    }
                }
            }
        } catch (DocumentException e) {
            
        }
        return xmlMap;
    }
	
}
