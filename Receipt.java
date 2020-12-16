package project2;

import java.util.Set;

public class Receipt {

    private final String memberNumber;
    private final String date;
    private final Set<String> items;

    public String getMemberNumber() {
        return memberNumber;
    }

    public String getDate() {
        return date;
    }

    public Set<String> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return memberNumber + "," + date + "," + items.size() + "," + items;
    }

    public Receipt(String memberNumber, String date, Set<String> items) {
        this.memberNumber = memberNumber;
        this.date = date;
        this.items = items;
    }
}
