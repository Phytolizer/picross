package picross;

import java.util.List;

@SuppressWarnings("WeakerAccess")
class Strings {
	public static int findLineWith(List<String> array, String search, boolean caseSensitive) {
		for (int i = 0; i < array.size(); i++) {
			if (caseSensitive) {
				if (array.get(i).contains(search)) {
					return i;
				}
			} else if (array.get(i).toLowerCase().contains(search.toLowerCase())) {
				return i;
			}
		}
		return - 1;
	}
}
