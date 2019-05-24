package kr.msp.admin.securepush.schedule.dto;

/**
 * 시큐어 푸쉬 사용자 테이블 데이터 클래스.
 */
public class UserDto {

	private String USER_ID;
	private String EMP_NO;
	private String USER_NM;
	private String DEPT_CD;
	private String DEPT_NM;
	private String ADMIN_CD;
	private String USE_YN;
	private String AGREE_YN;
	private String REG_DT;
	private String MOD_DT;
	public String getUSER_ID() {
		return USER_ID;
	}
	public void setUSER_ID(String uSER_ID) {
		USER_ID = uSER_ID;
	}
	public String getEMP_NO() {
		return EMP_NO;
	}
	public void setEMP_NO(String eMP_NO) {
		EMP_NO = eMP_NO;
	}
	public String getUSER_NM() {
		return USER_NM;
	}
	public void setUSER_NM(String uSER_NM) {
		USER_NM = uSER_NM;
	}
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
	public String getADMIN_CD() {
		return ADMIN_CD;
	}
	public void setADMIN_CD(String aDMIN_CD) {
		ADMIN_CD = aDMIN_CD;
	}
	public String getUSE_YN() {
		return USE_YN;
	}
	public void setUSE_YN(String uSE_YN) {
		USE_YN = uSE_YN;
	}
	public String getAGREE_YN() {
		return AGREE_YN;
	}
	public void setAGREE_YN(String aGREE_YN) {
		AGREE_YN = aGREE_YN;
	}
	public String getREG_DT() {
		return REG_DT;
	}
	public void setREG_DT(String rEG_DT) {
		REG_DT = rEG_DT;
	}
	public String getMOD_DT() {
		return MOD_DT;
	}
	public void setMOD_DT(String mOD_DT) {
		MOD_DT = mOD_DT;
	}
	
	
}
