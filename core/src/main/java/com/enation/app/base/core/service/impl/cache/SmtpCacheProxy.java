package com.enation.app.base.core.service.impl.cache;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.enation.app.base.core.model.Smtp;
import com.enation.app.base.core.service.ISmtpManager;
import com.enation.framework.cache.AbstractCacheProxy;
import com.enation.framework.cache.CacheFactory;
import com.enation.framework.cache.ICache;
import com.enation.framework.util.DateUtil;

@Service("smtpManager")
public class SmtpCacheProxy extends AbstractCacheProxy<List<Smtp>> implements
		ISmtpManager  {
	private static final String cacheName = "smtp_cache";
	
	@Autowired
	private ISmtpManager smtpManager;
 
	@Autowired
	public SmtpCacheProxy(ISmtpManager smtpDbManager){
		this.smtpManager = smtpDbManager;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.base.core.service.ISmtpManager#add(com.enation.app.base.core.model.Smtp)
	 */
	@Override
	public void add(Smtp smtp) {
		this.smtpManager.add(smtp);
		this.cleanCache();
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.base.core.service.ISmtpManager#edit(com.enation.app.base.core.model.Smtp)
	 */
	@Override
	public void edit(Smtp smtp) {
		this.smtpManager.edit(smtp);
		this.cleanCache();
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.base.core.service.ISmtpManager#delete(java.lang.Integer[])
	 */
	@Override
	public void delete(Integer[] idAr) {
		this.smtpManager.delete(idAr);
		this.cleanCache();
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.base.core.service.ISmtpManager#list()
	 */
	@Override
	public List<Smtp> list() {
		List<Smtp> smtpList = this.get();
		if(smtpList==null){
			smtpList = this.smtpManager.list();
			this.put(smtpList);
		}
		return smtpList;
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.base.core.service.ISmtpManager#sendOneMail(com.enation.app.base.core.model.Smtp)
	 */
	@Override
	public void sendOneMail(Smtp currSmtp) {
		
 
		currSmtp.setLast_send_time(DateUtil.getDateline());
		currSmtp.setSend_count(currSmtp.getSend_count()+1);
		
		this.smtpManager.sendOneMail(currSmtp);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.base.core.service.ISmtpManager#get(int)
	 */
	@Override
	public Smtp get(int id) {
		return this.smtpManager.get(id);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.base.core.service.ISmtpManager#getCurrentSmtp()
	 */
	@Override
	public Smtp getCurrentSmtp() {
		
		List<Smtp> smtpList = this.list();
		if( smtpList== null ) throw  new RuntimeException("???????????????smtp?????????");
		
		Smtp currentSmtp = null;
		
		//???????????????smtp  
		for(Smtp smtp:smtpList){
			if( checkCount(smtp)){
				currentSmtp= smtp;
				break;
			}
		}
		
		
		
		if(currentSmtp== null){
			this.logger.error("???????????????smtp");
			throw new RuntimeException("???????????????smtp?????????????????????????????? ");
		}
		
		if(this.logger.isDebugEnabled()){
			this.logger.debug("??????smtp->host["+currentSmtp.getHost()+"],username["+currentSmtp.getUsername()+"]");
		}
		
		return currentSmtp;
		
	}

	private boolean checkCount(Smtp smtp){
		long last_send_time = smtp.getLast_send_time(); //????????????????????????
		
		if(!DateUtil.toString(new Date(last_send_time*1000), "yyyy-MM-dd").equals(DateUtil.toString(new Date(), "yyyy-MM-dd"))){ //??????????????????
			smtp.setSend_count(0);
			
			if(this.logger.isDebugEnabled()){
				this.logger.debug("host["+smtp.getHost()+"]????????????,???smtp???????????????0");
			}
		}
		
		return smtp.getSend_count()< smtp.getMax_count();
	}
	
	private String getKey(){
		 
		return cacheName;
	}
	
	/**
	 * ????????????????????????
	 */
	private void cleanCache( ){
		ICache cache=CacheFactory.getCache(getKey());
		String mainkey = getKey();
		cache.remove( mainkey);
	 
	}
	
	private void put(List<Smtp> smtpList){
		ICache cache=CacheFactory.getCache(getKey());
		String mainkey =  getKey();
		cache.put(mainkey, smtpList);
	}
	
	private List<Smtp> get(){
		ICache cache=CacheFactory.getCache(getKey());
		String mainkey = getKey();
		return (List<Smtp>) cache.get(mainkey);
	}

	@Override
	public void testSend(Smtp smtp,String send_to) throws Exception{
		this.smtpManager.testSend(smtp,send_to);
	}

	

 


 
}
