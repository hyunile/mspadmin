package kr.msp.admin.securepush.registerHR.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import kr.msp.admin.base.utils.ExcelUtil;
import kr.msp.admin.securepush.registerHR.dto.RegisterHRParamDto;
import kr.msp.admin.securepush.schedule.dto.DeptDto;
import kr.msp.admin.securepush.schedule.dto.EmpDto;
import kr.msp.admin.securepush.schedule.dto.PosDto;
import kr.msp.admin.securepush.schedule.dto.RespDto;
import kr.msp.admin.securepush.schedule.dto.UserDto;
import kr.msp.admin.securepush.schedule.mapper.HRSyncDao;
import kr.msp.util.Sha256Util;

/**
 * 시큐어푸쉬 인사 DB 동기화 수행 클래스.
 */
@Service
public class RegisterHRService {

	private static Logger logger = LoggerFactory.getLogger(RegisterHRService.class);
	
	@Autowired(required=true)
	@Qualifier("securepush")
	SqlSessionTemplate sqlSessionTemplate_securepush;
	
	@Autowired(required=true)
	@Qualifier("securepush")
	private DataSourceTransactionManager transactionManager_securepush;
	
	/**
	 * 엑셀 파일로부터 인사 정보 등록 수행.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	public boolean registerHRFile(RegisterHRParamDto paramDto) throws Exception {
		boolean rt = false;
		
		// 엑셀 파일 파싱 및 데이터 로딩.
		if(paramDto.getATTACH_FILE()==null || paramDto.getATTACH_FILE().isEmpty())
			throw new Exception("엑셀 파일이 첨부되지 않았습니다.");
		
		XSSFWorkbook workBook = new XSSFWorkbook(paramDto.getATTACH_FILE().getInputStream());
		List<Map<String, String>> deptMapList = parseExcel(workBook, 0);
		List<Map<String, String>> posMapList = parseExcel(workBook, 1);
		List<Map<String, String>> respMapList = parseExcel(workBook, 2);
		List<Map<String, String>> empMapList = parseExcel(workBook, 3);
		
		List<DeptDto> deptDtoList = convertToDeptDtoList(deptMapList);
		List<PosDto> posDtoList = convertToPosDtoList(posMapList);
		List<RespDto> respDtoList = convertToRespDtoList(respMapList);
		List<EmpDto> empDtoList = convertToEmpDtoList(empMapList, deptDtoList);
		
		if(deptDtoList.size()==0 && posDtoList.size()==0 && respDtoList.size()==0 && empDtoList.size()==0)
			throw new Exception("등록할 데이터가 없습니다.");
		
		// DB sync 실행.
		registerDeptInfo(deptDtoList);
		registerPosInfo(posDtoList);
		registerRespInfo(respDtoList);
		registerEmployeeInfo(empDtoList);
		deleteDeptInfo(deptDtoList);
		
		rt = true;
		
		return rt;
	}
	
	/**
	 * ExcelUtil 유틸 클래스 메쏘드 재정의.<br/>
	 * 인자로 주어진 시트 번호에 해당하는 엑셀 데이터 목록을 추출한다.<br/>
	 * 첫번째 row의 값을 키로 사용.
	 * @param workBook
	 * @param sheetNum
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> parseExcel(XSSFWorkbook workBook, int sheetNum) throws Exception {
		List<Map<String, String>> rtList = new ArrayList<Map<String, String>>();
		
		XSSFSheet sheet = ExcelUtil.getSheetXlsx(workBook, sheetNum);
		
		int rows = ExcelUtil.getRowSizeXlsx(sheet, 1);
		
		// 첫번째 row의 값을 이용하여 기본 맵 키 배열 세팅.
		String[] headers = new String[]{};
		XSSFRow headerRow = sheet.getRow(0);
		if(headerRow!=null)
		for(int cellIndex = 0, length = headerRow.getLastCellNum(); cellIndex < length; cellIndex++) {
			XSSFCell cell = headerRow.getCell(cellIndex);
			String cellValue = ExcelUtil.getCellValueXlsx( cell );
			
			if( cellValue != null && cellValue.length() > 0) {
				headers = (String[])ArrayUtils.add(headers, cellValue);
			}
		}
		
		for(int rowIndex = 1; rowIndex < rows; rowIndex++) {
			XSSFRow row = sheet.getRow(rowIndex);
			if(row!=null){
				Map<String, String> _map = new HashMap<String, String>();
				for(int cellIndex = 0, length = row.getLastCellNum(); cellIndex < length; cellIndex++) {
					XSSFCell cell = row.getCell(cellIndex);
					String cellValue = ExcelUtil.getCellValueXlsx( cell );
					
					_map.put(headers[cellIndex], cellValue);
					
				}
				rtList.add(_map);
			}
		}
		
		return rtList;
	}
	
	
	/**
	 * 직급 정보를 시큐어푸쉬 직급 테이블에 등록한다.
	 * @param legacyPosList
	 * @throws Exception
	 */
	public void registerPosInfo(List<PosDto> legacyPosList) throws Exception {
		long startTime = System.currentTimeMillis();
		
		if(legacyPosList != null && legacyPosList.size() > 0){
			
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			TransactionStatus status = transactionManager_securepush.getTransaction(def);
			
			try{
				HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
				hrSyncDao.deletePositions();
				
				int updateCnt = 0;
				updateCnt = hrSyncDao.insertPositions(legacyPosList);
				
				logger.info("[직급 테이블 동기화] 업데이트된 수:" + updateCnt + ", 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
				
				transactionManager_securepush.commit(status);
			}catch(Exception e){
				transactionManager_securepush.rollback(status);
				throw e;
			}
		
		}
	}
	
	/**
	 * 직책 정보를 시큐어푸쉬 직책 테이블에 등록한다.
	 * @param legacyRespList
	 * @throws Exception
	 */
	public void registerRespInfo(List<RespDto> legacyRespList) throws Exception {
		long startTime = System.currentTimeMillis();
		
		if(legacyRespList != null && legacyRespList.size() > 0){
			
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			TransactionStatus status = transactionManager_securepush.getTransaction(def);
			
			try{
				HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
				hrSyncDao.deleteResps();
				
				int updateCnt = 0;
				updateCnt = hrSyncDao.insertResps(legacyRespList);
				
				logger.info("[직책 테이블 동기화] 업데이트된 수:" + updateCnt + ", 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
				
				transactionManager_securepush.commit(status);
			}catch(Exception e){
				transactionManager_securepush.rollback(status);
				throw e;
			}
		
		}
	}
	
	/**
	 * 엑셀에서 파싱한 부서 맵 구조를 dto리스트로 변환.
	 * @param deptMapList
	 * @return
	 */
	private List<DeptDto> convertToDeptDtoList(List<Map<String,String>> deptMapList) {
		List<DeptDto> deptList = new ArrayList<DeptDto>();
		
		if(deptMapList != null && deptMapList.size() > 0){
			for(Map<String, String> _deptMap : deptMapList){
				if(_deptMap.get("DEPT_CD") != null && !"".equals(_deptMap.get("DEPT_CD"))){
					DeptDto _deptDto = new DeptDto();
					_deptDto.setDEPT_CD(_deptMap.get("DEPT_CD"));
					_deptDto.setDEPT_NM(_deptMap.get("DEPT_NM"));
					_deptDto.setFULL_NM(_deptMap.get("FULL_NM"));
					_deptDto.setUP_DEPT_CD(_deptMap.get("UP_DEPT_CD"));
					deptList.add(_deptDto);
				}
			}
		}
		
		return deptList;
	}
	
	/**
	 * 엑셀에서 파싱한 직급 맵 구조를 dto리스트로 변환.
	 * @param posMapList
	 * @return
	 */
	private List<PosDto> convertToPosDtoList(List<Map<String,String>> posMapList) {
		List<PosDto> posList = new ArrayList<PosDto>();
		
		if(posMapList != null && posMapList.size() > 0){
			for(Map<String, String> _posMap : posMapList){
				if(_posMap.get("POS_CD") != null && !"".equals(_posMap.get("POS_CD"))){
					PosDto _posDto = new PosDto();
					_posDto.setPOS_CD(_posMap.get("POS_CD"));
					_posDto.setPOS_NM(_posMap.get("POS_NM"));
					posList.add(_posDto);
				}
			}
		}
		
		return posList;
	}
	
	/**
	 * 엑셀에서 파싱한 직책 맵 구조를 dto리스트로 변환.
	 * @param respMapList
	 * @return
	 */
	private List<RespDto> convertToRespDtoList(List<Map<String,String>> respMapList) {
		List<RespDto> respList = new ArrayList<RespDto>();
		
		if(respMapList != null && respMapList.size() > 0){
			for(Map<String, String> _respMap : respMapList){
				if(_respMap.get("RESP_CD") != null && !"".equals(_respMap.get("RESP_CD"))){
					RespDto _respDto = new RespDto();
					_respDto.setRESP_CD(_respMap.get("RESP_CD"));
					_respDto.setRESP_NM(_respMap.get("RESP_NM"));
					respList.add(_respDto);
				}
			}
		}
		
		return respList;
	}
	
	/**
	 * 엑셀에서 파싱한 조직원 맵 구조를 dto리스트로 변환.
	 * @param empMapList
	 * @return
	 */
	private List<EmpDto> convertToEmpDtoList(List<Map<String,String>> empMapList, List<DeptDto> deptDtoList) {
		List<EmpDto> empList = new ArrayList<EmpDto>();
		
		if(empMapList != null && empMapList.size() > 0){
			for(Map<String, String> _empMap : empMapList){
				if(_empMap.get("EMP_NO") != null && !"".equals(_empMap.get("EMP_NO"))){
					EmpDto _empDto = new EmpDto();
					_empDto.setEMP_NO(_empMap.get("EMP_NO"));
					_empDto.setCELL_NO(_empMap.get("CELL_NO"));
					_empDto.setDEPT_CD(_empMap.get("DEPT_CD"));
					DeptDto matchedDeptDto = getDeptByDeptCd(deptDtoList, _empMap.get("DEPT_CD"));
					if(matchedDeptDto != null)
						_empDto.setDEPT_NM(matchedDeptDto.getDEPT_NM());
					_empDto.setEMAIL(_empMap.get("EMAIL"));
					_empDto.setUSER_NM(_empMap.get("USER_NM"));
					_empDto.setLOGIN_ID(_empMap.get("LOGIN_ID"));
					// 사이트 요구 사항에 따라 DB 인증 방식을 사용하지 않고 패스워드 암호화를 하지 
					// 않는 경우 아래 암호화 처리 로직을 뺄 것.
					_empDto.setLOGIN_PWD(encryptPwd(_empMap.get("LOGIN_PWD")));
					_empDto.setPHOTO_PATH("");
					_empDto.setPOS_CD(_empMap.get("POS_CD"));
					_empDto.setRESP_CD(_empMap.get("RESP_CD"));
					_empDto.setTEL_NO(_empMap.get("TEL_NO"));
					_empDto.setWORK_YN("Y");
					_empDto.setUSER_ID(getUserIdFromEmpNo(_empMap.get("EMP_NO")));
					empList.add(_empDto);
				}
			}
		}
		
		return empList;
	}
	
	/**
	 * 인사 DB 사원 번호 기준, 시큐어 푸쉬 사용자 아이디를 발급하여 리턴.
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
	 * 부서 정보를 시큐어푸쉬 부서 테이블에 등록한다.
	 * @param legacyDeptList
	 * @throws Exception
	 */
	public void registerDeptInfo(List<DeptDto> legacyDeptList) throws Exception {
		long startTime = System.currentTimeMillis();
		
		if(legacyDeptList == null || legacyDeptList.size() == 0)
			throw new Exception("레거시 인사 DB 부서 테이블에 유효한 데이터가 없습니다.");
		
		// 현재 시큐어 푸쉬 DB의 부서 정보 목록을 가져온다.
		HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
		Map<String, Object> paramMap1 = new HashMap<String, Object>();
		List<DeptDto> deptList = hrSyncDao.selectDeptList(paramMap1);
		
		// 두 개의 목록을 비교하여 같은 부서 코드의 정보가 변경되었으면 업데이트하고 
		int updateCnt = 0;
		try{
			updateCnt = compareAndUpdateDept(legacyDeptList, deptList);
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		logger.info("[부서 테이블 동기화] 업데이트된 수:" + updateCnt + ", 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		
		// 없어진 부서는 지우고, 신규 추가된 부서는 추가한다.
		int insertCnt = findNewAndInsertDept(legacyDeptList, deptList);
		logger.info("[부서 테이블 동기화] 신규 등록된 수:" + insertCnt + ", 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		
		// 조직원 테이블에서 참조가 걸려 삭제가 안되므로 부서 정보 삭제는 조직원 테이블 동기화 후 처리한다.
	}
	
	/**
	 * 삭제된 부서 정보를 삭제한다.
	 * @param legacyDeptList
	 * @throws Exception
	 */
	public void deleteDeptInfo(List<DeptDto> legacyDeptList) throws Exception {
		long startTime = System.currentTimeMillis();
		
		if(legacyDeptList == null || legacyDeptList.size() == 0)
			throw new Exception("레거시 인사 DB 부서 테이블에 유효한 데이터가 없습니다.");
		
		// 현재 시큐어 푸쉬 DB의 부서 정보 목록을 가져온다.
		HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
		Map<String, Object> paramMap1 = new HashMap<String, Object>();
		List<DeptDto> deptList = hrSyncDao.selectDeptList(paramMap1);
		
		int delCnt = findDeletedAndDeleteDept(legacyDeptList, deptList);
		logger.info("[부서 테이블 동기화] 삭제된 수:" + delCnt + ", 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
	}
	
	public void registerEmployeeInfo(List<EmpDto> legacyEmpList) throws Exception{
		long startTime = System.currentTimeMillis();
		
		if(legacyEmpList == null || legacyEmpList.size() == 0)
			throw new Exception("레거시 인사 DB 조직원 테이블에 유효한 데이터가 없습니다.");
		
		// 현재 시큐어 푸쉬 DB의 조직원 목록을 가져온다.
		HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
		Map<String, Object> paramMap1 = new HashMap<String, Object>();
		List<EmpDto> empList = hrSyncDao.selectEmpList(paramMap1);
		
		// 두 개의 목록을 비교하여 같은 사원번호의 정보가 변경되었으면 업데이트한다. 
		// 가입된 사용자 테이블의 부서코드, 부서명 정보를 업데이트한다.
		int updateCnt = 0;
		try{
			updateCnt = compareAndUpdateEmp(legacyEmpList, empList);
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		logger.info("[조직원 테이블 동기화] 업데이트된 수:" + updateCnt + ", 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		
		// 추가된 조직원 정보를 시큐어 푸쉬 조직원 DB에 추가한다. 
		// 자동 서비스 가입 시나리오 이므로 가입된 사용자 테이블에 추가된 조직원 
		// 정보를 추가한다.
		int insertCnt = findNewAndInsertEmp(legacyEmpList, empList);
		logger.info("[조직원 테이블 동기화] 신규 등록된 수:" + insertCnt + ", 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
				
		// 없어진 조직원 정보는 WORK_YN 필드의 값을 N으로 변경.
		int delCnt = findDeletedAndDeleteEmp(legacyEmpList, empList);
		logger.info("[조직원 테이블 동기화] 삭제된 수:" + delCnt + ", 처리 시간(millis):" + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
	}
	
	
	/**
	 * 신규 추가된 부서를 등록한다.
	 * @param legacyDeptList
	 * @param deptList
	 * @return
	 * @throws Exception
	 */
	private int findNewAndInsertDept(List<DeptDto> legacyDeptList, List<DeptDto> deptList) throws Exception {
		int insertCnt = 0;
		
		List<DeptDto> newList = new ArrayList<DeptDto>();
		for(DeptDto _legacyDeptDto : legacyDeptList){
			String _legacyDeptCd = _legacyDeptDto.getDEPT_CD();
			if(_legacyDeptCd != null){
				DeptDto matchedDeptDto = getDeptByDeptCd(deptList, _legacyDeptCd);
				if(matchedDeptDto == null)
					newList.add(_legacyDeptDto);
			}
		}
		
		if(newList != null && newList.size() > 0){
			HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
			insertCnt = hrSyncDao.insertDepts(newList);
		}
		
		return insertCnt;
	}
	
	/**
	 * 두 개의 부서 목록을 비교하여 변경되었으면 업데이트.
	 * @param legacyDeptList
	 * @param deptList
	 * @return
	 * @throws Exception
	 */
	private int compareAndUpdateDept(List<DeptDto> legacyDeptList, List<DeptDto> deptList) throws Exception {
		int updateCnt = 0;
		if(legacyDeptList == null || legacyDeptList.size() == 0 
			|| deptList == null || deptList.size() == 0)throw new Exception("업데이트할 부서 정보가 없습니다.");
		
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status = transactionManager_securepush.getTransaction(def);
		
		try{
			HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
			for(DeptDto _legacyDeptDto : legacyDeptList){
				String _legacyDeptCd = _legacyDeptDto.getDEPT_CD();
				if(_legacyDeptCd != null){
					DeptDto matchedDeptDto = getDeptByDeptCd(deptList, _legacyDeptCd);
					
					if(matchedDeptDto != null){
						if(!isSameContDeptDto(_legacyDeptDto, matchedDeptDto)){
							hrSyncDao.updateDept(_legacyDeptDto);
							updateCnt++;
						}
					}
				}
			}
			transactionManager_securepush.commit(status);
		}catch(Exception e){
			transactionManager_securepush.rollback(status);
			throw e;
		}
		
		return updateCnt;
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
	 * 두 개의 부서 정보의 내용이 같은지 확인.
	 * @param legacyDept
	 * @param dept
	 * @return
	 */
	private boolean isSameContDeptDto(DeptDto legacyDept, DeptDto dept) {
		boolean rt = false;
		
		if(StringUtils.equals(legacyDept.getDEPT_NM(), dept.getDEPT_NM())){
			
		}else return rt;
		
		if(StringUtils.equals(legacyDept.getUP_DEPT_CD(), dept.getUP_DEPT_CD())){
			
		}else return rt;

		if(StringUtils.equals(legacyDept.getFULL_NM(), dept.getFULL_NM())){
			
		}else return rt;
		
		rt = true;
		
		return rt;
	}
	
	/**
	 * 삭제된 부서 목록을 삭제한다.
	 * @param legacyDeptList
	 * @param deptList
	 * @return
	 * @throws Exception
	 */
	private int findDeletedAndDeleteDept(List<DeptDto> legacyDeptList, List<DeptDto> deptList) throws Exception {
		int deleteCnt = 0;
		
		List<DeptDto> delList = new ArrayList<DeptDto>(deptList);
		for(DeptDto _legacyDeptDto : legacyDeptList){
			String _legacyDeptCd = _legacyDeptDto.getDEPT_CD();
			if(_legacyDeptCd != null){
				DeptDto matchedDeptDto = getDeptByDeptCd(deptList, _legacyDeptCd);
				if(matchedDeptDto != null)
					delList.remove(matchedDeptDto);
			}
		}
		
		if(delList != null && delList.size() > 0){
			List<String> delDeptCdList = new ArrayList<String>();
			for(DeptDto deptToDel : delList){
				delDeptCdList.add(deptToDel.getDEPT_CD());
			}
			HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
			deleteCnt = hrSyncDao.deleteDepts(delDeptCdList);
		}
		
		return deleteCnt;
	}
	
	/**
	 * 두 개의 조직원 목록을 비교하여 변경되었으면 업데이트.
	 * @param legacyDeptList
	 * @param deptList
	 * @return
	 * @throws Exception
	 */
	private int compareAndUpdateEmp(List<EmpDto> legacyEmpList, List<EmpDto> empList) throws Exception {
		int updateCnt = 0;
		if(legacyEmpList == null || legacyEmpList.size() == 0 
			|| empList == null || empList.size() == 0)throw new Exception("업데이트할 조직원 정보가 없습니다.");
		
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status = transactionManager_securepush.getTransaction(def);
		
		try{
			HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
			for(EmpDto _legacyEmpDto : legacyEmpList){
				String _legacyEmpNo = _legacyEmpDto.getEMP_NO();
				if(_legacyEmpNo != null){
					EmpDto matchedEmpDto = getEmpByEmpNo(empList, _legacyEmpNo);
					
					if(matchedEmpDto != null){
						if(!isSameContEmpDto(_legacyEmpDto, matchedEmpDto)){
							// 레거시 인사 DB에 존재하는(퇴직하지 않은) 사용자를 비교하는 것이므로 
							// 재직여부를 'Y'로 세팅.
							_legacyEmpDto.setWORK_YN("Y");
							hrSyncDao.updateEmp(_legacyEmpDto);
							
							UserDto userDto = new UserDto();
							
							userDto.setEMP_NO(_legacyEmpDto.getEMP_NO());
							userDto.setUSER_NM(_legacyEmpDto.getUSER_NM());
							userDto.setDEPT_CD(_legacyEmpDto.getDEPT_CD());
							userDto.setDEPT_NM(_legacyEmpDto.getDEPT_NM());
							hrSyncDao.updateUser(userDto);
							updateCnt++;
						}
					}
				}
			}
			transactionManager_securepush.commit(status);
		}catch(Exception e){
			transactionManager_securepush.rollback(status);
			throw e;
		}
		
		return updateCnt;
	}
	
	/**
	 * 삭제된 조직원 목록을 퇴직 상태로 업데이트한다.
	 * @param legacyEmpList
	 * @param empList
	 * @return
	 * @throws Exception
	 */
	private int findDeletedAndDeleteEmp(List<EmpDto> legacyEmpList, List<EmpDto> empList) throws Exception {
		int deleteCnt = 0;
		
		List<EmpDto> delList = new ArrayList<EmpDto>(empList);
		for(EmpDto _legacyEmpDto : legacyEmpList){
			String _legacyEmpNo = _legacyEmpDto.getEMP_NO();
			if(_legacyEmpNo != null){
				EmpDto matchedEmpDto = getEmpByEmpNo(empList, _legacyEmpNo);
				if(matchedEmpDto != null)
					delList.remove(matchedEmpDto);
			}
		}
		
		if(delList != null && delList.size() > 0){
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			TransactionStatus status = transactionManager_securepush.getTransaction(def);
			
			try{
				HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
				for(EmpDto _delDto : delList){
					// 이미 퇴직 처리된 조직원은 다시 퇴직상태로 업데이트하지 않음.
					if(_delDto.getWORK_YN()!=null && "N".equals(_delDto.getWORK_YN()))
						continue;
					hrSyncDao.updateDelEmp(_delDto.getEMP_NO());
					deleteCnt++;
				}
				transactionManager_securepush.commit(status);
			}catch(Exception e){
				transactionManager_securepush.rollback(status);
				throw e;
			}
		}
		
		return deleteCnt;
	}
	
	/**
	 * 신규 추가된 조직원 정보를 등록하고 사용자 테이블에 가입처리한다.
	 * @param legacyEmpList
	 * @param empList
	 * @return
	 * @throws Exception
	 */
	private int findNewAndInsertEmp(List<EmpDto> legacyEmpList, List<EmpDto> empList) throws Exception {
		int insertCnt = 0;
		
		List<EmpDto> newList = new ArrayList<EmpDto>();
		List<UserDto> newUserList = new ArrayList<UserDto>();
		for(EmpDto _legacyEmpDto : legacyEmpList){
			String _legacyEmpNo = _legacyEmpDto.getEMP_NO();
			if(_legacyEmpNo != null){
				EmpDto matchedEmpDto = getEmpByEmpNo(empList, _legacyEmpNo);
				if(matchedEmpDto == null){
					newList.add(_legacyEmpDto);
					
					UserDto userDto = new UserDto();
					userDto.setUSER_ID(_legacyEmpDto.getUSER_ID());
					userDto.setEMP_NO(_legacyEmpDto.getEMP_NO());
					userDto.setUSER_NM(_legacyEmpDto.getUSER_NM());
					userDto.setDEPT_CD(_legacyEmpDto.getDEPT_CD());
					userDto.setDEPT_NM(_legacyEmpDto.getDEPT_NM());
					newUserList.add(userDto);
				}
			}
		}
		
		if(newList != null && newList.size() > 0 && newUserList != null && newUserList.size() > 0){
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			TransactionStatus status = transactionManager_securepush.getTransaction(def);
			
			try{
				HRSyncDao hrSyncDao = sqlSessionTemplate_securepush.getMapper(HRSyncDao.class);
				insertCnt = hrSyncDao.insertEmps(newList);
				hrSyncDao.insertUsers(newUserList);
				transactionManager_securepush.commit(status);
			}catch(Exception e){
				transactionManager_securepush.rollback(status);
				throw e;
			}
		}
		
		return insertCnt;
	}
	
	/**
	 * 조직원 목록에서 매칭되는 사원 번호의 조직원 정보를 가져옴.
	 * @param empList
	 * @param empNo
	 * @return
	 */
	private EmpDto getEmpByEmpNo(List<EmpDto> empList, String empNo) {
		EmpDto empDto = null;
		
		for(EmpDto _empDto : empList){
			if(_empDto.getEMP_NO() != null && empNo.equals(_empDto.getEMP_NO())){
				empDto = _empDto;
				break;
			}
		}
		
		return empDto;
	}
	
	/**
	 * 두 개의 조직원 정보의 내용이 같은지 확인.
	 * @param legacyEmp
	 * @param emp
	 * @return
	 */
	private boolean isSameContEmpDto(EmpDto legacyEmp, EmpDto emp) {
		boolean rt = false;
		
		if(!StringUtils.equals(legacyEmp.getPOS_CD(), emp.getPOS_CD()))
			return rt;
		if(!StringUtils.equals(legacyEmp.getRESP_CD(), emp.getRESP_CD()))
			return rt;
		if(!StringUtils.equals(legacyEmp.getDEPT_CD(), emp.getDEPT_CD()))
			return rt;
		if(!StringUtils.equals(legacyEmp.getDEPT_NM(), emp.getDEPT_NM()))
			return rt;
		if(!StringUtils.equals(legacyEmp.getUSER_NM(), emp.getUSER_NM()))
			return rt;
		if(!StringUtils.equals(legacyEmp.getTEL_NO(), emp.getTEL_NO()))
			return rt;
		if(!StringUtils.equals(legacyEmp.getCELL_NO(), emp.getCELL_NO()))
			return rt;
		if(!StringUtils.equals(legacyEmp.getEMAIL(), emp.getEMAIL()))
			return rt;
		if(!StringUtils.equals(legacyEmp.getPHOTO_PATH(), emp.getPHOTO_PATH()))
			return rt;
		
		rt = true;
		
		return rt;
	}
}
