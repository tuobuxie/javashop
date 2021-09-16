package com.enation.app.shop.component.payment.plugin.alipay.sdk34.api.domain;

import com.enation.app.shop.component.payment.plugin.alipay.sdk34.api.AlipayObject;
import com.enation.app.shop.component.payment.plugin.alipay.sdk34.api.internal.mapping.ApiField;

/**
 * 个性化扩展区上下线接口
 *
 * @author auto create
 * @since 1.0, 2017-04-27 10:50:31
 */
public class AlipayOpenPublicPersonalizedExtensionSetModel extends AlipayObject {

	private static final long serialVersionUID = 6371277347242433392L;

	/**
	 * 扩展区套id，调用创建个性化扩展区接口时返回
	 */
	@ApiField("extension_key")
	private String extensionKey;

	/**
	 * 扩展区操作类型，支持2个值：ON、OFF，ON代表上线操作，OFF代表下线操作。当上线一个扩展区时，若存在同样的标签规则，且状态为上线的扩展区，该扩展区会自动下线
	 */
	@ApiField("status")
	private String status;

	public String getExtensionKey() {
		return this.extensionKey;
	}
	public void setExtensionKey(String extensionKey) {
		this.extensionKey = extensionKey;
	}

	public String getStatus() {
		return this.status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

}
