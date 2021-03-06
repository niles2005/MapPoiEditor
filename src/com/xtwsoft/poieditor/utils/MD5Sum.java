package com.xtwsoft.poieditor.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;

public class MD5Sum {
	private static MessageDigest md = null;

	/**
	 * Method: md5Sum Purpose: calculate the MD5 in a way compatible with how
	 * the scour.net protocol encodes its passwords (incidentally, it also
	 * outputs a string identical to the md5sum unix command).
	 * 
	 * @return the MD5 checksum
	 */
	public static String md5Sum() {
		byte[] digest = md.digest();
		StringBuffer hexString = new StringBuffer();

		for (int i = 0; i < digest.length; i++) {
			hexString.append(hexDigit(digest[i]));
		}

		return hexString.toString();
	}

	/**
	 * Method: hexDigit Purpose: convert a hex digit to a String, used by
	 * md5Sum.
	 * 
	 * @param x
	 *            the digit to translate
	 * @return the hex code for the digit
	 */
	static private String hexDigit(byte x) {
		StringBuffer sb = new StringBuffer();
		char c;

		// First nibble
		c = (char) ((x >> 4) & 0xf);
		if (c > 9) {
			c = (char) ((c - 10) + 'a');
		} else {
			c = (char) (c + '0');
		}

		sb.append(c);

		// Second nibble
		c = (char) (x & 0xf);
		if (c > 9) {
			c = (char) ((c - 10) + 'a');
		} else {
			c = (char) (c + '0');
		}

		sb.append(c);
		return sb.toString();
	}

	/**
	 * Method: getFileMD5Sum Purpose: get the MD5 sum of a file. Scour exchange
	 * only counts the first SCOUR_MD5_BYTE_LIMIT bytes of a file for
	 * caclulating checksums (probably for efficiency or better comaprison
	 * counts against unfinished downloads).
	 * 
	 * @param f
	 *            the file to read
	 * @return the MD5 sum string
	 * @throws IOException
	 *             on IO error
	 */
	public static String getFileMD5Sum(File f) {
		try {
			String sum = null;
			FileInputStream in = new FileInputStream(f.getAbsolutePath());

			byte[] b = new byte[8192];
			int num = 0;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if (md == null)
				md = MessageDigest.getInstance("MD5");

			md.reset();
			while ((num = in.read(b)) != -1) {
				md.update(b, 0, num);
			}

			if (sum == null)
				sum = md5Sum();

			in.close();
			out.close();

			return sum;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getByteArrayMD5Sum(byte[] byteArray) {
		try {
			if (md == null)
				md = MessageDigest.getInstance("MD5");

			md.reset();
			md.update(byteArray, 0, byteArray.length);

			return md5Sum();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getByteArrayMD5Sum(byte[] byteArray, int len) {
		try {
			if (md == null)
				md = MessageDigest.getInstance("MD5");

			md.reset();
			md.update(byteArray, 0, len);

			return md5Sum();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 取32位MD5的中间8-24位
	public static String encode16MD5(String str) {
		String text = encode32MD5(str);
		if (text != null) {
			return text.substring(8, 24);
		}
		return null;
	}

	public static String encode32MD5(String str) {
		try {
			byte[] byteArray = ("" + str).getBytes();

			if (md == null)
				md = MessageDigest.getInstance("MD5");

			md.reset();
			md.update(byteArray, 0, byteArray.length);

			return md5Sum();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 产生64位的Md5 sum，长度不定
	public static final String encode64MD5(String str) {
		try {
			Base64.Encoder encoder = Base64.getEncoder();
			final byte[] textByte = str.getBytes("UTF-8");
			String encodedText = encoder.encodeToString(textByte);

			return encodedText;
		} catch (Exception ex) {
			return null;
		}
	}

	public static final String decode64MD5(String str) {
		try {
			Base64.Decoder decoder = Base64.getDecoder();
			return new String(decoder.decode(str), "UTF-8");
		} catch (Exception ex) {
			return null;
		}
	}

	public static void main(String[] args) {
		String ss = MD5Sum
				.encode64MD5("Quis ad qui ut mollit voluptate laborum.Quis occaecat proident labore deserunt irure nostrud eu. Duis labore eiusmod aliqua irure ad nisi deserunt velit cupidatat cupidatat. In minim enim consequat Lorem ut fugiat minim proident reprehenderit cillum cupidatat anim officia sunt. Est irure sit incididunt culpa nisi dolor. Dolor quis ipsum quis deserunt exercitation ut. Anim ipsum dolor velit consectetur.");
		System.err.println(ss);
		ss = "UXVpcyBhZCBxdWkgdXQgbW9sbGl0IHZvbHVwdGF0ZSBsYWJvcnVtlF1aXMgb2NjYWVjYXQgcHJvaWRlbnQgbGFib3JlIGRlc2VydW50IGlydXJlIG5vc3RydWQgZXUuIER1aXMgbGFib3JlIGVpdXNtb2QgYWxpcXVhIGlydXJlIGFkIG5pc2kgZGVzZXJ1bQgdmVsaXQgY3VwaWRhdGF0IGN1cGlkYXRhdC4gSW4gbWluaW0gZW5pbSBjb25zZXF1YXQgTG9yZW0gdXQgZnVnaWF0IG1pbmltIHByb2lkZW50IHJlcHJlaGVuZGVyaXQgY2lsbHVtIGN1cGlkYXRhdCBhbmltIG9mZmljaWEgc3VudC4gRXN0IGlydXJlIHNpdCBpbmNpZGlkdW50IGN1bHBhIG5pc2kgZG9sb3IuIERvbG9yIHF1aXMgaXBzdW0gcXVpcyBkZXNlcnVudCBleGVyY2l0YXRpb24gdXQuIEFuaW0gaXBzdW0gZG9sb3IgdmVsaXQgY29uc2VjdGV0dXIu==";
		ss = MD5Sum.decode64MD5(ss);
		System.err.println(ss);

		ss = MD5Sum.encode32MD5("abc");
		System.err.println(ss);

		ss = MD5Sum.encode16MD5("abc");
		System.err.println(ss);

	}
}