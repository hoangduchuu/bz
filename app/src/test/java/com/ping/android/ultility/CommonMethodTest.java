package com.ping.android.ultility;

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
        Assert.assertEquals(result1, "TUan");
    }
}