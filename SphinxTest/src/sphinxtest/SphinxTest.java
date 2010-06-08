package sphinxtest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.jsapi.JSGFGrammar;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;

public class SphinxTest {

	private Recognizer recognizer;
	private JSGFGrammar jsgfGrammarManager;
	private Microphone microphone;

	private String logPath;
	private String log;

	private int totalCorrect = 0;
	private int totalIncorrect = 0;

	private String actualSentence;
	private String actualTestType;

	private String speakerLevel;

	private List<String> sentences;
	private List<String> testTypes;
	
	private String sentenceListFile;

	private static final String COMPLETE_GRAMMAR_NAME = "tipo_c";
	private static final String CHOOSE_GRAMMAR_NAME = "tipo_d";

	public SphinxTest(String listFile, String logPath) {
		URL url = SphinxTest.class.getResource("sphinxtest.config.xml");
		ConfigurationManager cm = new ConfigurationManager(url);

		recognizer = (Recognizer) cm.lookup("recognizer");
		jsgfGrammarManager = (JSGFGrammar) cm.lookup("jsgfGrammar");
		microphone = (Microphone) cm.lookup("microphone");

		this.sentenceListFile = listFile;
		
		this.logPath = logPath;
		this.log = "";

		loadSentences();
		loadTestTypes();
	}

	private void loadSentences() {
		/*
		 * sentences = Arrays.asList("fine thank you", "nice to meet you",
		 * "they're very light", "that's a nice car", "i have three sisters",
		 * "i'll be nineteen next month", "it's just like mine",
		 * "i don't speak english very well");
		 */

		sentences = new ArrayList<String>();

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(this.sentenceListFile));

			String line;
			while ((line = br.readLine()) != null) {
				String lerroBerri = line.replaceAll(
						"[!\\\"#%&()\\*+,\\./:;<=>?@[\\\\]_`{|}~]|^-", "");
				lerroBerri = lerroBerri.replaceAll(" +- ?", " ");
				lerroBerri = lerroBerri.replaceAll("[ \\t\\r\\n\\v\\f]+", " ");
				lerroBerri = lerroBerri.trim();
				lerroBerri = lerroBerri.toLowerCase();

				if (lerroBerri.length() > 0)
					sentences.add(lerroBerri);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void loadTestTypes() {
		testTypes = Arrays.asList("tipo_0", "tipo_a", "tipo_b",
				"tipo_c", "tipo_d");
	}

	public void execute() {
		System.out.println("SphinxTest\n");

		System.out.println("Loading recognizer...");
		recognizer.allocate();
		System.out.println("Ready\n");

		if (!microphone.startRecording()) {
			System.out.println("Cannot start microphone.");
			recognizer.deallocate();
			System.exit(1);
		}

		System.out.println("Select speaker level:");
		System.out.println("1 - High");
		System.out.println("2 - Medium");
		System.out.println("3 - Low");

		int lev = 0;
		while (lev == 0) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				lev = Integer.parseInt(br.readLine());
			} catch (IOException ioe) {
				System.out.println("Invalid level number");
			} catch (NumberFormatException e) {
				System.out.println("Invalid level number");
			}
		}

		switch (lev) {
		case 1:
			speakerLevel = "high";
			break;
		case 2:
			speakerLevel = "medium";
			break;
		case 3:
			speakerLevel = "low";
			break;
		default:
			speakerLevel = "unknown";
			break;
		}

		while (true) {
			actualSentence = "";

			System.out.println("Select sentence:");
			for (int i = 0; i < sentences.size(); i++)
				System.out.println(i + " - " + sentences.get(i));

			System.out.print("\nEnter number: ");
			int resp1;
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				resp1 = Integer.parseInt(br.readLine());
			} catch (IOException ioe) {
				System.out.println("Invalid level number");
				resp1 = -2;
			} catch (NumberFormatException e) {
				System.out.println("Invalid level number");
				resp1 = -2;
			}

