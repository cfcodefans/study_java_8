package cf.study.java.algothrims.search;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class KMPTests {
	public static int[] next(String pattern) {
		char[] p = pattern.toCharArray();
		int[] next = new int[p.length];

		next[0] = -1;
//		System.out.println("i\tp[i]\tj\tp[j]");
		for (int i = 0, j = -1, len = p.length - 1; i < len; ) {
			if (j == -1 || p[i] == p[j]) {
				i++;
				j++;
				if (p[i] != p[j]) {
					next[i] = j;
				} else {
					next[i] = next[j];
				}
			} else {
				j = next[j];
			}
//			System.out.printf("__________________________\n%d\t%c\t%d\t\n", i, p[i], j);
		}

		return next;
	}

	public static int kmpSearch(String str, String pattern) {
		int[] next = next(pattern);
		char[] s = str.toCharArray();
		char[] p = pattern.toCharArray();

		int i = 0, j = 0;
		for (int sl = s.length, pl = p.length; i < sl && j < pl; ) {
			if (j == -1 || s[i] == p[j]) {
				i++;
				j++;
			} else {
				j = next[j];
			}
		}

		return (j < p.length) ? -1 : i - p.length;
	}

	public static void printNext(String p) {
		System.out.println();
		Arrays.stream(ArrayUtils.toObject(p.toCharArray())).map(ch -> ch + "\t").forEach(System.out::print);
		System.out.println();
		Arrays.stream(ArrayUtils.toObject(next(p))).map(next -> next + "\t").forEach(System.out::print);
		System.out.println();
	}

	@Test
	public void testNext() {
		printNext("012345");
		printNext("012225");

		printNext("abababab");
		printNext("aaaaaa");
		printNext("abcdacde");
		printNext("abcdabce");
		printNext("abcdabcd");
		printNext("abcdef");
		printNext("abcabf");
		printNext("aaabbb");
	}

	@Test
	public void testSearch() {
		String str = "aljflasjflasjalfjasldfjlekjlrjociuvoiufa";
		String pattern = str.substring(12, 18);

		Assert.assertEquals(str.indexOf(pattern), kmpSearch(str, pattern));
	}

	public String reverse(String a) {
		String re = a;
		if (re.length() > 1) {
			re = a.charAt(a.length() - 1) + reverse(a.substring(0, a.length() -1));
		}
		return re;
	}

	@Test
	public void test() {
		System.out.println(reverse("1233344"));


	}

	public boolean[] toggleDoors(int n) {
		boolean[] res = new boolean[n];
		for (int i = 0; i < n; i++) {
			res[i] = true;
		}
		for (int i = 1; i <= n; i++) {
			for (int j = i; j <= n; j = j + i) {
				res[j-1] = !res[j-1];
			}
		}
		return res;
 	}

 	@Test
	public void testT() {
	    System.out.println(Arrays.toString(toggleDoors(9)));
    }
}