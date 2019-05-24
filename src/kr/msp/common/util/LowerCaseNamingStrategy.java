package kr.msp.common.util;

import org.codehaus.jackson.map.PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy;

public class LowerCaseNamingStrategy extends LowerCaseWithUnderscoresStrategy {
    @Override
    public String translate(String arg0) {
        return arg0.toUpperCase();
    }
}
