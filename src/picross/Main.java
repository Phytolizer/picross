package picross;

import common.Background;
import common.Timer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("WeakerAccess")
class Main {
	public static Timer timer;
	public static Timer FPSCounter;
	public static Timer animator;
	public static Timer colorSwitcher;
	public static Timer promptTimer;
	public static Timer gambaTimer;
	public static GameWindow mainWindow;
	public static SettingsDocument prefs;
	public static LoginBox lb = new LoginBox();
	/**
	 * This is used to accommodate other file systems by changing to a forward slash if that is the preferred character.
	 */
	public static char slashCharacter = File.separatorChar;

	public static void main(String[] args) {
		try {
			extractFile("resources/bgusolver.jar");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			extractFile("resources/puzzleCreator.jar");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Using " + slashCharacter + " for slash character in this file system.");
		try {
			prefs = new SettingsDocument("prefs.txt");
		} catch (IOException e) {
			System.out.println("Could not load prefs.txt! Expect errors.");
			e.printStackTrace();
		}
		System.out.println(prefs.get("volume"));
		FPSCounter = new Timer();
		new Thread(FPSCounter).start();
		colorSwitcher = new Timer();
		new Thread(colorSwitcher).start();
		colorSwitcher.begin();
		Background.setTimer(colorSwitcher);
		mainWindow = new GameWindow();
		new Thread(mainWindow).start();
		timer = new Timer();
		new Thread(timer).start();
		animator = new Timer();
		new Thread(animator).start();
		promptTimer = new Timer();
		new Thread(promptTimer).start();
		gambaTimer = new Timer(false, 10);
		gambaTimer.setDelay(10);
		new Thread(gambaTimer).start();
		lb.setVisible(true);
	}

	public static void runSolver(String fileName) {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "." + slashCharacter + "resources" + slashCharacter + "bgusolver.jar", "-file", fileName);
		pb.directory(new File("."));
		try {
			Process p = pb.start();
			LogStreamReader lsr = new LogStreamReader(p.getInputStream());
			Thread thread = new Thread(lsr, "LogStreamReader");
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runCreator() {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "." + slashCharacter + "puzzleCreator.jar");
		pb.directory(new File("." + slashCharacter + "resources"));
		try {
			Process p = pb.start();
			LogStreamReader lsr = new LogStreamReader(p.getInputStream());
			Thread thread = new Thread(lsr, "PuzzleCreatorStream");
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getNumPuzzles() {
		int i = 0;
		File savesFolder = new File("." + slashCharacter + "saves");
		File[] saves = savesFolder.listFiles();
		if (saves != null) {
			for (File f : saves) {
				if (f.getName().contains(".nin")) {
					i++;
				}
			}
		}
		return i;
	}

	public static List<String> getPuzzleNames() {
		List<String> out = new ArrayList<>();
		File savesFolder = new File("." + slashCharacter + "saves");
		File[] saves = savesFolder.listFiles();
		if (saves != null) {
			for (File f : saves) {
				if (f.getName().contains(".nin")) {
					out.add(f.getName());
				}
			}
		}
		return out;
	}

	public static List<String> getPuzzleSizes() {
		List<String> out = new ArrayList<>();
		File savesFolder = new File("." + slashCharacter + "saves");
		File[] saves = savesFolder.listFiles();
		if (saves != null) {
			for (File f : saves) {
				if (f.getName().contains(".nin")) {
					try {
						Scanner s = new Scanner(f);
						if (s.hasNext()) {
							String size = s.next();
							int spaceLoc = size.indexOf(' ');
							out.add(size.substring(0, spaceLoc) + "x" + size.substring(spaceLoc + 1, size.length()));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return out;
	}

	public static List<TextDocument> getPuzzles() throws IOException {
		List<String> puzzleTitles = getPuzzleNames();
		List<TextDocument> out = new ArrayList<>();
		for (String puzzle : puzzleTitles) {
			out.add(new TextDocument(puzzle));
		}
		return out;
	}

	private static void extractFile(String name) throws IOException {
		File target = new File(name);
		if (target.exists()) {
			System.out.println(name + " already exists!");
			return;
		}

		FileOutputStream out = new FileOutputStream(target);
		ClassLoader cl = Main.class.getClassLoader();
		InputStream in = cl.getResourceAsStream(name);
		if (in == null) {
			throw new IOException("Could not find " + name + " within jar!");
		}

		byte[] buf = new byte[8 * 1024];
		int len;
		while ((len = in.read(buf)) != - 1) {
			out.write(buf, 0, len);
		}
		out.close();
		in.close();
	}
}
