import java.io.*;
import java.util.Scanner;
import java.util.Arrays;

public class Proj2190101_3_6 {
	public static CountryInfected[] getInfected() throws IOException {
		CountryInfected[] countries;

		Scanner scanner = new Scanner(new File("src/time_series_covid19_confirmed_global.csv"));

		int i = 0;
		while (scanner.hasNext()) {
			scanner.next();
			i++;
		}

		countries = new CountryInfected[i];

		scanner = new Scanner(new File("src/time_series_covid19_confirmed_global.csv"));
		int days = scanner.nextLine().split(",").length - 4;
		String[] countryList = new String[i];
		int[][] infectedList = new int[i][days];

		int j = 0;
		String countryName = "";
		while (scanner.hasNext()) {
			String line = scanner.nextLine();

			String[] info = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

			boolean sameCountry = false;
			if (countryName.equals(info[1])) {
				sameCountry = true;
			}
			countryName = info[1];

			if (countryName.equals("Holy See")) {
				countryName = "Vatican City";
			} else if (countryName.equals("\"Korea, South\"")) {
				countryName = "South Korea";
			} else if (countryName.equals("Taiwan*")) {
				countryName = "Taiwan";
			}

			if (sameCountry) {
				for (int k = 0; k < days; k++) {
					infectedList[j][k] += Integer.parseInt(info[k + 4]);
				}
			} else {
				j++;
				for (int k = 0; k < days; k++) {
					infectedList[j][k] = Integer.parseInt(info[k + 4]);
				}
			}
			countryList[j] = countryName;
		}
		int n = 0;
		for (int k = 0; k < i; k++) {
			if (infectedList[k][days - 1] <= 100) {
				continue;
			}
			countries[n] = new CountryInfected(countryList[k], infectedList[k]);
			n++;
		}
		int k = 0;
		for (k = 0; k < i; k++) {
			if (countries[k].getCountryName().equals("Zimbabwe")) {
				break;
			}
		}
		CountryInfected[] finalCountries = new CountryInfected[k + 1];
		finalCountries = Arrays.copyOfRange(countries, 0, k + 1);
		return finalCountries;
	}

	public static void main(String[] args) throws IOException {
		CountryInfected[] countries = getInfected();
		int fra = 0;
		int ger = 0;
		int ned = 0;
		for (int i = 0; i < countries.length; i++) {
			if (countries[i].getCountryName().equals("France")) {
				fra = i;
			} else if (countries[i].getCountryName().equals("Germany")) {
				ger = i;
			} else if (countries[i].getCountryName().equals("Netherlands")) {
				ned = i;
			}
		}
		int[] countrySet = {fra, ger, ned};
		double[] paramLower = {0.0, 0.0, 0.0, 0.0};
		double[] paramUpper = {10000000, 1000, 1.0, 10000000};
		String[] showSCurve = new String[countrySet.length];
		int numFutureDays = 90;
		int startDay = 350;
		for (int j = 0; j < showSCurve.length; j++) {
			int[] pastData = new int[countries[countrySet[j]].getInfectedList().length - startDay];
			for (int g = 0; g < pastData.length; g++) {
				pastData[g] = countries[countrySet[j]].getInfectedList()[g + startDay];
			}
			showSCurve[j] =  Arrays.toString(getSCurve(pastData, numFutureDays, paramLower, paramUpper));
		}
		PrintStream ps = new PrintStream(new File("3_6.csv"));
		for (int k = 0; k < countrySet.length; k++) {
			ps.println(showSCurve[k].substring(1, showSCurve[k].length() - 1));
			ps.println(getDonothingCumulative(countries[countrySet[k]].getInfectedList(), numFutureDays)); // DonothingCumulative 
		}
		ps.close();
		System.out.println("Finished!");
	}

