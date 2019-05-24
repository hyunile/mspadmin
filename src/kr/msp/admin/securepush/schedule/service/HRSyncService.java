package kr.msp.admin.securepush.schedule.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;

import kr.msp.admin.securepush.registerHR.service.RegisterHRService;
import kr.msp.admin.securepush.schedule.dto.DeptDto;
import kr.msp.admin.securepush.schedule.dto.EmpDto;
import kr.msp.admin.securepush.schedule.mapper.HRSyncLegacyDao;
import kr.msp.util.Sha256Util;

/**
 * 인사 정보 동기화 스케쥴링 job을 실행.<br/>
 * 사이트 요구 사항에 따라 이 클래스 구현 로직이 달라져야 함.
 */
@Service
public class HRSyncService {

	private static Logger logger = LoggerFactory.getLogger(HRSyncService.class);
	
	@Autowired(required=true)
	@Qualifier("securepushLegacyHR")
	private SqlSessionTemplate sqlSessionTemplate_securepushLegacyHR;
	
	@Autowired(required=true)
	@Qualifier("securepushLegacyHR")
	private DataSourceTransactionManager transactionManager_securepushLegacyHR;
	
	@Autowired(required=true)
	private RegisterHRService registerHrService;
	
	public void syncHR(){
		long startTime = System.currentTimeMillis();
		
		try{
			syncDeptInfo();
			logger.info("[부서 테이블 동기화(업데이트/추가)] 총 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		try{
			syncEmployeeInfo();
			logger.info("[조직원 테이블 동기화] 총 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		
		try{
			deleteDeptInfo();
			logger.info("[부서 테이블 동기화(삭제)] 총 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
			startTime = System.currentTimeMillis();
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
	}
	
	/**
	 * 부서 테이블 정보를 동기화한다.
	 */
	public void syncDeptInfo() throws Exception{
		
		HRSyncLegacyDao legacyHRDao = sqlSessionTemplate_securepushLegacyHR.getMapper(HRSyncLegacyDao.class);
		
		// 현재 레거시 인사 DB의 부서 정보 목록을 가져온다. 
		Map<String, Object> paramMap0 = new HashMap<String, Object>();
		List<DeptDto> legacyDeptList = legacyHRDao.selectDeptList(paramMap0);
		
		registerHrService.registerDeptInfo(legacyDeptList);
		
	}
	
	/**
	 * 부서 테이블 삭제 동기화.
	 * @throws Exception
	 */
	public void deleteDeptInfo() throws Exception {
		HRSyncLegacyDao legacyHRDao = sqlSessionTemplate_securepushLegacyHR.getMapper(HRSyncLegacyDao.class);
		
		// 현재 레거시 인사 DB의 부서 정보 목록을 가져온다. 
		Map<String, Object> paramMap0 = new HashMap<String, Object>();
		List<DeptDto> legacyDeptList = legacyHRDao.selectDeptList(paramMap0);
		
		registerHrService.deleteDeptInfo(legacyDeptList);
	}
	
	
	/**
	 * 조직원 테이블 정보를 동기화한다.
	 */
	public void syncEmployeeInfo() throws Exception{
		// TODO: 유라클 직책 정보를 알아오는 방법을 알아봐야 함.
		// 직급은 매칭할 수 있으나 직책 중 알아내기 어려운 것들이 있음.
		// 예) 이사 -> 부문장?본부장?소장?
		// 단말 UI 상에 직책과 직급이 표시되지 않으므로 
		// 알고 있는 매칭 조건으로 프로그램 로직 상에서 분기 처리하고 
		// 매칭할 수 없는 값은 빈 값으로 처리.
		
		HRSyncLegacyDao legacyHRDao = sqlSessionTemplate_securepushLegacyHR.getMapper(HRSyncLegacyDao.class);
		
		// 현재 레거시 인사 DB의 조직원 목록을 가져온다. 
		Map<String, Object> paramMap0 = new HashMap<String, Object>();
		List<EmpDto> legacyEmpList = legacyHRDao.selectEmpList(paramMap0);
		
		// TODO: 유라클 인사 DB에서 GROUPNAME이 폐기부서가 아닌 조직원을 임포트하면 누락되는 사용자가 발생함.
		// (GROUPNAME이 폐기부서인데 GROUPCODE에 맵핑된 부서는 폐기부서가 아닌 경우)
		// 부서 테이블의 부서명이 폐기 부서가 아닌 조직원을 임포트해도 폐기된 부서의 조직원이 더 임포트되므로 
		// 확실한 조회 조건을 확인할 필요 있음.
		// 지금은 폐기된 부서의 조직원이 더 임포트되더라도 그것을 걸러내도록 처리함.
		// 000010002 - 경영지원실 (폐기부서)
		legacyEmpList = removeInvalidDeptCdUser(legacyEmpList);
		
		setPosRespCdUserIdAndPhotoPath(legacyEmpList);
		
		// 사이트 요구 사항에 따라 DB 인증을 하고 패스워드 암호화가 필요할 경우 
		// 위 메쏘드 안이나 이 위치에서 패스워드 암호화 처리를 넣을 것.
		
		registerHrService.registerEmployeeInfo(legacyEmpList);
		
	}
	
	/**
	 * 조직원 목록에서 실제 부서 정보 테이블에 존재하는 부서 코드를 가진 조직원 목록만 걸러냄.
	 * @param legacyEmpList
	 * @return
	 */
	private List<EmpDto> removeInvalidDeptCdUser(List<EmpDto> legacyEmpList) {
		List<EmpDto> _resultList = new ArrayList<EmpDto>();
		if(legacyEmpList != null){
			HRSyncLegacyDao legacyHRDao = sqlSessionTemplate_securepushLegacyHR.getMapper(HRSyncLegacyDao.class);
			Map<String, Object> paramMap0 = new HashMap<String, Object>();
			List<DeptDto> legacyDeptList = legacyHRDao.selectDeptList(paramMap0);
			if(legacyDeptList != null && legacyDeptList.size() > 0){
				for(EmpDto _empDto : legacyEmpList){
					if(_empDto.getDEPT_CD() != null){
						if(getDeptByDeptCd(legacyDeptList, _empDto.getDEPT_CD()) != null){
							_resultList.add(_empDto);
						}
					}
				}
			}
		}
		return _resultList;
	}
	
	/**
	 * 부서 목록에서 매칭되는 부서코드의 부서 정보를 가져옴.
	 * @param deptList
	 * @param deptCd
	 * @return
	 */
	private DeptDto getDeptByDeptCd(List<DeptDto> deptList, String deptCd) {
		DeptDto deptDto = null;
		
		for(DeptDto _deptDto : deptList){
			if(_deptDto.getDEPT_CD() != null && deptCd.equals(_deptDto.getDEPT_CD())){
				deptDto = _deptDto;
				break;
			}
		}
		
		return deptDto;
	}
	
	/**
	 * 레거시 인사 DB 조직원 목록에 매칭되는 직급, 직책 정보를 세팅한다.
	 * @param empList
	 */
	private void setPosRespCdUserIdAndPhotoPath(List<EmpDto> legacyEmpList){
		if(legacyEmpList != null){
			for(EmpDto empDto : legacyEmpList){
				// 유라클 사용자 아이디 발급 규칙은 사원 번호 앞에 'u' 캐릭터가 붙는 형식.
				empDto.setUSER_ID(getUserIdFromEmpNo(empDto.getEMP_NO()));
				empDto.setPOS_CD(getPosCd(empDto.getPOSITION()));
				empDto.setRESP_CD(getRespCd(empDto.getPOSITION()));
				empDto.setPHOTO_PATH(getPhotoPath(empDto.getEMP_NO()));
				// 유라클 로그인 아이디는 사용자명과 같은 값으로 세팅.
				empDto.setLOGIN_ID(empDto.getUSER_NM());
			}
		}
	}
	
	/**
	 * 유라클 position (직급명)값으로 매칭되는 직급 코드를 리턴.
	 * @param posName
	 * @return
	 */
	private String getPosCd(String posName) {
		//유라클 position (직급) distinct 값.
		// CEO
		// 전무이사
		// 상무이사
		// 이사
		// 수석연구원
		// 부장
		// 책임연구원
		// 차장
		// 선임연구원
		// 과장
		// 주임연구원
		// 대리
		// 연구원
		// 사원
		// 관리자
		String posCd = null;
		if(posName != null){
			if("CEO".equals(posName))
				posCd = "P000001";
			else if("전무이사".equals(posName) || posName.contains("전무"))
				posCd = "P000002";
			else if("상무이사".equals(posName) || posName.contains("상무"))
				posCd = "P000003";
			else if("이사".equals(posName) || posName.contains("이사"))
				posCd = "P000004";
			else if("수석연구원".equals(posName) || posName.contains("수석"))
				posCd = "P000005";
			else if("부장".equals(posName))
				posCd = "P000005";
			else if("책임연구원".equals(posName) || posName.contains("책임"))
				posCd = "P000006";
			else if("차장".equals(posName))
				posCd = "P000006";
			else if("선임연구원".equals(posName) || posName.contains("선임"))
				posCd = "P000007";
			else if("과장".equals(posName))
				posCd = "P000007";
			else if("주임연구원".equals(posName) || posName.contains("주임"))
				posCd = "P000008";
			else if("대리".equals(posName))
				posCd = "P000008";
			else if("연구원".equals(posName))
				posCd = "P000009";
			else if("사원".equals(posName))
				posCd = "P000009";
		}
		
		return posCd;
	}
	
	/**
	 * 유라클 position 값으로 매칭되는 직책 코드를 리턴.
	 * @param respName
	 * @return
	 */
	private String getRespCd(String respName) {
		String respCd = null;
		if(respName != null){
			if("CEO".equals(respName))
				respCd = "R000001";
			//else if("전무이사".equals(respName) || respName.contains("전무"))
			//else if("상무이사".equals(respName) || respName.contains("상무"))
			//else if("이사".equals(respName) || respName.contains("이사"))
			else if("수석연구원".equals(respName) || respName.contains("수석"))
				respCd = "R000007";
			else if("부장".equals(respName))
				respCd = "R000007";
			else if("책임연구원".equals(respName) || respName.contains("책임"))
				respCd = "R000008";
			else if("차장".equals(respName))
				respCd = "R000008";
			else if("선임연구원".equals(respName) || respName.contains("선임"))
				respCd = "R000009";
			else if("과장".equals(respName))
				respCd = "R000009";
			else if("주임연구원".equals(respName) || respName.contains("주임"))
				respCd = "R000010";
			else if("대리".equals(respName))
				respCd = "R000010";
			else if("연구원".equals(respName))
				respCd = "R000011";
			else if("사원".equals(respName))
				respCd = "R000011";
		}
		
		return respCd;
	}
	
	/**
	 * 유라클 인사 DB 기준, 사원 번호로부터 시큐어 푸쉬 사용자 아이디를 발급하여 리턴.
	 * @param empNo
	 * @return
	 */
	private String getUserIdFromEmpNo(String empNo) {
		return "u" + empNo;
	}
	
	/**
	 * 복호화 불가능한 암호화 처리.
	 * @param pwd
	 * @return
	 */
	private String encryptPwd(String pwd) {
		String encrypted = pwd;
		if(pwd != null && !"".equals(pwd))
			encrypted = Sha256Util.getEncrypt(pwd);
		
		return encrypted;
	}
	
	/**
	 * 클라이언트에서 프로필 이미지 표시를 위해 내려지는 시큐어푸쉬 gw 프로필 이미지 조회 url을 조합하여 리턴.
	 * @param empNo
	 * @return
	 */
	private String getPhotoPath(String empNo) {
		// 아래 프로퍼티 처리할 것.
		String photoPath = "http://211.241.199.220:8080/secure-push-gw/api/securepush/getProfileImage?user_id=";
		return photoPath + getUserIdFromEmpNo(empNo);
	}
	
	
}
