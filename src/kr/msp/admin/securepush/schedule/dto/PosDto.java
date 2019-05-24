package kr.msp.admin.securepush.schedule.dto;

/**
 * 시큐어푸쉬 인사정보 직급 테이블 데이터 구조체.
 */
public class PosDto {

	private String POS_CD;
	private String POS_NM;
	
	public String getPOS_CD() {
		return POS_CD;
	}
	public void setPOS_CD(String pOS_CD) {
		POS_CD = pOS_CD;
	}
	public String getPOS_NM() {
		return POS_NM;
	}
	public void setPOS_NM(String pOS_NM) {
		POS_NM = pOS_NM;
	}
	
}