	public static double[] getDoNothingCurve(int[] pastData, int numFutureDay) {

		double increaseNum;
		double[] infectedNumSet = new double[numFutureDay];
		double[] totalList = new double[pastData.length + numFutureDay];
		for (int i = 0; i < pastData.length; i++) {
			totalList[i] = pastData[i];
		}
		int countFutureDay = 0;
		for (int countDay = pastData.length; countDay < totalList.length; countDay++) {
			double numer = (totalList[countDay - 1] - totalList[countDay - 2])
					/ (totalList[countDay - 2] - totalList[countDay - 3])
					+ (totalList[countDay - 2] - totalList[countDay - 3])
							/ (totalList[countDay - 3] - totalList[countDay - 4])
					+ (totalList[countDay - 3] - totalList[countDay - 4])
							/ (totalList[countDay - 4] - totalList[countDay - 5]);
			increaseNum = (totalList[countDay - 1] - totalList[countDay - 2]) * (numer / 3);
			totalList[countDay] = increaseNum + totalList[countDay - 1];
			infectedNumSet[countFutureDay] = increaseNum;
			countFutureDay++;
		}

		return infectedNumSet;
	}

	public static String getDonothingCumulative(int[] pastData, int numFutureDays) throws IOException {
		double[] predictList = new double[numFutureDays];
		double[] increaseNumSet = getDoNothingCurve(pastData, numFutureDays); 
		int finalInfected = pastData[pastData.length - 1];
		predictList[0] = increaseNumSet[0] + finalInfected;
		for (int day = 0; day < numFutureDays - 1; day++) {
			predictList[day + 1] = predictList[day] + increaseNumSet[day + 1];
		}
		String x = (Arrays.toString(predictList).substring(1, Arrays.toString(predictList).length() - 1));
		return x;

	}

	public static double[] getSCurve(int[] pastData,int numFutureDays, double[] paramLowerBounds,
	double[] paramUpperBounds){
		double[] SDLM = {0.0,0.0,0.0,0.0};
		double[] bestSDLM = SDLM.clone();
		int numberOfChoices = 50;
		int numberOfPastDays = pastData.length;
		double minError = 1E20d;
		for (int i1 = 0; i1 < numberOfChoices; i1++) {
			for (int i2 = 0; i2 < numberOfChoices; i2++ ) {
				for (int i3 = 0; i3 < numberOfChoices; i3++) {
					for (int i4 = 0; i4 < numberOfChoices; i4++) {
						int[] config = {i1,i2,i3,i4};
						for (int j = 0; j < 4; j++) {
                        	SDLM[j] = paramLowerBounds[j] + (paramUpperBounds[j] - paramLowerBounds[j]) / (numberOfChoices - 1) * config[j];
                        }
                        double[] forecast = SCurveForecast(SDLM,1,numberOfPastDays);
                        double error = MSE(pastData,forecast);
                        if (error < minError){
                        	minError = error;
                        	bestSDLM = SDLM.clone();
                        }
					}
				}
			}
		}
		System.out.println("The fitted S-curve model has S = [" + bestSDLM[0] + "]," + "D = [" + bestSDLM[1] + "],"
                + "L = [" + bestSDLM[2] + "]," + "M = [" + bestSDLM[3] + "], "
                + "with the first projected day being d = [" + (numberOfPastDays + 1) + "]");
        return SCurveForecast(bestSDLM,numberOfPastDays + 1, numberOfPastDays + numFutureDays);      
	}

	public static double sigmoid(double[] SDLM, int d) {
        return SDLM[0] + SDLM[3] / (1 + Math.exp(-SDLM[2] * (d - SDLM[1])));
    }
    public static double MSE(int[] pastData, double[] forecast) {
    	int numberOfDays = pastData.length;
        double sum = 0;
        for (int i = 0; i < numberOfDays; i++) {
            sum += Math.pow((forecast[i] - pastData[i]), 2);
        }
        return sum / numberOfDays;
    }
    public static double[] SCurveForecast(double[] SDLM,int beginDay, int endDay){
    	int numberOfDays = endDay - beginDay + 1;
    	double[] forecast = new double[numberOfDays];
    	for (int i = 0; i < numberOfDays; i++) {
    		forecast[i] = sigmoid(SDLM,i + beginDay);
    	}
    	return forecast;
    }
}