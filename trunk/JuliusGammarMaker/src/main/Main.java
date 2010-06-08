package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

public class Main {

	static String acModelFile = "";
	static String file = "";
	static String saveFolder = "";
	static String fileName = "";

	static String log = "";

	static HashMap<String, Vector<String>> voca;

	static String chooseGrammarEdukia = "";
	static String chooseVocaEdukia = "";

	static Vector<String> vocaDone = new Vector<String>();
	static Vector<String> grammarDone = new Vector<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 3) {
			System.out
					.println("Missing parameters: specify the foldel with the acoustic model files, the file with text, the name for the grammar files and the folder for saving the grammar files");
			System.exit(1);
		}

		acModelFile = args[0];
		file = args[1];
		fileName = args[2];
		saveFolder = args[3];

		Dictionary h = new Dictionary(acModelFile + File.separator + "dict");
		voca = new HashMap<String, Vector<String>>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			String line;
			int lerroKont = 0;
			while ((line = br.readLine()) != null) {
				String lerroBerri = line.replaceAll(
						"[!\\\"#%&()\\*+,\\./:;<=>?@[\\\\]_`{|}~]|^-", "");
				lerroBerri = lerroBerri.replaceAll(" +- ?", " ");
				lerroBerri = lerroBerri.replaceAll("\\$", " DOLLAR");
				lerroBerri = lerroBerri.replaceAll("[ \\t\\r\\n\\v\\f]+", " ");
				lerroBerri = lerroBerri.trim();

				if (lerroBerri != null && lerroBerri.length() != 0) {
					lerroKont++;
					sortuEsaldikaGrammarVoca(lerroBerri, h, lerroKont);
					sortuEsaldikaOsoaGrammarVoca(lerroBerri, h, lerroKont);
					sortuHitzKopurkaGrammar(lerroBerri, h, lerroKont);
				}
			}

			sortuOsoaGrammar();
			sortuOsoaVoca();

			sortuChooseGrammarVoca();

			sortuDfaJconf();

			System.out.println(log);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void sortuEsaldikaGrammarVoca(String esaldia, Dictionary h,
			int lerroKont) {
		
		String name = "tipo_0";

		String esaldiPath = saveFolder + File.separator + lerroKont + "-"
				+ esaldia.replace(' ', '_') + File.separator + name;

		File file = new File(esaldiPath);
		if (!file.exists())
			file.mkdirs();

		String grammarHeader = "S : NS_B SENT NS_E\n";
		String grammarEdukia = "SENT: "
				+ esaldia.toUpperCase().replace("'", "").replace("-", "")
				+ "\n";
		if (!grammarDone.contains(esaldia)) {
			chooseGrammarEdukia += grammarEdukia;
			grammarDone.addElement(esaldia);
		}
		grammarEdukia = grammarHeader + grammarEdukia;

		String vocaHeader = "% NS_B\n<s>\tsil\n\n% NS_E\n</s>\tsil\n\n";

		String vocaEdukia = "";
		Vector<String> v = new Vector<String>(Arrays.asList(esaldia.split(" ")));
		Vector<String> done = new Vector<String>();
		for (String s : v) {
			s = s.toUpperCase();
			if (!done.contains(s)) {
				String voca = "% " + s.replace("'", "").replace("-", "") + "\n";
				if (h.containsKey(s)) {
					Vector<String> fon = h.get(s);
					if (!Main.voca.containsKey(s))
						Main.voca.put(s, fon);
					voca += s + "\t";
					for (String f : fon)
						if (!f.equals("sp"))
							voca += " " + f;
					voca += "\n\n";
				} else
					log += "lerroa:" + lerroKont + "; "
							+ "sortuEsaldikaGrammarVoca: " + s
							+ " hitza ez da aurkitu.\n";
				done.addElement(s);
				if (!vocaDone.contains(s)) {
					chooseVocaEdukia += voca + "\n";
					vocaDone.addElement(s);
				}
				vocaEdukia += voca;
			}
		}

		vocaEdukia = vocaHeader + vocaEdukia;

		try {
			// Create file
			FileWriter fstream = new FileWriter(esaldiPath + File.separator
					+ fileName + ".grammar");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(grammarEdukia);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		try {
			// Create file
			FileWriter fstream = new FileWriter(esaldiPath + File.separator
					+ fileName + ".voca");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(vocaEdukia);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static void sortuEsaldikaOsoaGrammarVoca(String esaldia,
			Dictionary h, int lerroKont) {

		String name = "tipo_a";
		
		String esaldiPath = saveFolder + File.separator + lerroKont + "-"
				+ esaldia.replace(' ', '_') + File.separator + name;

		File file = new File(esaldiPath);
		if (!file.exists())
			file.mkdirs();

		String grammarEdukia = "S : NS_B ";
		int hitzKop = esaldia.toUpperCase().split(" ").length;
		for (int i = 0; i < hitzKop; i++)
			grammarEdukia += "WORD ";
		grammarEdukia += "NS_E\n";

		String vocaEdukia = "% NS_B\n<s>\tsil\n\n% NS_E\n</s>\tsil\n\n% WORD\n";

		Vector<String> v = new Vector<String>(Arrays.asList(esaldia.split(" ")));
		for (String s : v) {
			s = s.toUpperCase();
			if (h.containsKey(s)) {
				Vector<String> fon = h.get(s);
				if (!voca.containsKey(s))
					voca.put(s, fon);
				vocaEdukia += s + "\t";
				for (String f : fon)
					if (!f.equals("sp"))
						vocaEdukia += " " + f;
				vocaEdukia += "\n";
			} else
				log += "lerroa:" + lerroKont + "; "
						+ "sortuEsaldikaOsoaGrammarVoca: " + s
						+ " hitza ez da aurkitu.\n";
		}

		try {
			// Create file
			FileWriter fstream = new FileWriter(esaldiPath + File.separator
					+ fileName + ".grammar");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(grammarEdukia);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		try {
			// Create file
			FileWriter fstream = new FileWriter(esaldiPath + File.separator
					+ fileName + ".voca");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(vocaEdukia);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static void sortuHitzKopurkaGrammar(String esaldia, Dictionary h,
			int lerroKont) {

		String name = "tipo_b";
		
		String esaldiPath = saveFolder + File.separator + lerroKont + "-"
				+ esaldia.replace(' ', '_') + File.separator + name;

		File file = new File(esaldiPath);
		if (!file.exists())
			file.mkdirs();
		String grammarEdukia = "S : NS_B ";
		int hitzKop = esaldia.toUpperCase().split(" ").length;
		for (int i = 0; i < hitzKop; i++)
			grammarEdukia += "WORD ";
		grammarEdukia += "NS_E\n";

		try {
			// Create file
			FileWriter fstream = new FileWriter(esaldiPath + File.separator
					+ fileName + ".grammar");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(grammarEdukia);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			log += "makeHitzKopurkaGrammar: " + e.getMessage() + "\n";
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static void sortuOsoaGrammar() {
		
		String name = "tipo_c";
		
		String grammarEdukia = "S : NS_B WORD_LOOP NS_E\nWORD_LOOP: WORD_LOOP WORD\nWORD_LOOP: WORD\n";

		try {
			File dir = new File(saveFolder);
			String[] azpiDir = dir.list();
			for (int i = 0; i < azpiDir.length; i++) {
				File f = new File(saveFolder + File.separator + azpiDir[i]);
				if (f.isDirectory()) {
					File file = new File(f.getPath() + File.separator + name);
					if (!file.exists())
						file.mkdirs();
					FileWriter fstream = new FileWriter(f.getPath()
							+ File.separator + name + File.separator
							+ fileName + ".grammar");
					BufferedWriter out = new BufferedWriter(fstream);
					out.write(grammarEdukia);
					out.close();
				} else
					System.out.println(f.getPath() + " ez da dir");
			}
		} catch (IOException e) {
			log += "sortuOsoaGrammar: " + e.getMessage() + "\n";
			e.printStackTrace();
		}
	}

	private static void sortuOsoaVoca() {
		String vocaEdukia = "% NS_B\n<s>\tsil\n\n% NS_E\n</s>\tsil\n\n% WORD\n";

		Set<String> keys = voca.keySet();
		for (String key : keys) {
			vocaEdukia += key + "\t\t";
			Vector<String> v = voca.get(key);
			for (String f : v)
				if (!f.equals("sp"))
					vocaEdukia += " " + f;
			vocaEdukia += "\n";
		}

		try {
			File dir = new File(saveFolder);
			String[] azpiDir = dir.list();
			for (int i = 0; i < azpiDir.length; i++) {
				File f = new File(saveFolder + File.separator + azpiDir[i]);
				if (f.isDirectory()) {
					File file = new File(f.getPath() + File.separator + "tipo_c");
					if (!file.exists())
						file.mkdirs();
					File file2 = new File(f.getPath() + File.separator
							+ "tipo_b");
					if (!file2.exists())
						file2.mkdirs();
					FileWriter fstream = new FileWriter(f.getPath()
							+ File.separator + "tipo_c" + File.separator
							+ fileName + ".voca");
					BufferedWriter out = new BufferedWriter(fstream);
					out.write(vocaEdukia);
					out.close();
					FileWriter fstream2 = new FileWriter(f.getPath()
							+ File.separator + "tipo_b" + File.separator
							+ fileName + ".voca");
					BufferedWriter out2 = new BufferedWriter(fstream2);
					out2.write(vocaEdukia);
					out2.close();
				} else
					System.out.println(f.getPath() + " ez da dir");
			}
		} catch (IOException e) {
			log += "sortuOsoaGrammar: " + e.getMessage() + "\n";
			e.printStackTrace();
		}
	}

	private static void sortuChooseGrammarVoca() {
		String esaldiPath = saveFolder + File.separator + "All"
				+ File.separator + "tipo_d";

		File file = new File(esaldiPath);
		if (!file.exists())
			file.mkdirs();

		chooseGrammarEdukia = "S : NS_B SENT NS_E\n" + chooseGrammarEdukia;
		chooseVocaEdukia = "% NS_B\n<s>\tsil\n\n% NS_E\n</s>\tsil\n\n"
				+ chooseVocaEdukia;

		try {
			// Create file
			FileWriter fstream = new FileWriter(esaldiPath + File.separator
					+ fileName + ".grammar");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(chooseGrammarEdukia);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		try {
			// Create file
			FileWriter fstream = new FileWriter(esaldiPath + File.separator
					+ fileName + ".voca");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(chooseVocaEdukia);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static void sortuDfaJconf() {

		String jconfEdukia = "-dfa " + fileName + ".dfa\n";
		jconfEdukia += "-v " + fileName + ".dict\n";
		jconfEdukia += "-h " + acModelFile + File.separator + "hmmdefs\n";
		jconfEdukia += "-hlist " + acModelFile + File.separator + "tiedlist\n";
		jconfEdukia += "-smpFreq 48000\n";

		File dir = new File(saveFolder);
		String[] azpiDir = dir.list();
		for (int i = 0; i < azpiDir.length; i++) {
			File f = new File(saveFolder + File.separator + azpiDir[i]);
			if (f.isDirectory()) {
				String[] azpiDir2 = f.list();
				for (int j = 0; j < azpiDir2.length; j++) {
					File f2 = new File(f.getAbsolutePath() + File.separator
							+ azpiDir2[j]);
					if (f2.isDirectory()) {
						try {
							String command = "mkdfa " + fileName;
							Runtime.getRuntime().exec(command, null, f2);

							try {
								// Create file
								FileWriter fstream = new FileWriter(f2
										.getAbsolutePath()
										+ File.separator + fileName + ".jconf");
								BufferedWriter out = new BufferedWriter(fstream);
								out.write(jconfEdukia);
								// Close the output stream
								out.close();
							} catch (Exception e) {// Catch exception if any
								log += "sortuDfaJconf: jconf; "
										+ e.getMessage() + "\n";
								System.err.println("Error: " + e.getMessage());
							}

						} catch (IOException e) {
							log += "sortuDFA: mkdfa; " + e.getMessage() + "\n";
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

}
