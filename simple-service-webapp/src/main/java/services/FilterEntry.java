package services;

public class FilterEntry {
	private String quertId;
	private long filterId;
	private String filterValue;

	public FilterEntry(String quertId, long filterId, String filterValue) {
		super();
		this.quertId = quertId;
		this.filterId = filterId;
		this.filterValue = filterValue;
	}

	public String getQuertId() {
		return quertId;
	}

	public void setQuertId(String quertId) {
		this.quertId = quertId;
	}

	public long getFilterId() {
		return filterId;
	}

	public void setFilterId(long filterId) {
		this.filterId = filterId;
	}

	public String getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

	@Override
	public String toString() {
		return "FilterEntry [quertId=" + quertId + ", filterId=" + filterId
				+ ", filterValue=" + filterValue + "]";
	}
}
