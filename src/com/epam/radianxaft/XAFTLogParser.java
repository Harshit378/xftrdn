package com.epam.radianxaft;

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
import org.json.JSONObject;

public class XAFTLogParser {

	private String path;

	XAFTLogParser(String path) {
		this.path = path;
	}

	public static void main(String[] args) throws IOException {
		String path = "D:\\OfficeStuff\\Projects\\Radian_xAFT_CICD\\Log_Files\\AutomatedFunctionalTester_20180312_153253.log";
		XAFTLogParser rdr = new XAFTLogParser(path);
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
			System.out.println("The line where test suite report is " + s);
			createJsonFile(s);
		}

	}

	private void createJsonFile(String s) throws IOException {
		String[] suiteCount = s.split(",");
		for (String status : suiteCount) {
			System.out.println(status);
		}

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

		FileWriter xaftCICDJsonFile = new FileWriter("./jsonReport.json");
		xaftCICDJsonFile.write(xaftCICDReport.toString());
		xaftCICDJsonFile.flush();
		xaftCICDJsonFile.close();

		System.out.println(xaftCICDReport);
		System.exit(0);
	}

}
