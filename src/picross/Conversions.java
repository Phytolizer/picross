package picross;

class Conversions {
	public static int ctoi(char c) {//directly converts one character into a single-digit integer
		if ((int) c >= 48 && (int) c <= 57)
			return (int) c - 48;
		else if (c != '-')
			return - 1;
		else
			return - 2;
	}

	public static int stoi(String s) {//combines ctoi and pow to return an integer conversion of s
		return Integer.parseInt(s);
	}

	public static int pow(int n, int p) {//returns n^p
		return (int) Math.pow(n, p);
	}

	public static int intLen(int n) {
		if (n == 0)
			return 1;
		int i = 0;
		while (n != 0) {
			i++;
			n /= 10;
		}
		return i;
	}

	public static String itos(int n) {
		return Integer.toString(n);
	}
}
