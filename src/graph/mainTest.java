package graph;

import java.io.File;

import interfaces.Graph;

public class mainTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Graph graph = new UndirectedGraphReader().read(new File("E:\\Programming\\Java\\GeneralPurposeCodeJava\\test\\structures\\undirectedGraph\\testFiles\\instance199.gr"));
		
	}

}
