package online.Hanahime.signfaker.tools;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.DecoderException;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Hex;

public class ShowDetFlag {

    public static String getFlags(byte[] bArr){
        String LAST_DETECTION_FLAGS = "Unable to obtain detection flags!";
        String pendingHex = Hex.encodeHexString(bArr);
        int pendingPosition = pendingHex.indexOf("010d");
        if(pendingPosition != -1 && pendingHex.length() > pendingPosition + 24) {
            byte[] detectionFlags;
            try {
                detectionFlags = Hex.decodeHex((pendingHex.substring((pendingPosition + 4), (pendingPosition + 24))).toCharArray());
                byte pwd = detectionFlags[0];
                for (int i = 0; i < detectionFlags.length; i++) {
                    detectionFlags[i] = (byte) (detectionFlags[i] ^ pwd);
                    LAST_DETECTION_FLAGS = Hex.encodeHexString(detectionFlags);
                }
            } catch (DecoderException e) {
                LAST_DETECTION_FLAGS = String.valueOf(e);
            }
            return LAST_DETECTION_FLAGS;
        }
        return LAST_DETECTION_FLAGS;
    }
}