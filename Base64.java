import java.io.IOException;
import java.nio.ByteBuffer;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author Ben
 */
public class Base64 {

    /**
     * method used for testing this class
     *
     * @param args
     */
    public static void main(String[] args) {
        // testing the encoder decoder
        int[] intArray;
        intArray = new int[10];
        intArray[0] = 100;
        byte[] byteArray;

        byteArray = new byte[32];
        byteArray[0] = (byte) 1.90;
        byteArray[1] = 103;
        byteArray[2] = 100;
        byteArray[3] = 00;

        String en = encode(byteArray);
        System.out.println("encodedBytes " + en);
        byte[] de = decode(en);
        for (int i = 0; i < de.length; i++) {
            System.out.println("decodedBytes " + de[i]);
        }
        System.out.println("encodedBytes " + encode(de));
    }

    /**
     * method to convert int array to byte array
     *
     * @param data is the int array
     * @return byte array
     */
    public static byte[] intArrToByteArr(int[] data) {
        int srcLength = data.length;
        byte[] dst = new byte[srcLength << 2];

        for (int i = 0; i < srcLength; i++) {
            int x = data[i];
            int j = i << 2;
            dst[j++] = (byte) ((x >>> 0) & 0xff);
            dst[j++] = (byte) ((x >>> 8) & 0xff);
            dst[j++] = (byte) ((x >>> 16) & 0xff);
            dst[j++] = (byte) ((x >>> 24) & 0xff);
        }
        return dst;
    }

    /**
     * method to encode a byte array into a Base 64 string
     *
     * @param s is the byte array
     * @return base 64 string
     */
    public static String encode(byte[] s) {
        BASE64Encoder encoder = new BASE64Encoder();
        String encodedBytes = encoder.encodeBuffer(s);
        return encodedBytes;
    }

    /**
     * method to decode a Base 64 string into a byte array
     *
     * @param s is the base 64 string
     * @return byte array
     */
    public static byte[] decode(String s) {
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] decodedBytes = decoder.decodeBuffer(s);
            return decodedBytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
