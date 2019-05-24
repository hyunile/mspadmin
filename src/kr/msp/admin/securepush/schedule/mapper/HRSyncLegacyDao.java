package kr.msp.admin.securepush.schedule.mapper;

import java.util.List;
import java.util.Map;

import kr.msp.admin.securepush.schedule.dto.DeptDto;
import kr.msp.admin.securepush.schedule.dto.EmpDto;

/**
 * 레거시 인사 DB 관련 DAO 클래스.
 *
 */
public interface HRSyncLegacyDao {

	/**
	 * 레거시 인사 DB의 부서 정보 목록 조회.
	 * @param paramMap
	 * @return
	 */
	public List<DeptDto> selectDeptList(Map<String, Object> paramMap);
	
	/**
	 * 레거시 인사 DB의 사원 정보 목록 조회.
	 * @param paramMap
	 * @return
	 */
	public List<EmpDto> selectEmpList(Map<String, Object> paramMap);
}
