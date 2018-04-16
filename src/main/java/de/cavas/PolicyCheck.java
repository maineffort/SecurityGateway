package de.cavas;

import java.util.Arrays;
import java.util.List;

import org.zaproxy.clientapi.core.Alert.Risk;

public class PolicyCheck {

//	public static void main(String[] args) {
//
//		// initializing unsorted int array
//		int iArr[] = { 2, 1, 9, 6, 4 };
//
//		// let us print all the elements available in list
//		for (int number : iArr) {
//			System.out.println("Number = " + number);
//		}
//
//		// sorting array
//		Arrays.sort(iArr);
//
//		// let us print all the elements available in list
//		System.out.println("The sorted int array is:");
//		for (int number : iArr) {
//			System.out.println("Number = " + number);
//		}
//		System.out.println("biggest number = " + iArr[iArr.length - 1]);
//
//	}

	public static String riskCheck(List<Risk> riskList) {

		String riskCheck = null;
		String riskAnalysis = null;
		int lowRiskCount = 0;
		int mediumRiskCount = 0;
		int highRiskCount = 0;
		int informationalRiskCount = 0;
		for (Risk risks : riskList) {
			riskCheck = String.valueOf(risks);

			if (riskCheck.equalsIgnoreCase("High")) {
				highRiskCount++;
			} else if (riskCheck.equalsIgnoreCase("Medium")) {
				mediumRiskCount++;
			} else if (riskCheck.equalsIgnoreCase("Low")) {
				lowRiskCount++;
			} else if (riskCheck.equalsIgnoreCase("Informational")) {
				informationalRiskCount++;
			} else

				System.err.println("no risk value returned !!! ");

		}

		int[] riskArray = { informationalRiskCount, lowRiskCount, mediumRiskCount, highRiskCount };

		Arrays.sort(riskArray);
		int totalRisk = informationalRiskCount + lowRiskCount + mediumRiskCount + highRiskCount;

//		System.out.println(totalRisk +  " @ PolicyCheck");
		int mostRisks = riskArray[riskArray.length - 1];

		if (mostRisks == informationalRiskCount) {
			riskAnalysis = "INFORMATIONAL";
		} else if (mostRisks == lowRiskCount) {
			riskAnalysis = "LOW";
		} else if (mostRisks == mediumRiskCount) {
			riskAnalysis = "MEDIUM";
		} else if (mostRisks == highRiskCount) {
			riskAnalysis = "HIGH";
		}
		// System.out.println("sorted list : " + Arrays.sort(riskArray));

		// riskArray = {informationalRiskCount, lowRiskCount, mediumRiskCount,
		// highRiskCount};

		// log("Total number of risks: " + riskList.size());
		// log("High Risk : " + highRiskCount + "\n" + "Medium Risk : " +
		// mediumRiskCount + "\n Low Risk : " + lowRiskCount + "\n Informational Risk :
		// " + informationalRiskCount);
		// implement policy

		// if (riskCheck.equalsIgnoreCase("Medium")) {
		// System.err.println(target + " FAILED POLICY CHECK");
		// probationList.remove(target);
		// System.err.println(" REGISTRATION REQUEST REJECTED ");
		// handleCancelation(info.getAppGroupName(), info.getId(), false);
		// //
		// } else {
		// System.out.println( target + " PASSED POLICY CHECK");
		// probationList.remove(target);
		// System.out.println(" REGISTRATION REQUEST APPROVED ");
		// info.setStatus(com.netflix.appinfo.InstanceInfo.InstanceStatus.UP);
		//
		// }

		System.out.println(riskAnalysis + " @ PolicyCheck");
		return riskAnalysis;

	}

	
}