			if (resp1 > -1) {
				actualSentence = sentences.get(resp1);

				System.out.println();
				while (true) {
					actualTestType = "";
					System.out.println("Select test type:");
					for (int i = 0; i < testTypes.size(); i++)
						System.out.println(i + " - " + testTypes.get(i));
					System.out.println("-1 - Return");

					System.out.print("\nEnter number: ");
					int resp2;
					try {
						BufferedReader br = new BufferedReader(
								new InputStreamReader(System.in));
						resp2 = Integer.parseInt(br.readLine());

					} catch (IOException ioe) {
						System.out.println("Invalid level number");
						resp2 = -2;
					} catch (NumberFormatException e) {
						System.out.println("Invalid level number");
						resp2 = -2;
					}

					if (resp2 > -1) {
						actualTestType = testTypes.get(resp2);

						String grammarName;
						if (actualTestType.equals(COMPLETE_GRAMMAR_NAME)
								|| actualTestType.equals(CHOOSE_GRAMMAR_NAME))
							grammarName = actualTestType;
						else
							grammarName = actualSentence.replace(' ', '_')
									+ "_" + actualTestType;

						try {
							loadAndRecognize(grammarName);
						} catch (IOException e) {
							System.out.println("Grammar " + grammarName
									+ " doesn't exist");
						}
					} else if (resp2 == -1) {
						writeLog();
						actualSentence = "";

						totalCorrect = 0;
						totalIncorrect = 0;
						break;
					}
				}
			} else if (resp1 == -1)
				break;
		}
	}

	private void loadAndRecognize(String grammarName) throws IOException {
		jsgfGrammarManager.loadJSGF(grammarName);

		String results = "";
		int correct = 0;
		int incorrect = 0;

		while (true) {
			System.out.println("Start speaking: " + actualSentence + "\n");

			Result recogResult = recognizer.recognize();
			if (recogResult != null) {
				String recogResultText = recogResult
						.getBestFinalResultNoFiller();
				System.out.println("You said: " + recogResultText + '\n');
				System.out
						.println("Correct result? (C - correct | I - incorrect | [S] - skip this one| R - return)");

				String resp = "";
				try {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(System.in));
					resp = br.readLine();
				} catch (IOException ioe) {
					System.out.println("Result not saved");
				}

				if (resp.equalsIgnoreCase("C")) {
					results += "\n\nResult: " + recogResultText + "\nCORRECT";
					correct++;
				} else if (resp.equalsIgnoreCase("I")) {
					results += "\n\nResult: " + recogResultText + "\nINCORRECT";
					incorrect++;
				} else if (resp.equalsIgnoreCase("R")) {
					break;
				}
			} else {
				System.out.println("I can't hear what you said.\n");
			}
		}

		logResult(results, correct, incorrect, grammarName);
	}

	private void logResult(String results, int correct, int incorrect,
			String grammarName) {
		log += "=============================================================================\n";
		log += "  Sentence: " + actualSentence + "\n";
		log += "  Test type: " + actualTestType + "\n";
		log += "  Grammar name:" + grammarName + "\n";
		log += "=============================================================================";
		log += "\n\n";
		log += results;
		log += "\n\n";
		log += "-----------\n";
		log += "Correct: " + correct + "; Incorrect: " + incorrect;
		log += "\n\n\n";

		totalCorrect += correct;
		totalIncorrect += incorrect;
	}

	private void writeLog() {
		if (!log.isEmpty()) {
			String header = "Speaker level: " + speakerLevel.toUpperCase()
					+ "\n" + "Sentence: " + actualSentence + "\n"
					+ "------------------------------" + "\n\n\n"
					+ "Total correct: " + totalCorrect + " ; Total incorrect: "
					+ totalIncorrect;
			log = header + "\n\n\n=======RESULTS======\n" + log;

			File file = new File(logPath + File.separator + speakerLevel);
			if (!file.exists())
				file.mkdirs();

			try {
				FileWriter fstream = new FileWriter(logPath + File.separator
						+ speakerLevel + File.separator
						+ actualSentence.replace(" ", "_") + "_" + speakerLevel
						+ "_results");
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(log);
				out.close();
				log = "";
			} catch (Exception e) {
				System.out.println("Error writing results: " + e.getMessage());
			}

		}
	}

	/**
	 * @param args
	 *            The path to the folder where the files with the results will
	 *            be saved
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out
					.println("Missing parameters: Specify the sentence list and the folder for saving results");
			System.exit(1);
		}
		SphinxTest sphinxTest = new SphinxTest(args[0], args[1]);
		sphinxTest.execute();
	}

}
