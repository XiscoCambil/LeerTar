import org.junit.Test;

import java.math.BigInteger;
import java.security.MessageDigest;

import static org.junit.Assert.*;

/**
 * Created by pnegre on 19/05/16.
 */
public class TarTest {
    @Test
    public void test1() throws Exception {
        Tar tar = new Tar("/tmp/archive2.tar");
        tar.expand();

        assertArrayEquals(new String[]{
                "aec8c99e699eac8ecc7d71986c931587.jpg",
                "linux-kernel.png",
                "linux.png",
                "neo tux.png",
                "tux-goku.png",
                "xanderrun-tux-construction-8454.png"
        }, tar.list());

        byte[] data = tar.getBytes("noexisteix");
        assertTrue(data == null);

        data = tar.getBytes("aec8c99e699eac8ecc7d71986c931587.jpg");
        assertEquals("F2898EF3E141667F16D9A078758A5DC4", md5(data));

        data = tar.getBytes("linux-kernel.png");
        assertEquals("8E09AAD1748D947EFE588D95C1441213", md5(data));

        data = tar.getBytes("linux.png");
        assertEquals("D09ADE6D39256A31EDD14BE9F45713EA", md5(data));

        data = tar.getBytes("neo tux.png");
        assertEquals("C77C44ED79BDBE64B65CF5E9BE08E525", md5(data));

        data = tar.getBytes("tux-goku.png");
        assertEquals("C8CA1D1773E3C16003BE5A4C61624EF4", md5(data));

        data = tar.getBytes("xanderrun-tux-construction-8454.png");
        assertEquals("AE698319B876FF2CA86608A1C22ACAFD", md5(data));

    }

    private String md5(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data);
        return toHex(md.digest());
    }

    private static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}