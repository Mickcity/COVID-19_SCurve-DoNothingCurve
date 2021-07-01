public class CountryInfected {
	private String country;
	private int[] infected;

	public CountryInfected(String country, int[] infected) {
		this.country = country;
		this.infected = infected;
	}

	public String getCountryName() {
		return this.country;
	}

	public int[] getInfectedList() {
		return this.infected;
	}
}