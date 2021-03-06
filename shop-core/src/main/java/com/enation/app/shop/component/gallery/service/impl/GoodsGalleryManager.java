package com.enation.app.shop.component.gallery.service.impl;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.enation.app.base.core.model.ClusterSetting;
import com.enation.app.base.core.service.ISettingService;
import com.enation.app.base.core.upload.IUploader;
import com.enation.app.base.core.upload.UploadFacatory;
import com.enation.app.shop.component.gallery.model.GoodsGallery;
import com.enation.app.shop.component.gallery.service.IGoodsGalleryManager;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.eop.SystemSetting;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.eop.sdk.utils.IClusterFileManager;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.context.spring.SpringContextHolder;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.image.IThumbnailCreator;
import com.enation.framework.image.ThumbnailCreatorFactory;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.FileUtil;
import com.enation.framework.util.ImageMagickMaskUtil;
import com.enation.framework.util.StringUtil;

@Service("goodsGalleryManager")
public class GoodsGalleryManager implements IGoodsGalleryManager {

	@Autowired
	private IGoodsManager goodsManager;

	@Autowired
	private ISettingService settingService;

	@Autowired
	private IDaoSupport daoSupport;


	private static IClusterFileManager getClusterFileManager(){
		if(ClusterSetting.getFdfs_open()==1){
			return (IClusterFileManager)SpringContextHolder.getBean("clusterFileManager");
		}
		return null;

	}
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void add(GoodsGallery gallery) {
		this.daoSupport.insert("es_goods_gallery", gallery);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.component.gallery.service.IGoodsGalleryManager#updateSort(java.lang.String, int)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateSort(String original, int sort) {
		// TODO Auto-generated method stub
		String sql = "UPDATE es_goods_gallery SET sort = ? WHERE  original = ?";
		this.daoSupport.execute(sql, sort, original);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void delete(Integer[] goodsid) {
		String id_str = StringUtil.arrayToString(goodsid, ",");
		List<GoodsGallery> result = this.daoSupport.queryForList("select * from es_goods_gallery where goods_id in (" + id_str + ")", GoodsGallery.class);
		// ????????????????????????
		for (GoodsGallery gallery : result) {
			this.deletePohto(gallery.getOriginal());
			this.deletePohto(gallery.getBig());
			this.deletePohto(gallery.getSmall());
			this.deletePohto(gallery.getThumbnail());
			this.deletePohto(gallery.getTiny());
		}

		String sql = "delete from es_goods_gallery where goods_id in (" + id_str + ")";
		this.daoSupport.execute(sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.shop.component.gallery.service.IGoodsGalleryManager#delete
	 * (int)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void delete(int goodsid) {
		this.daoSupport.execute("delete from es_goods_gallery where goods_id=?", goodsid);
	}

	@Override
	public List<GoodsGallery> list(int goods_id) {
		List<GoodsGallery> result = this.daoSupport.queryForList("select gg.*,g.params from es_goods_gallery gg left join es_goods g on gg.goods_id=g.goods_id where gg.goods_id = ? ORDER BY gg.sort", GoodsGallery.class, goods_id);
		for (GoodsGallery gallery : result) {
			if (!StringUtil.isEmpty(gallery.getOriginal()))
				gallery.setOriginal(gallery.getOriginal());
			if (!StringUtil.isEmpty(gallery.getBig()))
				gallery.setBig(StaticResourcesUtil.convertToUrl(gallery.getBig()));
			if (!StringUtil.isEmpty(gallery.getSmall()))
				gallery.setSmall(StaticResourcesUtil.convertToUrl(gallery.getSmall()));
			if (!StringUtil.isEmpty(gallery.getThumbnail()))
				gallery.setThumbnail(StaticResourcesUtil.convertToUrl(gallery.getThumbnail()));
			if (!StringUtil.isEmpty(gallery.getTiny()))
				gallery.setTiny(StaticResourcesUtil.convertToUrl(gallery.getTiny()));			
		}
		return result;
	}

	/**
	 * ?????????????????????<br>
	 * ?????????????????????????????????????????????????????????<br>
	 * ?????????????????????????????????
	 */
	private void deletePohto(String photoName) {
		if (photoName != null) {
			photoName = StaticResourcesUtil.convertToUrl(photoName);
			IUploader uploader=UploadFacatory.getUploaer();
			uploader.deleteFile(photoName);
		}
	}

	/**
	 * ????????????????????????????????? ?????????????????????????????????????????????
	 */
	public void delete(String photoName) {
		String static_server_domain= SystemSetting.getStatic_server_domain();
		List<GoodsGallery> galleryList = this.daoSupport.queryForList("SELECT * FROM es_goods_gallery WHERE original=?", GoodsGallery.class, photoName.replaceAll(static_server_domain, EopSetting.FILE_STORE_PREFIX));
		for(GoodsGallery gallery : galleryList){
			this.deletePohto(gallery.getOriginal());
			this.deletePohto(gallery.getBig());
			this.deletePohto(gallery.getSmall());
			this.deletePohto(gallery.getThumbnail());
			this.deletePohto(gallery.getTiny());
		}
		this.daoSupport.execute("delete from es_goods_gallery where original=?", photoName.replaceAll(static_server_domain, EopSetting.FILE_STORE_PREFIX));

	}

	/**
	 * ??????????????????<br>
	 * ???????????????????????????????????????????????????????????????????????????<br>
	 * ??????????????????????????????????????????+?????????????????????????????????<br>
	 * ?????????????????????????????????????????????????????????????????????fs:?????????????????????????????????,???:<br>
	 * http://static.enationsoft.com/user/1/1/attachment/goods/1.jpg??????????????????:
	 * fs:/attachment/goods/1.jpg
	 */
	public String upload(MultipartFile file) {
		String fileName = null;
		String filePath = "";

		String path = null;
		String static_server_domain= SystemSetting.getStatic_server_domain();
		String static_server_path= SystemSetting.getStatic_server_path();
		if (file != null && file.getOriginalFilename() != null) {
			String ext = FileUtil.getFileExt(file.getOriginalFilename());
			fileName = DateUtil.toString(new Date(), "yyyyMMddHHmmss") + StringUtil.getRandStr(4) + "." + ext;
			filePath = static_server_path + "/attachment/goods/";
			path =static_server_domain + "/attachment/goods/" + fileName;
			filePath += fileName;
			FileUtil.write(file, filePath);

			String watermark = settingService.getSetting("photo", "watermark");
			String marktext = settingService.getSetting("photo", "marktext");
			String markpos = settingService.getSetting("photo", "markpos");
			String markcolor = settingService.getSetting("photo", "markcolor");
			String marksize = settingService.getSetting("photo", "marksize");

			marktext = StringUtil.isEmpty(marktext) ? "????????????" : marktext;
			marksize = StringUtil.isEmpty(marksize) ? "12" : marksize;

			if (watermark != null && watermark.equals("on")) {
				ImageMagickMaskUtil magickMask = new ImageMagickMaskUtil();
				String app_apth = StringUtil.getRootPath();

				magickMask.mask(filePath, marktext, markcolor, Integer.valueOf(marksize), Integer.valueOf(markpos),	app_apth + "/font/st.TTF");
			}
		}
		return path;
	}



	/**
	 * ?????????????????????<br>
	 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????<br>
	 * ?????????????????????????????????????????????
	 */
	public void createThumb(String filepath, String thumbName, int thumbnail_pic_width, int thumbnail_pic_height) {
		if (filepath != null) {
			String static_server_domain= SystemSetting.getStatic_server_domain();
			String static_server_path= SystemSetting.getStatic_server_path();
			// //System.out.println("????????????["+thumbName+"]");
			String serverPath = static_server_path.replaceAll("\\\\", "/");
			filepath = filepath.replaceAll(static_server_domain, serverPath);
			filepath = filepath.replaceAll("\\\\","/");
			thumbName = thumbName.replaceAll(static_server_domain, serverPath);
			thumbName = thumbName.replaceAll("\\\\","/");

			// ???????????????????????????http???????????????????????????????????????????????????
			if (filepath.startsWith("http")) {
				// //System.out.println("??????");
				return;
			}

			File tempFile = new File(thumbName);
			if (tempFile.exists()) {
				// //System.out.println("?????????");
			} else {
				IThumbnailCreator thumbnailCreator = ThumbnailCreatorFactory.getCreator(filepath, thumbName);
				thumbnailCreator.resize(thumbnail_pic_width, thumbnail_pic_height);
				// //System.out.println("????????????");
			}
		}
	}

	@Override
	public int getTotal() {
		return this.goodsManager.list().size();
	}

	@Override
	public void recreate(int start, int end) {
		int tiny_pic_width = 60;
		int tiny_pic_height = 60;
		int thumbnail_pic_width = 107;
		int thumbnail_pic_height = 107;
		int small_pic_width = 320;
		int small_pic_height = 240;
		int big_pic_width = 550;
		int big_pic_height = 412;

		/**
		 * ????????????????????????????????????
		 */
		try {
			tiny_pic_width = Integer.valueOf(getSettingValue("tiny_pic_width").toString());
			tiny_pic_height = Integer.valueOf(getSettingValue("tiny_pic_height").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			thumbnail_pic_width = Integer.valueOf(getSettingValue("thumbnail_pic_width").toString());
			thumbnail_pic_height = Integer.valueOf(getSettingValue("thumbnail_pic_height").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			small_pic_width = Integer.valueOf(getSettingValue("small_pic_width").toString());
			small_pic_height = Integer.valueOf(getSettingValue("small_pic_height").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			big_pic_width = Integer.valueOf(getSettingValue("big_pic_width").toString());
			big_pic_height = Integer.valueOf(getSettingValue("big_pic_height").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<Map> goodsList = this.goodsManager.list();

		for (int i = start - 1; i < end; i++) {
			Map goods = goodsList.get(i);

			int goodsid = (Integer) goods.get("goods_id");
			List<GoodsGallery> galleryList = this.daoSupport.queryForList("select * from es_goods_gallery where goods_id = ?", GoodsGallery.class, goodsid);

			if (galleryList != null) {
				String static_server_domain= SystemSetting.getStatic_server_domain();
				String static_server_path= SystemSetting.getStatic_server_path();

				//System.out.println("Create thumbnail image, the index:"	+ (i + 1));
				for (GoodsGallery gallery : galleryList) {
					String imgFile = gallery.getOriginal();
					String realPath = StaticResourcesUtil.convertToUrl(imgFile);
					realPath = realPath.replaceAll(static_server_domain, static_server_path);
					System.out.print("Create Image for file:" + realPath + "...");

					if(FileUtil.exist(realPath)) {
						// ??????????????????
						String thumbName = gallery.getTiny();
						this.createThumb1(realPath, thumbName, tiny_pic_width,	tiny_pic_height);

						// ???????????????
						thumbName = gallery.getThumbnail();
						this.createThumb1(realPath, thumbName, thumbnail_pic_width,	thumbnail_pic_height);

						// ????????????
						thumbName = gallery.getSmall();
						createThumb1(realPath, thumbName, small_pic_width, small_pic_height);

						// ????????????
						thumbName = gallery.getBig();
						createThumb1(realPath, thumbName, big_pic_width, big_pic_height);

						//System.out.println(" OK");
					} else {
						//System.out.println(" file not found");
					}
				}
			}
		}
	}		

	private String getSettingValue(String code) {
		return settingService.getSetting("photo", code);
	}

	/**
	 * ?????????????????????
	 * 
	 * @param filepath
	 * @param thumbName
	 * @param thumbnail_pic_width
	 * @param thumbnail_pic_height
	 */
	private void createThumb1(String filepath, String thumbName, int thumbnail_pic_width, int thumbnail_pic_height) {
		if (!StringUtil.isEmpty(filepath)) {
			String static_server_path= SystemSetting.getStatic_server_path();

			filepath = filepath.replaceAll(EopSetting.FILE_STORE_PREFIX, static_server_path);
			thumbName = thumbName.replaceAll(EopSetting.FILE_STORE_PREFIX,static_server_path);
			IThumbnailCreator thumbnailCreator = ThumbnailCreatorFactory.getCreator(filepath, thumbName);
			thumbnailCreator.resize(thumbnail_pic_width, thumbnail_pic_height);
		}
	}

	/**
	 * ???????????????????????????
	 * @param relativePath	??????????????????fs://attachment/a.jpg
	 * @return
	 */
	private static String uploadToCluster(String relativePath){
		IClusterFileManager fileManager = getClusterFileManager();
		if(fileManager == null)
			return relativePath;
		String filePath = relativePath;
		if(relativePath.startsWith(EopSetting.FILE_STORE_PREFIX)){
			filePath = StringUtils.replace(relativePath, EopSetting.FILE_STORE_PREFIX, SystemSetting.getStatic_server_path());
		}
		String[] filePaths = relativePath.split("\\/");
		InputStream stream=null;
		try {
			stream=new URL(filePath).openStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileManager.upload(stream, filePaths[filePaths.length-1], relativePath);
	}

}