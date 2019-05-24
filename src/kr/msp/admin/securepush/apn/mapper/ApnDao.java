package kr.msp.admin.securepush.apn.mapper;

import java.util.HashMap;

/**
 * APN 관련 DB 입출력 클래스.
 */
public interface ApnDao {

	/**
	 * APN 조회.
	 * @return
	 */
	public HashMap<String,String> getApn();
	
	/**
	 * APN 최초 등록.
	 * @param paramMap
	 * @return
	 */
	public int insertApnIp(HashMap<String,String> apnMap);
	
	/**
	 * APN 업데이트.
	 * @param paramMap
	 * @return
	 */
	public int updateApnIp(HashMap<String,String> apnMap);
}
