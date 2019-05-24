package kr.msp.admin.securepush.schedule.dto;

/**
 * 인사 DB 부서 정보 데이터 클래스.
 */
public class DeptDto {

	private String DEPT_CD;
	private String DEPT_NM;
	private String FULL_NM;
	private String UP_DEPT_CD;
	
	public String getDEPT_CD() {
		return DEPT_CD;
	}
	public void setDEPT_CD(String dEPT_CD) {
		DEPT_CD = dEPT_CD;
	}
	public String getDEPT_NM() {
		return DEPT_NM;
	}
	public void setDEPT_NM(String dEPT_NM) {
		DEPT_NM = dEPT_NM;
	}
	public String getFULL_NM() {
		return FULL_NM;
	}
	public void setFULL_NM(String fULL_NM) {
		FULL_NM = fULL_NM;
	}
	public String getUP_DEPT_CD() {
		return UP_DEPT_CD;
	}
	public void setUP_DEPT_CD(String uP_DEPT_CD) {
		UP_DEPT_CD = uP_DEPT_CD;
	}
	
	
}
