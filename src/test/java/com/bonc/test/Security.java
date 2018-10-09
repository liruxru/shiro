package com.bonc.test;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.junit.Test;

public class Security {
	@Test
	public void test(){
		Md5Hash m=new Md5Hash("admin#sojson");
		System.out.println(m.toHex());
		
		
	}

}
