package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class Dictionary extends Vector<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String hiztegiFitx;

	public Dictionary(String fitxIzena) {
		super();
		this.hiztegiFitx = fitxIzena;
		kargatuHiztegia();
	}

	private void kargatuHiztegia() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					this.hiztegiFitx));

			String line;
			while ((line = br.readLine()) != null) {
				String[] lerroa = line.split("[ ]+");
				this.addElement(lerroa[0].split("\\([0-9]+\\)")[0]);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void print() {
		for (String f : this)
			System.out.println(f + " ");
	}

}
