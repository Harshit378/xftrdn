package com.epm.rdnxft;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class XFTLogParser {

	private String path;

	XFTLogParser(String path) {
		this.path = path;
	}

	public static void main(String[] args) throws IOException {
		String path = args[0];
		XFTLogParser rdr = new XFTLogParser(path);
		rdr.readFile();
	}

	public String readFile() throws IOException {
		File logFile = new File(path);
		if (logFile.exists() && logFile.isFile()) {
			Stream<String> str = Files.lines(Paths.get(path), Charset.defaultCharset());
			str.collect(Collectors.toCollection(LinkedList::new)).descendingIterator().forEachRemaining(s -> {
				try {
					checkForPassFail(s);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			str.close();
		}
		return "";
	}

	private void checkForPassFail(String s) throws IOException {
		if (s.contains("(TestSuite.java:236)")) {
			createJsonFile(s);
		}
	}

	private void createJsonFile(String s) throws IOException {
		try(FileWriter xaftCICDJsonFile = new FileWriter("./jsonReport.json");) {
			String[] suiteCount = s.split(",");

			String scenariosExecuted = StringUtils.substringBetween(suiteCount[0], "[", "]");
			String scenariosPassed = StringUtils.substringBetween(suiteCount[1], "[", "]");
			String scenariosFailed = StringUtils.substringBetween(suiteCount[2], "[", "]");
			String scenariosNoRun = StringUtils.substringBetween(suiteCount[3], "[", "]");
			int totalScenarios = Integer.parseInt(scenariosExecuted) + Integer.parseInt(scenariosNoRun);

			JSONObject xaftCICDReport = new JSONObject();
			xaftCICDReport.put("totalScenario", String.valueOf(totalScenarios));
			xaftCICDReport.put("scenarioPassed", scenariosPassed);
			xaftCICDReport.put("scenarioFailed", scenariosFailed);
			xaftCICDReport.put("passPercentage",
					String.valueOf((Integer.parseInt(scenariosPassed) * 100) / totalScenarios));
			
			xaftCICDJsonFile.write(xaftCICDReport.toString());
			xaftCICDJsonFile.flush();
			System.out.println("Successfully generated json file jsonReport.json ");
			System.out.println(xaftCICDReport.toString(4));
		} catch (NumberFormatException | JSONException e) {
			System.err.println("Exception occurred !!! " + e.getMessage());
		} finally {
			System.exit(0);
		}
	}

}
