/**
 * The Share class represents a single entry of a Share price retrieved from:
 * https://markets.businessinsider.com/index/components/s&p_500
 *
 * @Author: Allen Jue
 * @Since: 2022-08-17
 */
package webScraper.jue;

public class Share implements Comparable<Share> {
    private final String name;
    private final double latestPrice;
    private final double change;
    private final double change_3Months;
    private final double change_6Months;
    private final double change_1Year;

    /**
     * Creates a Share object that stores data about that share: name, latestPrice, change in price (daily),
     * change in price (3 Months), change in price (6 Months), change in price (1 Year).
     *
     * @param name of share
     * @param latestPrice of share since last market close
     * @param change in price of share recently
     * @param change_3Months change in price of share over 3 Months
     * @param change_6Months change in price of share over 6 Months
     * @param change_1Year change in price of share over 1 Year
     */
    public Share(String name, double latestPrice, double change, double change_3Months,
                 double change_6Months, double change_1Year) {
        this.name = name;
        this.latestPrice = latestPrice;
        this.change = change;
        this.change_3Months = change_3Months;
        this.change_6Months = change_6Months;
        this.change_1Year = change_1Year;
    }

    /**
     * Gets the String representation of a Share's data
     *
     * @return the concatenated fields of a share
     */
    @Override
    public String toString() {
        return "[name: " + name + " | latestPrice: " + latestPrice + " | change: " + change
            + " | change 3 Mo: " + change_3Months + " | change 6 Mo: " + change_6Months
            + " | change 1 Y: " + change_1Year;
    }

    /**
     * Gets the name of the Share
     *
     * @return this.name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the latest price of the Share
     *
     * @return this.latestPrice
     */
    public double getLatestPrice() {
        return latestPrice;
    }

    /**
     * Gets the latest price change of the Share
     *
     * @return this.change
     */
    public double getChange() {
        return change;
    }

    /**
     * Gets the change in price of the Share over a year
     *
     * @return this.change_1Year
     */
    public double getChange_1Year() {
        return change_1Year;
    }

    /**
     * Gets the change in price of the Share over 3 Months
     *
     * @return this.change3Months
     */
    public double getChange_3Months() {
        return change_3Months;
    }

    /**
     * Gets the change in price of the Share over 6 Months
     *
     * @return this.change_6Months
     */
    public double getChange_6Months() {
        return change_6Months;
    }


    /**
     * compareTo method for Shares. It makes it so that Shares will be sorted based
     * on price change in ascending order
     *
     * @param o the object to be compared.
     * @return (int) (this.change - o.change)
     */
    @Override
    public int compareTo(Share o) {
        int diffInChange = (int) (this.change - o.change);
        return diffInChange;
    }
}
