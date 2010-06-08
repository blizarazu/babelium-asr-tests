package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

public class Dictionary extends HashMap<String, Vector<String>> {

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
				Vector<String> fonemak = new Vector<String>();
				for (int i = 2; i < lerroa.length; i++)
					fonemak.addElement(lerroa[i]);
				this.put(lerroa[0], fonemak);
			}

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void print(){
		Set<String> keys = this.keySet();
		for(String key: keys){
			System.out.print(key + "\t");
			Vector<String> v = this.get(key);
			for(String f: v)
				System.out.print(f + " ");
			System.out.println();
		}
	}

}
