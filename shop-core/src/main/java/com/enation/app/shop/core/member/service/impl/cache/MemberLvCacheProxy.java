package com.enation.app.shop.core.member.service.impl.cache;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.base.core.model.MemberLv;
import com.enation.app.shop.core.member.service.IMemberLvManager;
import com.enation.framework.cache.AbstractCacheProxy;
import com.enation.framework.cache.CacheFactory;
import com.enation.framework.cache.ICache;
import com.enation.framework.database.Page;
@Service("memberLvManager")
public class MemberLvCacheProxy extends AbstractCacheProxy<List<MemberLv> > implements IMemberLvManager {

	private IMemberLvManager memberLvManager;
	
	public static final String CACHE_KEY= "member_lv";
	@Autowired
	public MemberLvCacheProxy(IMemberLvManager memberLvDbManager) {
		this.memberLvManager = memberLvDbManager;
	}
	
	private void cleanCache(){
		ICache cache=CacheFactory.getCache(CACHE_KEY);
		cache.remove(CACHE_KEY) ;
	}
	
	public void add(MemberLv lv) {
		memberLvManager.add(lv);
		cleanCache();
	}

	
	public void edit(MemberLv lv) {
		memberLvManager.edit(lv);
		cleanCache();
	}

	
	public Integer getDefaultLv() {
		return memberLvManager.getDefaultLv();
	}

	
	public Page list(String order, int page, int pageSize) {
		return memberLvManager.list(order, page, pageSize);
	}

	
	public MemberLv get(Integer lvId) {
		return memberLvManager.get(lvId);
	}

	
	public void delete(Integer[] id) {
		memberLvManager.delete(id);
		cleanCache();
	}

	
	public List<MemberLv> list() {
		ICache cache=CacheFactory.getCache(CACHE_KEY);
		List<MemberLv> memberLvList  = (List<MemberLv>) cache.get(CACHE_KEY);
		if(memberLvList == null){
			memberLvList  = this.memberLvManager.list();
			cache.put(CACHE_KEY, memberLvList);
			if(this.logger.isDebugEnabled()){
				this.logger.debug("load memberLvList from database");
			}			
		} else{
			if(this.logger.isDebugEnabled()){
				this.logger.debug("load memberLvList from cache");
			}
		}
		return memberLvList;
	}
	
	public MemberLv getNextLv(int point){
		return memberLvManager.getNextLv(point);
	}

	
	public List<MemberLv> list(String ids) {
		return memberLvManager.list(ids);		 
	}


	public MemberLv getByPoint(int point) {
		return memberLvManager.getByPoint(point);
	}
	
	/**
	 * ?????????????????????????????????????????????
	 * @param lv_id
	 * @return
	 */
	public List getCatDiscountByLv(int lv_id){
		return memberLvManager.getCatDiscountByLv(lv_id);
	}
	
	/**
	 * ???????????????????????????????????????????????????
	 * @param lv_id
	 * @return
	 */
	public List getHaveCatDiscountByLv(int lv_id){
		return memberLvManager.getHaveCatDiscountByLv(lv_id);
	}
	
	/**
	 * ??????????????????????????????????????????
	 * @param cat_ids ??? discount?????????????????????
	 * @param discounts ???catids?????????????????????
	 * @param lv_id
	 */
	public void saveCatDiscountByLv(int[] cat_ids,int[] discounts,int lv_id){
		memberLvManager.saveCatDiscountByLv(cat_ids, discounts, lv_id);
	}
	/**
	 * ???????????????????????????????????????
	 * @param cat_id
	 */
	public List getHaveLvDiscountByCat(int cat_id){
		return memberLvManager.getHaveLvDiscountByCat(cat_id);
	}
}
