package kr.msp.admin.securepush.schedule.dto;

/**
 * 시큐어푸쉬 인사정보 직책 테이블 데이터 구조체.
 */
public class RespDto {
	
	private String RESP_CD;
	private String RESP_NM;
	
	public String getRESP_CD() {
		return RESP_CD;
	}
	public void setRESP_CD(String rESP_CD) {
		RESP_CD = rESP_CD;
	}
	public String getRESP_NM() {
		return RESP_NM;
	}
	public void setRESP_NM(String rESP_NM) {
		RESP_NM = rESP_NM;
	}

}
