package kr.msp.admin.securepush.apn.service;

import java.util.HashMap;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;

import kr.msp.admin.securepush.apn.mapper.ApnDao;

/**
 * APN 관련 서비스 클래스.
 */
@Service
public class ApnService {

	private static Logger logger = LoggerFactory.getLogger(ApnService.class);
	
	@Autowired(required=true)
	@Qualifier("securepush")
	SqlSessionTemplate sqlSessionTemplate_securepush;
	
	@Autowired(required=true)
	@Qualifier("securepush")
	private DataSourceTransactionManager transactionManager_securepush;
	
	/**
	 * APN IP 조회.
	 * @return
	 */
	public HashMap<String, String> getApn() {
		ApnDao apnDao = sqlSessionTemplate_securepush.getMapper(ApnDao.class);
		HashMap<String,String> apnMap = apnDao.getApn();
		if(apnMap != null){
			logger.debug("apn name ::: " + apnMap.get("APN_NAME"));
			logger.debug("apn ip ::: " + apnMap.get("APN_IP"));
		}else{
			// 값이 없을 경우 디폴트 구조 세팅.
			apnMap = new HashMap<String, String>();
			apnMap.put("APN_NAME", "");
			apnMap.put("APN_IP", "");
		}
		return apnMap;
	}
	
	/**
	 * APN IP 등록 처리.
	 * @param apnIp
	 * @return
	 * @throws Exception
	 */
	public boolean registerApn(HashMap<String,String> apnMap) throws Exception {
		logger.debug("requested APN MAP : " + apnMap);
		boolean rt = false;
		ApnDao apnDao = sqlSessionTemplate_securepush.getMapper(ApnDao.class);
		
		// 이미 등록된 값이 있으면 업데이트, 없으면 insert.
		HashMap<String,String> dbApnMap = apnDao.getApn();
		if(dbApnMap == null || dbApnMap.isEmpty()){
			apnDao.insertApnIp(apnMap);
		}else{
			logger.debug("DB APN MAP : " + dbApnMap);
			apnDao.updateApnIp(apnMap);
		}
		rt = true;
		return rt;
	}
}
