package com.enation.app.base.core.util.smsutil;



/**
 * 发送短信返回信息
 *
 * @author caiwei
 */

public class SmsResult implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *短信请求想要返回码
     */
    protected Integer code;

    /**
     * 短信请求响应返回中文描述
     */
    protected String msg;

    /**
     * 成功发送的短信计费条数
     */
    protected Integer fee;

    /**
     * 手机号
     */
    protected String mobile;

    /**
     * 短信标识符
     */
    protected String sid;

    /**
     * 用户透传id
     */
    protected String uid;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getFee() {
		return fee;
	}

	public void setFee(Integer fee) {
		this.fee = fee;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

    
    
    
}
