package kr.msp.admin.securepush.schedule.mapper;

import java.util.List;
import java.util.Map;

import kr.msp.admin.securepush.schedule.dto.DeptDto;
import kr.msp.admin.securepush.schedule.dto.EmpDto;
import kr.msp.admin.securepush.schedule.dto.PosDto;
import kr.msp.admin.securepush.schedule.dto.RespDto;
import kr.msp.admin.securepush.schedule.dto.UserDto;

/**
 * 인사 DB 동기화 관련 DAO 클래스.
 *
 */
public interface HRSyncDao {

	/**
	 * 시큐어푸쉬 인사 테이블의 부서 정보 목록 조회.
	 * @param paramMap
	 * @return
	 */
	public List<DeptDto> selectDeptList(Map<String, Object> paramMap);
	
	/**
	 * 시큐어푸쉬 인사 테이블의 사원 정보 목록 조회.
	 * @param paramMap
	 * @return
	 */
	public List<EmpDto> selectEmpList(Map<String, Object> paramMap);
	
	/**
	 * 시큐어 푸쉬 부서 테이블 정보 업데이트.
	 * @param param
	 * @return
	 */
	public int updateDept(DeptDto param);
	
	/**
	 * 시큐어 푸쉬 조직원 테이블 정보 업데이트.
	 * @param param
	 * @return
	 */
	public int updateEmp(EmpDto param);
	
	/**
	 * 시큐어 푸쉬 조직원 테이블 퇴직(삭제) 처리.
	 * @param empNo
	 * @return
	 */
	public int updateDelEmp(String empNo);
	
	/**
	 * 시큐어 푸쉬 가입된 사용자 테이블 업데이트 처리.
	 * @param param
	 * @return
	 */
	public int updateUser(UserDto param);
	
	/**
	 * 시큐어 푸쉬 부서 목록 등록.
	 * @param deptList
	 * @return
	 */
	public int insertDepts(List<DeptDto> deptList);
	
	/**
	 * 시큐어 푸쉬 조직원 목록 등록.
	 * @param empList
	 * @return
	 */
	public int insertEmps(List<EmpDto> empList);
	
	/**
	 * 시큐어 푸쉬 사용자 목록 등록.
	 * @param userList
	 * @return
	 */
	public int insertUsers(List<UserDto> userList);
	
	/**
	 * 시큐어 푸쉬 직급 목록 등록.
	 * @param posList
	 * @return
	 */
	public int insertPositions(List<PosDto> posList);
	
	/**
	 * 시큐어 푸쉬 직책 목록 등록.
	 * @param respList
	 * @return
	 */
	public int insertResps(List<RespDto> respList);
	
	/**
	 * 시큐어 푸쉬 부서 목록 삭제.
	 * @param deptCdList
	 * @return
	 */
	public int deleteDepts(List<String> deptCdList);
	
	/**
	 * 시큐어 푸쉬 직급 목록 삭제.
	 * @return
	 */
	public int deletePositions();
	
	/**
	 * 시큐어 푸쉬 직책 목록 삭제.
	 * @return
	 */
	public int deleteResps();
}
