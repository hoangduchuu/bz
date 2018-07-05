package com.ping.android.ultility;

import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.configs.Constant;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by tuanluong on 12/12/17.
 */
public class CommonMethodTest extends TestCase {
    public void testCapitalFirstLetter() throws Exception {
        String before = "tuan";
        String result = CommonMethod.capitalFirstLetter(before);
        Assert.assertEquals(result, "Tuan");

        String before1 = "tUan";
        String result1 = CommonMethod.capitalFirstLetter(before1);
        Assert.assertEquals(result1, "Tuan");

        String before2 = "tUAN";
        String result2 = CommonMethod.capitalFirstLetter(before2);
        Assert.assertEquals(result2, "Tuan");
    }

    public void testCapitalFirstLetters() {
        String sample = "luong ANH TUan";
        String result = CommonMethod.capitalFirstLetters(sample);
        Assert.assertEquals(result, "Luong Anh Tuan");
        Assert.assertEquals(CommonMethod.capitalFirstLetters("Tien dung"), "Tien Dung");
    }

    public void testValidPassword() {
        Assert.assertFalse(CommonMethod.isValidPassword("123456"));
        Assert.assertTrue(CommonMethod.isValidPassword("12345678"));
        Assert.assertTrue(CommonMethod.isValidPassword("123TuanLuong"));
        Assert.assertFalse(CommonMethod.isValidPassword("123TuanLuong123"));
    }

    public void testDaysFromTimestamp() {
        long timestamp1 = 1523336400000L; // 2018/04/10 12:00:00
        long result1 = (long) (timestamp1 / Constant.MILLISECOND_PER_DAY);
        long timestamp2 = 1523358000000L;
        long result2 = (long) (timestamp2 / Constant.MILLISECOND_PER_DAY);
        Assert.assertTrue(result1 == result2);

        long timestamp3 = 1522753200000L;
        long result3 = (long) (timestamp3 / Constant.MILLISECOND_PER_DAY);
        Assert.assertFalse(result1 == result3);

        long timestamp4 = 1522645200000L; // 02/04/2018
        long result4 = (long) (timestamp4 / Constant.MILLISECOND_PER_DAY);
        Assert.assertFalse(result3 == result4);
    }

    public void testVersionValid() {
        String currentVersion = "3.0.0";
        String expectVersion = "2.3.3";
        Assert.assertTrue(CommonMethod.checkVersionValid(currentVersion, expectVersion));
        Assert.assertFalse(CommonMethod.checkVersionValid("2.3.3", "3.0.0"));
    }
}