package com.enation.app.base.core.util.smsutil;





public class SmsMsg {
    private String mobile;
    private String smstype;
    private String content;
    /**
     * 发送时间，为空立即发送
     */
    private String sendtime;
    /**
     * 扩展字段
     */
    private String extend;
    /**
     * 透传ID
     */
    private String uid;
    /**
     * 帐号
     */
    private String clientid;
    private String password;
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getSmstype() {
		return smstype;
	}
	public void setSmstype(String smstype) {
		this.smstype = smstype;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSendtime() {
		return sendtime;
	}
	public void setSendtime(String sendtime) {
		this.sendtime = sendtime;
	}
	public String getExtend() {
		return extend;
	}
	public void setExtend(String extend) {
		this.extend = extend;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getClientid() {
		return clientid;
	}
	public void setClientid(String clientid) {
		this.clientid = clientid;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

    
    
}
