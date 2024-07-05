package online.hanahime.signfaker.tools;

import static java.lang.String.*;

import android.annotation.SuppressLint;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.DecoderException;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Hex;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowDetFlag {

    public static String bytesToHex (byte[] bytes) {
        StringBuilder hexString = new StringBuilder ();
        for (byte b : bytes) {
            String hex = Integer.toHexString (0xFF & b);
            if (hex.length () == 1) {
                hexString.append ('0');
            }
            hexString.append (hex);
        }
        return hexString.toString ().toUpperCase (); // 转换为大写
    }

    public static byte[] hexToBytes (String hexString) {
        hexString = hexString.toUpperCase (); // 忽略大小写
        int length = hexString.length ();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit (hexString.charAt (i), 16) << 4)
                + Character.digit (hexString.charAt (i + 1), 16));
        }
        return data;
    }

    public static short byteArrayToInt (byte[] bytes) {
        return (short) (bytes[1] & 0xFF |
            (bytes[0] & 0xFF) << 8);
    }

    public static byte[] intToByteArray (int value) {
        return new byte[] {
            (byte) (value >> 8),
            (byte) value
        };
    }

    public static String[] DecodeFlags (String hex, String hexKeywordStart, String hexKeywordEnd, int length) {
        short start = byteArrayToInt (hexToBytes (hexKeywordStart));
        short end = byteArrayToInt (hexToBytes (hexKeywordEnd));
        String[] result = new String[2];
        for (short i = start; i <= end; i++) {
            String hexKeyword = bytesToHex (intToByteArray (i));
            result[1] = hexKeyword;
            result[0] = Decode (hex, hexKeyword, length);
            if (!result[0].startsWith ("Unable")) {
                break;
            }
        }
        return result;
    }

    public static String Decode (String hex, String hexKeyword, int length) {
        String LAST_DETECTION_FLAGS = "Unable to obtain detection flags!";
        try {
            @SuppressLint("DefaultLocale") Matcher matcher = Pattern.compile (format ("%s([a-zA-Z0-9]{%d})", hexKeyword, length), Pattern.CASE_INSENSITIVE).matcher (hex);
            if (!matcher.find ()) {
                return LAST_DETECTION_FLAGS;
            }
            byte[] detectionFlags = Hex.decodeHex (Objects.requireNonNull(matcher.group(1)).toCharArray ());
            byte pwd = detectionFlags[0];
            for (int i = 0; i < detectionFlags.length; i++) {
                detectionFlags[i] = (byte) (detectionFlags[i] ^ pwd);
                LAST_DETECTION_FLAGS = Hex.encodeHexString (detectionFlags);
            }
        } catch (DecoderException e) {
            LAST_DETECTION_FLAGS = valueOf (e);
        }
        return LAST_DETECTION_FLAGS;
    }

/*
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
    }*/
}